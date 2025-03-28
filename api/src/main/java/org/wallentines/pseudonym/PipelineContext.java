package org.wallentines.pseudonym;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PipelineContext {

    private final List<Object> values;
    private final Map<Class<?>, List<Object>> valuesByClass;

    private final Map<String, Placeholder<?, ?>> contextPlaceholders;

    public static final PipelineContext EMPTY = new PipelineContext();

    public PipelineContext() {
        this.values = Collections.emptyList();
        this.valuesByClass = Collections.emptyMap();
        this.contextPlaceholders = Collections.emptyMap();
    }

    public PipelineContext(List<Object> values) {

        List<Object> inValues = new ArrayList<>();
        Map<String, Placeholder<?, ?>> inPlaceholders = new HashMap<>();
        Map<Class<?>, List<Object>> inValuesByClass = new HashMap<>();
        for(Object value : values) {
            if(value instanceof Placeholder<?, ?> pl) {
                inPlaceholders.put(pl.name(), pl);
            } else {
                inValues.add(value);
                addWithSuperclasses(inValuesByClass, value.getClass(), value);
            }
        }

        this.values = List.copyOf(inValues);
        this.valuesByClass = Map.copyOf(inValuesByClass);
        this.contextPlaceholders = Map.copyOf(inPlaceholders);
    }

    public PipelineContext(List<Object> values, Map<String, Placeholder<?, ?>> contextPlaceholders) {

        Map<Class<?>, List<Object>> inValuesByClass = new HashMap<>();
        for(Object value : values) {
            addWithSuperclasses(inValuesByClass, value.getClass(), value);
        }

        this.values = values;
        this.valuesByClass = Map.copyOf(inValuesByClass);
        this.contextPlaceholders = contextPlaceholders;
    }

    private PipelineContext(List<Object> values, Map<String, Placeholder<?, ?>> contextPlaceholders, Map<Class<?>, List<Object>> valuesByClass) {
        this.values = values;
        this.valuesByClass = valuesByClass;
        this.contextPlaceholders = contextPlaceholders;
    }

    public List<Object> values() {
        return values;
    }

    @SuppressWarnings("unchecked")
    public <T> Stream<T> getByClass(Class<T> clazz) {
        if(clazz == Object.class) return (Stream<T>) values().stream();

        List<Object> values = valuesByClass.get(clazz);
        if (values == null) {
            return Stream.empty();
        }
        return (Stream<T>) values.stream();
    }

    public <T> Optional<T> getFirst(Class<T> clazz) {
        return getByClass(clazz).findFirst();
    }

    public Optional<Placeholder<?, ?>> getContextPlaceholder(String name) {
        return Optional.ofNullable(contextPlaceholders.get(name));
    }

    public PipelineContext and(PipelineContext other) {
        if(other == EMPTY) return this;
        if(this == EMPTY) return other;
        if(this == other) return this;

        Map<Class<?>, List<Object>> valuesByClass = new HashMap<>(this.valuesByClass);
        joinClassCache(valuesByClass, other);

        return new PipelineContext(
                Stream.concat(values.stream(), other.values.stream()).toList(),
                Stream.concat(contextPlaceholders.entrySet().stream(), other.contextPlaceholders.entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                valuesByClass);
    }

    public PipelineContext and(Stream<PipelineContext> other) {
        if(other == EMPTY) return this;
        if(this == EMPTY) return this;
        if(this == other) return this;

        List<PipelineContext> contexts = other.toList();

        Map<Class<?>, List<Object>> valuesByClass = new HashMap<>(this.valuesByClass);
        for(PipelineContext context : contexts) {
            joinClassCache(valuesByClass, context);
        }

        return new PipelineContext(
                Stream.concat(values.stream(), contexts.stream().flatMap(c -> c.values().stream())).toList(),
                Stream.concat(contextPlaceholders.entrySet().stream(), contexts.stream().flatMap(c -> c.contextPlaceholders.entrySet().stream()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private void joinClassCache(Map<Class<?>, List<Object>> valuesByClass, PipelineContext context) {
        for(Class<?> c : context.valuesByClass.keySet()) {
            valuesByClass.compute(c, (k,v) -> {
                if(v == null) v = new ArrayList<>();
                v.addAll(context.valuesByClass.get(c));
                return v;
            });
        }
    }


    public static PipelineContext of(Object... values) {
        if(values == null || values.length == 0) return EMPTY;
        return new PipelineContext(Arrays.stream(values).filter(Objects::nonNull).toList());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Object... values) {
        Builder out = new Builder();
        if(values != null && values.length > 0) {
            out.addAll(Arrays.stream(values).filter(Objects::nonNull).toList());
        }
        return out;
    }

    public static class Builder {

        private final List<Object> values = new ArrayList<>();
        private final Map<String, Placeholder<?, ?>> contextPlaceholders = new HashMap<>();

        public Builder add(Object... values) {
            if(values.length == 1) {
                this.values.add(values[0]);
            } else {
                this.values.addAll(Arrays.asList(values));
            }
            return this;
        }

        public Builder addAll(List<Object> values) {
            if(values != null && !values.isEmpty()) {
                this.values.addAll(values);
            }
            return this;
        }

        public Builder withContextPlaceholder(String name, String value) {
            contextPlaceholders.put(name, Placeholder.of(name, String.class, PlaceholderSupplier.of(value)));
            return this;
        }

        public Builder withContextPlaceholder(String name, PlaceholderSupplier<String, Void> value) {
            contextPlaceholders.put(name, Placeholder.of(name, String.class, value));
            return this;
        }

        public <T> Builder withContextPlaceholder(String name, Class<T> clazz, T value) {
            contextPlaceholders.put(name, Placeholder.of(name, clazz, PlaceholderSupplier.of(value)));
            return this;
        }

        public <T> Builder withContextPlaceholder(String name, Class<T> clazz, PlaceholderSupplier<T, Void> value) {
            contextPlaceholders.put(name, Placeholder.of(name, clazz, value));
            return this;
        }

        public Builder withContextPlaceholder(Placeholder<?, ?> placeholder) {
            contextPlaceholders.put(placeholder.name(), placeholder);
            return this;
        }

        public PipelineContext build() {
            return new PipelineContext(values, contextPlaceholders);
        }

    }

    private void addWithSuperclasses(Map<Class<?>, List<Object>> map, Class<?> clazz, Object value) {

        if(clazz == Object.class) return;
        map.computeIfAbsent(clazz, k -> new ArrayList<>()).add(value);

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            addWithSuperclasses(map, superclass, value);
        }
        for(Class<?> interfaceClass : clazz.getInterfaces()) {
            addWithSuperclasses(map, interfaceClass, value);
        }
    }

}
