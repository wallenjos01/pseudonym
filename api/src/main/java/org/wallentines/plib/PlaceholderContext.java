package org.wallentines.plib;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlaceholderContext {

    private final List<Object> values;
    private final Map<Class<?>, List<Object>> valuesByClass;

    public PlaceholderContext() {
        this.values = Collections.emptyList();
        this.valuesByClass = Collections.emptyMap();
    }

    public PlaceholderContext(List<Object> values) {
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

    public PlaceholderContext and(PlaceholderContext other) {
        return new PlaceholderContext(Stream.concat(values.stream(), other.values.stream()).toList());
    }


    public static PlaceholderContext of(Object... values) {
        return new PlaceholderContext(List.of(values));
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

        public PlaceholderContext build() {
            return new PlaceholderContext(values);
        }

    }

}
