package org.wallentines.plib;

import java.util.Optional;
import java.util.function.Function;

public record PlaceholderInstance<T, P>(Placeholder<T, P> parent, P param) {

    public Optional<T> resolve(PipelineContext ctx) {
        return parent.resolve(new ResolveContext<>(ctx, param));
    }

    public <O> PlaceholderInstance<O, P> map(Class<O> clazz, Function<T, O> mapper) {

        return new PlaceholderInstance<>(parent.map(clazz, mapper), param);

    }


}
