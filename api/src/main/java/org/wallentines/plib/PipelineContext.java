package org.wallentines.plib;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PipelineContext {

    private final List<Object> values;
    private final Map<Class<?>, List<Object>> valuesByClass;

    public static final PipelineContext EMPTY = new PipelineContext();

    public PipelineContext() {
        this.values = Collections.emptyList();
        this.valuesByClass = Collections.emptyMap();
    }

    public PipelineContext(List<Object> values) {
        this.values = values;
        this.valuesByClass = values.stream().collect(Collectors.groupingBy(Object::getClass));
    }

    public List<Object> values() {
        return values;
    }

    @SuppressWarnings("unchecked")
    public <T> Stream<T> getByClass(Class<T> clazz) {
        return (Stream<T>) valuesByClass.get(clazz).stream();
    }

    public <T> Optional<T> getFirst(Class<T> clazz) {
        return getByClass(clazz).findFirst();
    }

    public PipelineContext and(PipelineContext other) {
        if(other == EMPTY) return this;
        if(this == EMPTY) return other;
        if(this == other) return this;
        return new PipelineContext(Stream.concat(values.stream(), other.values.stream()).toList());
    }


    public static PipelineContext of(Object... values) {
        return new PipelineContext(List.of(values));
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

        public PipelineContext build() {
            return new PipelineContext(values);
        }

    }

}
