package org.wallentines.pseudonym.lang;


import java.util.Collections;
import java.util.Map;

public record LangRegistry<P>(Map<String, P> registry) {

    @SuppressWarnings("rawtypes")
    private static final LangRegistry EMPTY = new LangRegistry<>(Collections.emptyMap());

    @SuppressWarnings("unchecked")
    public static <P> LangRegistry<P> empty() { return (LangRegistry<P>) EMPTY; }

}
