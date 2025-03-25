package org.wallentines.pseudonym.lang;

import org.wallentines.pseudonym.UnresolvedMessage;

import java.util.Collections;
import java.util.Map;

public record LangRegistry(Map<String, UnresolvedMessage<String>> registry) {

    public static LangRegistry EMPTY = new LangRegistry(Collections.emptyMap());

}
