package org.wallentines.plib.text;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mdcfg.serializer.*;
import org.wallentines.mdcfg.registry.Registry;

import java.util.*;

public interface Content {

    Type<?> type();

    /**
     * A Content type which displays literal text
     */
    record Text(String text) implements Content {
        @Override
        public Type<?> type() {
            return Type.TEXT;
        }

        public static final Text EMPTY = new Text("");
        public static final Serializer<Text> SERIALIZER = InlineSerializer.RAW.fieldOf("text").flatMap(Text::text, Text::new);
    }

    /**
     * A Content type which displays Translatable text
     */
    record Translate(String key, @Nullable String fallback, List<Component> with) implements Content {


        public Translate(String key) {
            this(key, null, null);
        }

        @Override
        public Type<?> type() {
            return Type.TRANSLATE;
        }

        public static final Serializer<Translate> SERIALIZER = ObjectSerializer.create(
                Serializer.STRING.entry("translate", Translate::key),
                Serializer.STRING.entry("fallback", Translate::fallback).optional(),
                Component.SERIALIZER.listOf().mapToList().entry("with", Translate::with).orElse(Collections.emptyList()),
                Translate::new
        );
    }


    /**
     * A Content type which displays the key bound to a specific action on the client
     */
    record Keybind(String key) implements Content {

        @Override
        public Type<?> type() {
            return Type.KEYBIND;
        }

        public static final Serializer<Keybind> SERIALIZER = ObjectSerializer.create(
                Serializer.STRING.entry("keybind", con -> con.key),
                Keybind::new
        );
    }


    /**
     * A Content type which displays the scoreboard score of a specific entity and objective
     */
    record Score(String name, String objective) implements Content {

        public static final Serializer<Score> SERIALIZER = ObjectSerializer.create(
                Serializer.STRING.entry("name", Score::name),
                Serializer.STRING.entry("objective", Score::objective),
                Score::new
        );

        public static final Serializer<Score> FIELD_SERIALIZER = SERIALIZER.fieldOf("score");

        @Override
        public Type<?> type() {
            return Type.SCORE;
        }
    }


    /**
     * A content type which display's the display names of any entities which apply to the given selector text.
     */
    record Selector(String value, @Nullable Component separator) implements Content {

        public Selector(String value) {
            this(value, null);
        }

        public static final Serializer<Selector> SERIALIZER = ObjectSerializer.create(
                Serializer.STRING.entry("selector", Selector::value),
                Component.SERIALIZER.entry("separator", Selector::separator).optional(),
                Selector::new
        );


        @Override
        public Type<?> type() {
            return Type.SELECTOR;
        }
    }


    /**
     * A Content type which looks up NBT data on entities, block entities, or command storage
     */
    record NBT(String path, boolean interpret, Component separator, DataSourceType dataSourceType, String data) implements Content {

        @Override
        public Type<?> type() {
            return Type.NBT;
        }

        public enum DataSourceType {
            BLOCK,
            ENTITY,
            STORAGE
        }

        public static final Serializer<NBT> SERIALIZER = ObjectSerializer.create(
                Serializer.STRING.entry("nbt", NBT::path),
                Serializer.BOOLEAN.entry("interpret", NBT::interpret).optional(),
                Component.SERIALIZER.entry("separator", NBT::separator).optional(),
                Serializer.STRING.<NBT>entry("block", con -> con.dataSourceType == DataSourceType.BLOCK ? con.data : null).optional(),
                Serializer.STRING.<NBT>entry("entity", con -> con.dataSourceType == DataSourceType.ENTITY ? con.data : null).optional(),
                Serializer.STRING.<NBT>entry("storage", con -> con.dataSourceType == DataSourceType.STORAGE ? con.data : null).optional(),
                (path, interpret, sep, block, entity, storage) -> {

                    String data;
                    DataSourceType type;
                    if(block != null) {
                        data = block;
                        type = DataSourceType.BLOCK;
                    }
                    else if(entity != null) {
                        data = entity;
                        type = DataSourceType.ENTITY;
                    }
                    else if(storage != null) {
                        data = storage;
                        type = DataSourceType.STORAGE;
                    } else {
                        throw new IllegalArgumentException("Not enough data to deserialize NBT component!");
                    }

                    return new NBT(path, interpret, sep, type, data);
                }
        );
    }


    interface Type<T extends Content> {
        Serializer<T> serializer();

        Registry<String, Type<?>> TYPES = Registry.createStringRegistry();
        record Simple<T extends Content>(Serializer<T> serializer) implements Type<T> {}

        static <T extends Content> Type<T> register(String name, Serializer<T> serializer) {
            Type<T> out = new Simple<>(serializer);
            TYPES.register(name, out);
            return out;
        }

        @SuppressWarnings("unchecked")
        static <O, C extends Content> SerializeResult<O> serialize(SerializeContext<O> ctx, C content) throws SerializeException {
            Type<C> type = (Type<C>) content.type();
            return type.serializer().serialize(ctx, content);
        }


        Type<Text> TEXT = register("text", Text.SERIALIZER);
        Type<Translate> TRANSLATE = register("translate", Translate.SERIALIZER);
        Type<Keybind> KEYBIND = register("keybind", Keybind.SERIALIZER);
        Type<Score> SCORE = register("score", Score.FIELD_SERIALIZER);
        Type<Selector> SELECTOR = register("selector", Selector.SERIALIZER);
        Type<NBT> NBT = register("nbt", Content.NBT.SERIALIZER);
    }


    Serializer<Content> SERIALIZER = new Serializer<>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, Content value) {
            String id = Type.TYPES.getId(value.type());
            if(id == null) {
                return SerializeResult.failure("Unregistered type " + value.type());
            }
            return Type.serialize(context, value).flatMap(o -> context.toMap(Map.of(id, o)));
        }

        @Override
        public <O> SerializeResult<Content> deserialize(SerializeContext<O> context, O value) {
            if(!context.isMap(value)) return SerializeResult.failure("Expected a map!");
            for(String key : context.getOrderedKeys(value)) {
                Type<?> type = Type.TYPES.get(key);
                if(type != null) {
                    return type.serializer().deserialize(context, value).cast(Content.class);
                }
            }
            return SerializeResult.failure("Unable to determine component type!");
        }
    };

}
