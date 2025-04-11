package org.wallentines.pseudonym.lang;


import org.wallentines.mdcfg.codec.Codec;
import org.wallentines.mdcfg.serializer.*;
import org.wallentines.pseudonym.MessagePipeline;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public record LangRegistry<P>(Map<String, P> registry) {

    @SuppressWarnings("rawtypes")
    private static final LangRegistry EMPTY = new LangRegistry<>(Collections.emptyMap());

    @SuppressWarnings("unchecked")
    public static <P> LangRegistry<P> empty() { return (LangRegistry<P>) EMPTY; }


    public static <P> Builder<P> builder(MessagePipeline<String, P> parser) {
        return new Builder<>(parser);
    }

    public static class Builder<P> {

        private final Map<String, P> map = new HashMap<>();
        private final MessagePipeline<String, P> parser;

        public Builder(MessagePipeline<String, P> parser) {
            this.parser = parser;
        }

        public Builder<P> add(String key, P value) {
            map.put(key, value);
            return this;
        }

        public Builder<P> add(String key, String language) {
            return add(key, parser.accept(language));
        }

        public LangRegistry<P> build() {
            return new LangRegistry<>(Map.copyOf(map));
        }

    }

    public static <P> SerializeResult<LangRegistry<P>> fromStream(InputStream is, Codec codec, MessagePipeline<String, P> parser) throws IOException {
        return serializer(parser).deserialize(ConfigContext.INSTANCE, codec.decode(ConfigContext.INSTANCE, is));
    }

    public static <P> BackSerializer<LangRegistry<P>> serializer(MessagePipeline<String, P> parser) {

        return new BackSerializer<>() {
            @Override
            public <O> SerializeResult<LangRegistry<P>> deserialize(SerializeContext<O> context, O value) {

                return context.asMap(value).map(map -> {

                    Map<String, P> out = new HashMap<>();
                    for (Map.Entry<String, O> ent : map.entrySet()) {
                        SerializeResult<String> val = context.asString(ent.getValue());
                        if (!val.isComplete()) {
                            return SerializeResult.failure(val.getError());
                        }

                        out.put(ent.getKey(), parser.accept(val.getOrThrow()));
                    }

                    return SerializeResult.success(new LangRegistry<>(Map.copyOf(out)));
                });
            }
        };
    }

}
