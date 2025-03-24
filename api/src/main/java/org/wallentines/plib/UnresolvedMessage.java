package org.wallentines.plib;

import org.wallentines.mdcfg.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record UnresolvedMessage<T>(List<Either<T, PlaceholderInstance<?, ?>>> parts) {

    public static String resolve(UnresolvedMessage<String> msg, PlaceholderContext ctx) {
        return MessagePipeline.RESOLVE_STRING.accept(msg, ctx);
    }

    public <O> UnresolvedMessage<O> map(Function<T, O> mapper) {
        List<Either<O, PlaceholderInstance<?, ?>>> out = new ArrayList<>();
        for(Either<T, PlaceholderInstance<?, ?>> part : parts()) {
            if(part.hasLeft()) {
                out.add(Either.left(mapper.apply(part.leftOrThrow())));
            } else {
                out.add(Either.right(part.rightOrThrow()));
            }
        }
        return new UnresolvedMessage<>(List.copyOf(out));
    }


}
