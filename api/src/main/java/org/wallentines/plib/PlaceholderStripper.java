package org.wallentines.plib;

import org.wallentines.mdcfg.Either;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderStripper<T> implements MessagePipeline.PipelineStage<UnresolvedMessage<T>, List<T>> {

    @Override
    public List<T> apply(UnresolvedMessage<T> message, PipelineContext ctx) {
        List<T> out = new ArrayList<>();
        for(Either<T, PlaceholderInstance<?, ?>> e : message.parts()) {
            if(e.hasLeft()) {
                out.add(e.leftOrThrow());
            }
        }
        return List.copyOf(out);
    }
}
