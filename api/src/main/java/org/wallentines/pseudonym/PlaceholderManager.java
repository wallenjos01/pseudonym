package org.wallentines.pseudonym;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class PlaceholderManager {

    // Starts with a lowercase letter, only contains lowercase letters, numbers, or underscores
    public static final Pattern VALID_PLACEHOLDER = Pattern.compile("[a-z][a-z0-9_]*");

    private final Map<String, Placeholder<?, ?>> placeholders = new HashMap<>();

    public Placeholder<?, ?> get(String name) {
        return placeholders.get(name);
    }

    public void register(Placeholder<?, ?> placeholder) {
        placeholders.put(placeholder.name(), placeholder);
    }

}
