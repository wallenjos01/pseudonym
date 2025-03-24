package org.wallentines.plib;

import java.util.Optional;
import java.util.function.Function;

public interface PlaceholderSupplier<T, P> {

    Optional<T> get(ResolveContext<P> context);

    default <O> PlaceholderSupplier<O, P> map(Function<T, O> mapper) {
        return context -> PlaceholderSupplier.this.get(context).map(mapper);
    }

    static <T> PlaceholderSupplier<T, Void> of(T value) {
        return context -> Optional.of(value);
    }

}
