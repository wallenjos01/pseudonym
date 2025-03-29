package org.wallentines.pseudonym.text;

import org.wallentines.mdcfg.serializer.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public interface Component {

    Content content();

    Style style();

    List<Component> children();


    default MutableComponent copy() {
        return new MutableComponent(content(), style(), children());
    }

    default MutableComponent deepCopy() {
        List<Component> children = new ArrayList<>();
        for(Component c : children()) {
            children.add(c.deepCopy());
        }
        return new MutableComponent(content(), style(), children);
    }


    static MutableComponent empty() {
        return new MutableComponent(Content.Text.EMPTY, Style.EMPTY, Collections.emptyList());
    }

    static MutableComponent text(String text) {
        return new MutableComponent(new Content.Text(text), Style.EMPTY, Collections.emptyList());
    }

    static MutableComponent join(List<Component> list) {
        if(list.isEmpty()) return empty();
        if(list.size() == 1) return list.getFirst().copy();

        MutableComponent out = list.getFirst().copy();
        for(int i = 1; i < list.size(); i++) {
            out.append(list.get(i));
        }
        return out;
    }


    Serializer<Component> SERIALIZER = new Serializer<>() {

        private Serializer<Component> internal;
        private Supplier<Serializer<Component>> serializer = () -> {
            if(internal == null) {
                internal = new Serializer<Component>() {
                    @Override
                    public <O> SerializeResult<Component> deserialize(SerializeContext<O> context, O value) {

                        SerializeResult<Content> content = Content.SERIALIZER.deserialize(context, value);
                        if(!content.isComplete()) return SerializeResult.failure("Unable to deserialize component content!", content.getError());

                        SerializeResult<Style> style = Style.SERIALIZER.deserialize(context, value);
                        if(!style.isComplete()) return SerializeResult.failure("Unable to deserialize component style!", content.getError());

                        List<Component> outChildren = Collections.emptyList();

                        O extra = context.get("extra", value);
                        if(!context.isNull(extra)) {
                            SerializeResult<List<Component>> children = SERIALIZER.listOf().mapToList().deserialize(context, extra);
                            if(!children.isComplete()) {
                                return SerializeResult.failure("Unable to deserialize component children!", children.getError());
                            }
                            outChildren = children.getOrThrow();
                        }

                        return SerializeResult.success(new ImmutableComponent(content.getOrThrow(), style.getOrThrow(), outChildren));
                    }

                    @Override
                    public <O> SerializeResult<O> serialize(SerializeContext<O> context, Component value) {

                        SerializeResult<O> content = Content.SERIALIZER.serialize(context, value.content());
                        if(!content.isComplete()) return SerializeResult.failure("Unable to serialize component content!", content.getError());

                        SerializeResult<O> style = Style.SERIALIZER.serialize(context, value.style());
                        if(!style.isComplete()) return SerializeResult.failure("Unable to serialize component style!", content.getError());

                        O out = context.merge(content.getOrThrow(), style.getOrThrow());
                        if(!value.children().isEmpty()) {
                            SerializeResult<O> children = SERIALIZER.listOf().serialize(context, value.children());
                            if(!children.isComplete()) {
                                return SerializeResult.failure("Unable to serialize component children!", children.getError());
                            }
                            context.set("extra", children.getOrThrow(), out);
                        }

                        return SerializeResult.success(out);
                    }
                }
                .or(Serializer.STRING.flatMap(c -> "", Component::text))
                .or(SERIALIZER.listOf().mapToList().flatMap(List::of, Component::join));
            }
            return internal;
        };

        @Override
        public <O> SerializeResult<Component> deserialize(SerializeContext<O> serializeContext, O o) {
            return serializer.get().deserialize(serializeContext, o);
        }

        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> serializeContext, Component component) {
            return serializer.get().serialize(serializeContext, component);
        }
    };

}
