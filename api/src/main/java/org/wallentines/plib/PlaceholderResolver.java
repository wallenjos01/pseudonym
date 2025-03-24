package org.wallentines.plib;

import org.wallentines.mdcfg.Either;

import java.util.ArrayList;
import java.util.List;

public record PlaceholderResolver<T>(Class<T> clazz) implements MessagePipeline.PipelineStage<UnresolvedMessage<T>, UnresolvedMessage<T>> {

    @Override
    @SuppressWarnings("unchecked")
    public UnresolvedMessage<T> apply(UnresolvedMessage<T> message, PlaceholderContext ctx) {
        List<Either<T, PlaceholderInstance<?, ?>>> out = new ArrayList<>();
        for(Either<T, PlaceholderInstance<?, ?>> e : message.parts()) {
            if(e.hasLeft() || !e.rightOrThrow().parent().canResolve(clazz)) {
                out.add(e);
            } else {
                PlaceholderInstance<T, ?> inst = (PlaceholderInstance<T, ?>) e.rightOrThrow();
                inst.resolve(ctx).ifPresent(t -> out.add(Either.left(t)));
            }
        }
        return new UnresolvedMessage<>(List.copyOf(out) );
    }

    public static final PlaceholderResolver<String> STRING = new PlaceholderResolver<>(String.class);

}
