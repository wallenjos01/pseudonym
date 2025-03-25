package org.wallentines.pseudonym.text;

import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.SNBTCodec;
import org.wallentines.mdcfg.serializer.*;
import org.wallentines.mdcfg.registry.Identifier;
import org.wallentines.mdcfg.registry.Registry;

import java.util.*;

public interface HoverEvent {

    Action<?> action();

    static Simple<Component> create(Component component) {
        return new Simple<>(Action.SHOW_TEXT, component);
    }

    static Simple<ItemInfo> forItem(ItemInfo item) {
        return new Simple<>(Action.SHOW_ITEM, item);
    }

    static Simple<EntityInfo> forEntity(EntityInfo ent) {
        return new Simple<>(Action.SHOW_ENTITY, ent);
    }

    record Simple<T>(HoverEvent.Simple.Action<T> action, T value) implements HoverEvent {

        public static class Action<T> implements HoverEvent.Action<Simple<T>> {
            private final Serializer<Simple<T>> serializer;

            Action(Serializer<T> serializer) {
                this.serializer = serializer.flatMap(Simple::value, value -> new Simple<>(this, value));
            }

            @Override
            public Serializer<Simple<T>> serializer() {
                return serializer;
            }
        }
    }

    interface Action<T extends HoverEvent> {
        Serializer<T> serializer();
        Registry<String, Action<?>> REGISTRY = Registry.createStringRegistry();

        static <T extends HoverEvent, A extends Action<T>> A register(String name, A action) {
            REGISTRY.register(name, action);
            return action;
        }

        Simple.Action<Component> SHOW_TEXT = register("show_text", new Simple.Action<>(
                ProtocolContext.select(ctx -> {
                    if(ctx.hasFeature(Features.HOVER_CONTENTS) && !ctx.hasFeature(Features.INLINE_HOVER_CONTENTS)) {
                        return Component.SERIALIZER.fieldOf("contents");
                    }
                    return Component.SERIALIZER.fieldOf("value");
                })
        ));


        Simple.Action<ItemInfo> SHOW_ITEM = register("show_item", new Simple.Action<>(ItemInfo.SERIALIZER));
        Simple.Action<EntityInfo> SHOW_ENTITY = register("show_entity", new Simple.Action<>(EntityInfo.SERIALIZER));
    }

    Serializer<HoverEvent> SERIALIZER = Action.REGISTRY.byIdSerializer().fieldOf("action").dispatch(Action::serializer, HoverEvent::action);
    Serializer<HoverEvent> MAP_SERIALIZER = ProtocolContext.select(ctx -> {
        if(ctx.hasFeature(Features.INLINE_HOVER_CONTENTS)) return SERIALIZER.fieldOf("hover_event");
        return SERIALIZER.fieldOf("hoverEvent");
    });



    record ItemInfo(Identifier type, int count, Byte damage, Map<Identifier, ConfigObject> components) {

        private static final Identifier CUSTOM_DATA = Identifier.parse("minecraft:custom_data");

        public static ItemInfo modern(Identifier type, int count, Map<Identifier, ConfigObject> components) {
            return new ItemInfo(type, count, null, components);
        }

        public static ItemInfo tagged(Identifier type, int count, ConfigSection tag) {
            Map<Identifier, ConfigObject> components = tag == null ? Collections.emptyMap() : Map.of(CUSTOM_DATA, tag);
            return new ItemInfo(type, count, null, components);
        }

        public static ItemInfo legacy(Identifier type, int count, byte damage, ConfigSection tag) {
            Map<Identifier, ConfigObject> components = tag == null ? Collections.emptyMap() : Map.of(CUSTOM_DATA, tag);
            return new ItemInfo(type, count, damage, components);
        }

        public ConfigSection getCustomData() {
            ConfigObject out = components.get(CUSTOM_DATA);
            if(out == null) return null;
            return out.asSection();
        }

        public byte getLegacyDamageValue() {
            return damage == null ? 0 : damage;
        }


        private static final SNBTCodec LEGACY_SNBT = new SNBTCodec(false, false).useDoubleQuotes();

        public static final Serializer<ItemInfo> LEGACY = ObjectSerializer.create(
                Identifier.serializer("minecraft").entry("id", ItemInfo::type),
                Serializer.INT.entry("Count", ItemInfo::count).orElse(1),
                Serializer.BYTE.entry("Damage", ItemInfo::getLegacyDamageValue).orElse((byte) 0),
                ConfigSection.SERIALIZER.mapToString(LEGACY_SNBT).entry("tag", ItemInfo::getCustomData).optional(),
                ItemInfo::legacy
        ).mapToString(LEGACY_SNBT).fieldOf("value");

        public static final Serializer<ItemInfo> TAGGED_STRING = ObjectSerializer.create(
                Identifier.serializer("minecraft").entry("id", ItemInfo::type),
                Serializer.INT.entry("Count", ItemInfo::count).orElse(1),
                ConfigSection.SERIALIZER.mapToString(LEGACY_SNBT).entry("tag", ItemInfo::getCustomData).optional(),
                ItemInfo::tagged
        ).mapToString(LEGACY_SNBT).fieldOf("value");

        public static final Serializer<ItemInfo> TAGGED = ObjectSerializer.create(
                Identifier.serializer("minecraft").entry("id", ItemInfo::type),
                Serializer.INT.entry("count", ItemInfo::count).orElse(1),
                ConfigSection.SERIALIZER.entry("tag", ItemInfo::getCustomData).optional(),
                ItemInfo::tagged).fieldOf("contents");

        public static final Serializer<ItemInfo> MODERN = ObjectSerializer.create(
                Identifier.serializer("minecraft").entry("id", ItemInfo::type),
                Serializer.INT.entry("count", ItemInfo::count).orElse(1),
                ConfigObject.SERIALIZER.mapOf(Identifier.serializer("minecraft")).entry("components", ItemInfo::components).orElse(Collections.emptyMap()),
                ItemInfo::modern).fieldOf("contents");

        public static final Serializer<ItemInfo> MODERN_INLINE = ObjectSerializer.create(
                Identifier.serializer("minecraft").entry("id", ItemInfo::type),
                Serializer.INT.entry("count", ItemInfo::count).orElse(1),
                ConfigObject.SERIALIZER.mapOf(Identifier.serializer("minecraft")).entry("components", ItemInfo::components).orElse(Collections.emptyMap()),
                ItemInfo::modern);

        public static final Serializer<ItemInfo> SERIALIZER = ProtocolContext.select(ctx -> {
                if(ctx.hasFeature(Features.INLINE_HOVER_CONTENTS)) return MODERN_INLINE;
                if(ctx.hasFeature(Features.ITEM_COMPONENTS)) return MODERN;
                if(ctx.hasFeature(Features.HOVER_CONTENTS)) return TAGGED;
                if(ctx.hasFeature(Features.NAMESPACED_IDS)) return TAGGED_STRING;
                return LEGACY;
            }
        );


    }

    record EntityInfo(Component name, Identifier type, UUID uuid) {



        private static final Serializer<UUID> UUID_ARRAY_SERIALIZER = Serializer.INT.listOf().mapToList().map(uuid -> {
            long u1 = uuid.getMostSignificantBits();
            long u2 = uuid.getLeastSignificantBits();
            int[] parts = new int[] { (int) (u1 >> 32), (int) u1, (int) (u2 >> 32), (int) u2 };
            return SerializeResult.success(Arrays.stream(parts).boxed().toList());
        },  ints -> {
            if(ints.size() != 4) return SerializeResult.failure("Expected 4 integers!");
            UUID uuid = new UUID(
                    (long) ints.get(0) << 32 | (long) ints.get(1) & 0xFFFFFFFFL,
                    (long) ints.get(2) << 32 | (long) ints.get(3) & 0xFFFFFFFFL
            );
            return SerializeResult.success(uuid);
        });

        public static final Serializer<UUID> UUID_SERIALIZER = ProtocolContext.select(ctx -> {
            if(ctx.hasFeature(Features.INT_ARRAY_UUIDS)) return UUID_ARRAY_SERIALIZER;
            return Serializer.UUID;
        });

        private static final Serializer<EntityInfo> CONTENTS_SERIALIZER = ObjectSerializer.create(
                Component.SERIALIZER.entry("name", EntityInfo::name),
                Identifier.serializer("minecraft").<EntityInfo>entry("type", (ei) -> ei.type).orElse(new Identifier("minecraft", "pig")),
                Serializer.UUID.entry("id", EntityInfo::uuid),
                EntityInfo::new
        );

        private static final Serializer<EntityInfo> LEGACY_SERIALIZER = CONTENTS_SERIALIZER.mapToString(new SNBTCodec(false, false).useDoubleQuotes());

        private static final Serializer<EntityInfo> MODERN_SERIALIZER = ObjectSerializer.create(
                Component.SERIALIZER.entry("name", EntityInfo::name),
                Identifier.serializer("minecraft").<EntityInfo>entry("id", (ei) -> ei.type).orElse(new Identifier("minecraft", "pig")),
                UUID_SERIALIZER.entry("uuid", EntityInfo::uuid),
                EntityInfo::new
        );

        public static final Serializer<EntityInfo> SERIALIZER = ProtocolContext.select(ctx -> {
            if(ctx.hasFeature(Features.INLINE_HOVER_CONTENTS)) return MODERN_SERIALIZER;
            if(ctx.hasFeature(Features.HOVER_CONTENTS)) return CONTENTS_SERIALIZER;
            return LEGACY_SERIALIZER;
        });

    }
}
