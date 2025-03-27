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

        List<Object> inValues = new ArrayList<>(values);
        Map<String, Placeholder<?, ?>> inPlaceholders = new HashMap<>();
        for(Object value : values) {
            if(value instanceof Placeholder<?, ?> pl) {
                inPlaceholders.put(pl.name(), pl);
            } else {
                inValues.add(value);
            }
        }

        this.values = List.copyOf(inValues);
        this.valuesByClass = values.stream().collect(Collectors.groupingBy(Object::getClass));
        this.contextPlaceholders = Map.copyOf(inPlaceholders);
    }

    public PipelineContext(List<Object> values, Map<String, Placeholder<?, ?>> contextPlaceholders) {
        this.values = values;
        this.valuesByClass = values.stream().collect(Collectors.groupingBy(Object::getClass));
        this.contextPlaceholders = contextPlaceholders;
    }

    public List<Object> values() {
        return values;
    }

    @SuppressWarnings("unchecked")
    public <T> Stream<T> getByClass(Class<T> clazz) {
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
        return new PipelineContext(
                Stream.concat(values.stream(), other.values.stream()).toList(),
                Stream.concat(contextPlaceholders.entrySet().stream(), other.contextPlaceholders.entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public PipelineContext and(Stream<PipelineContext> other) {
        if(other == EMPTY) return this;
        if(this == EMPTY) return this;
        if(this == other) return this;

        List<PipelineContext> contexts = other.toList();
        return new PipelineContext(
                Stream.concat(values.stream(), contexts.stream().flatMap(c -> c.values().stream())).toList(),
                Stream.concat(contextPlaceholders.entrySet().stream(), contexts.stream().flatMap(c -> c.contextPlaceholders.entrySet().stream()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
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
        out.addAll(List.of(values));
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

        public <T> Builder withContextPlaceholder(String name, Class<T> clazz, T value) {
            contextPlaceholders.put(name, Placeholder.of(name, clazz, PlaceholderSupplier.of(value)));
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

}
