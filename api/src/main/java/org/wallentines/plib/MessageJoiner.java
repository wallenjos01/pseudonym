package org.wallentines.plib;

import java.util.List;

public interface MessageJoiner<T> extends MessagePipeline.PipelineStage<List<T>, T> {

    MessageJoiner<String> STRING = (message, ctx) -> String.join("", message);

}
