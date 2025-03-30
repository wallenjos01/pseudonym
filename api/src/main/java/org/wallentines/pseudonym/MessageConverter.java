package org.wallentines.pseudonym;


public interface MessageConverter<T, O> extends MessagePipeline.PipelineStage<PartialMessage<T>, PartialMessage<O>> { }
