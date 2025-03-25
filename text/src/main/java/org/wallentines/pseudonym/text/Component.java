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
                internal = GroupSerializer.<Component>builder()
                        .add(Content.SERIALIZER, Component::content)
                        .add(Style.SERIALIZER, Component::style)
                        .add(SERIALIZER.listOf().mapToList().optionalFieldOf("extra", Collections.emptyList()), Component::children)
                        .build(res -> SerializeResult.success(new ImmutableComponent(res.get(0), res.get(1), res.get(2))))
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
