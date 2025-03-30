package org.wallentines.pseudonym;

import org.wallentines.mdcfg.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record PartialMessage<T>(List<Either<T, PlaceholderInstance<?, ?>>> parts, PipelineContext context) {

    public PartialMessage(List<Either<T, PlaceholderInstance<?, ?>>> parts) {
        this(parts, PipelineContext.EMPTY);
    }

    public static String resolve(PartialMessage<String> msg, PipelineContext ctx) {
        return MessagePipeline.RESOLVE_STRING.accept(msg, msg.context.and(ctx));
    }

    public <O> PartialMessage<O> map(Function<T, O> mapper) {
        List<Either<O, PlaceholderInstance<?, ?>>> out = new ArrayList<>();
        for(Either<T, PlaceholderInstance<?, ?>> part : parts()) {
            if(part.hasLeft()) {
                out.add(Either.left(mapper.apply(part.leftOrThrow())));
            } else {
                out.add(Either.right(part.rightOrThrow()));
            }
        }
        return new PartialMessage<>(List.copyOf(out), context);
    }

    @SuppressWarnings("unchecked")
    public <C, O> PartialMessage<T> mapPlaceholders(Class<C> clazz, Class<O> toClazz, Function<C, O> mapper) {
        List<Either<T, PlaceholderInstance<?, ?>>> out = new ArrayList<>();
        for(Either<T, PlaceholderInstance<?, ?>> part : parts()) {
            if(part.hasLeft()) {
                out.add(Either.left(part.leftOrThrow()));
            } else if(part.rightOrThrow().parent().canResolve(clazz, new PipelineContext())) {

                PlaceholderInstance<C, ?> p = (PlaceholderInstance<C, ?>) part.rightOrThrow();
                out.add(Either.right(mapPlaceholder(toClazz, mapper, p)));
            } else {
                out.add(Either.right(part.rightOrThrow()));
            }
        }
        return new PartialMessage<>(List.copyOf(out));
    }

    private <T1, T2> PlaceholderInstance<T2, ?> mapPlaceholder(Class<T2> clazz, Function<T1, T2> mapper, PlaceholderInstance<T1, ?> msg) {
        return msg.map(clazz, mapper);
    }

}
