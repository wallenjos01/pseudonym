package org.wallentines.plib;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public record Placeholder<T, P>(String name, Class<T> clazz, PlaceholderSupplier<T, P> supplier, @Nullable ParameterTransformer<P> transformer) {

    static <T> Placeholder<T, Void> of(String name, Class<T> clazz, PlaceholderSupplier<T, Void> supplier) {
        return new Placeholder<>(name, clazz, supplier, null);
    }

    public boolean canResolve(Class<?> other) {
        return clazz.isAssignableFrom(other);
    }

    public boolean acceptsParameter() {
        return transformer != null;
    }

    public PlaceholderInstance<T, P> instantiate(UnresolvedMessage<String> parameter) {
        P param = null;
        if(transformer != null) {
            param = transformer.transform(parameter);
        }
        return new PlaceholderInstance<>(this, param);
    }

    public Optional<T> resolve(ResolveContext<P> context) {
        return supplier.get(context);
    }

    public <O> Placeholder<O, P> map(Class<O> clazz, Function<T, O> mapper) {
        return new Placeholder<>(name, clazz, supplier.map(mapper), transformer);
    }

}
