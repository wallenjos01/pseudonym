package org.wallentines.plib;

import org.wallentines.mdcfg.Either;

import java.util.ArrayList;
import java.util.List;

public interface MessageJoiner<T> extends MessagePipeline.PipelineStage<List<T>, T> {

    MessageJoiner<String> STRING = (message, ctx) -> String.join("", message);

    MessageConverter<String, String> STRING_PARTIAL = (message, ctx) -> {

        List<Either<String, PlaceholderInstance<?, ?>>> out = new ArrayList<>();
        StringBuilder currentString = new StringBuilder();

        for(Either<String, PlaceholderInstance<?, ?>> part : message.parts()) {
            if(part.hasLeft()) {
                currentString.append(part.leftOrThrow());
            } else {
                if(!currentString.isEmpty()) {
                    out.add(Either.left(currentString.toString()));
                    currentString.setLength(0);
                }
                out.add(Either.right(part.rightOrThrow()));
            }
        }
        if(!currentString.isEmpty()) {
            out.add(Either.left(currentString.toString()));
        }

        return new UnresolvedMessage<>(List.copyOf(out));
    };

}
