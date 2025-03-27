package org.wallentines.pseudonym;

import org.wallentines.mdcfg.Either;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PlaceholderResolver<T>(Class<T> clazz) implements MessagePipeline.PipelineStage<UnresolvedMessage<T>, UnresolvedMessage<T>> {

    @Override
    @SuppressWarnings("unchecked")
    public UnresolvedMessage<T> apply(UnresolvedMessage<T> message, PipelineContext context) {
        PipelineContext ctx = message.context().and(context);
        Map<PlaceholderManager, PipelineContext> contexts = new HashMap<>();
        List<Either<T, PlaceholderInstance<?, ?>>> out = new ArrayList<>();
        for(Either<T, PlaceholderInstance<?, ?>> e : message.parts()) {
            if(e.hasLeft()) {
                out.add(e);
            } else {

                PlaceholderInstance<?, ?> pl = e.rightOrThrow();
                PipelineContext finalContext = contexts.computeIfAbsent(pl.holder(), man -> ctx.and(man.getContext()));

                if(pl.parent().type() == Void.class) { // Unknown placeholder. Check context
                    ctx.getContextPlaceholder(pl.parent().name())
                            .filter(cpl -> cpl.canResolve(clazz, ctx))
                            .flatMap(cpl -> ((Placeholder<T, Void>) cpl).resolve(new ResolveContext<>(finalContext, null)))
                            .ifPresent(t -> out.add(Either.left(t))
                );

                } else if(pl.parent().canResolve(clazz, ctx)) {
                    PlaceholderInstance<T, ?> inst = (PlaceholderInstance<T, ?>) e.rightOrThrow();
                    inst.resolve(ctx).ifPresent(t -> out.add(Either.left(t)));
                } else {
                    out.add(e);
                }
            }
        }
        return new UnresolvedMessage<>(List.copyOf(out) );
    }

    public static final PlaceholderResolver<String> STRING = new PlaceholderResolver<>(String.class);

}
