package org.wallentines.pseudonym;

import java.util.*;
import java.util.regex.Pattern;

public class PlaceholderManager {

    // Starts with a lowercase letter, only contains lowercase letters, numbers, or underscores
    public static final Pattern VALID_PLACEHOLDER = Pattern.compile("[a-z][a-z0-9_]*");

    private final Map<String, Placeholder<?, ?>> placeholders = new HashMap<>();
    private final PipelineContext context;
    private final List<PlaceholderManager> parents;

    public PlaceholderManager() {
        this(PipelineContext.EMPTY, Collections.emptyList());
    }

    public PlaceholderManager(PipelineContext context) {
        this(context, Collections.emptyList());
    }

    public PlaceholderManager(PipelineContext context, List<PlaceholderManager> parents) {
        this.parents = parents.stream().filter(this::findSelf).toList();
        this.context = context.and(this.parents.stream().map(PlaceholderManager::getContext));
    }

    public Placeholder<?, ?> get(String name) {
        Placeholder<?, ?> out = placeholders.get(name);
        int index = 0;
        while(out == null && index < parents.size()) {
            out = parents.get(index).get(name);
            index++;
        }
        return out;
    }

    public PipelineContext getContext() {
        return context;
    }

    public void register(Placeholder<?, ?> placeholder) {
        placeholders.put(placeholder.name(), placeholder);
    }

    public void unregister(String name) {
        placeholders.remove(name);
    }

    private boolean findSelf(PlaceholderManager other) {
        for(PlaceholderManager child : other.parents) {
            if(child == this || findSelf(child)) {
                return false;
            }
        }
        return true;
    }
}
