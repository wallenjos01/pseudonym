package org.wallentines.pseudonym;

public interface Message<T> {

    T get(PipelineContext context);

    default T get() {
        return get(PipelineContext.EMPTY);
    }

    static <T> Message<T> complete(T message) {
        return new Complete<>(message);
    }

    static <R,T> Message<T> forPipeline(R raw, MessagePipeline<R, T> pipeline) {
        return new Pipelined<>(raw, pipeline);
    }

    record Complete<T>(T message) implements Message<T> {
        @Override
        public T get(PipelineContext context) {
            return message;
        }
    }

    record Pipelined<R,T>(R raw, MessagePipeline<R,T> pipeline) implements Message<T> {
        @Override
        public T get(PipelineContext context) {
            return pipeline.accept(raw, context);
        }
    }

}
