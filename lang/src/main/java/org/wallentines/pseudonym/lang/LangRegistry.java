package org.wallentines.pseudonym.lang;


import org.wallentines.pseudonym.MessagePipeline;

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

}
