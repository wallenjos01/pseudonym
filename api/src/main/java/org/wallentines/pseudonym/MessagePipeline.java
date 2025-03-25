package org.wallentines.pseudonym;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MessagePipeline<I, O> {

    private final List<PipelineStage<?, ?>> stages;

    private MessagePipeline(List<PipelineStage<?, ?>> stages) {
        this.stages = stages;
    }

    public static final MessagePipeline<UnresolvedMessage<String>, String> RESOLVE_STRING = MessagePipeline.<UnresolvedMessage<String>>builder()
            .add(PlaceholderResolver.STRING)
            .add(new PlaceholderStripper<>())
            .add(MessageJoiner.STRING)
            .build();

    public static MessagePipeline<String, UnresolvedMessage<String>> parser(PlaceholderManager manager) {
        return MessagePipeline.<String>builder()
                .add(new PlaceholderParser(manager))
                .build();
    }


    public O accept(I message) {
        return accept(message, PipelineContext.EMPTY);
    }

    @SuppressWarnings("unchecked")
    public O accept(I message, PipelineContext ctx) {
        Object obj = message;
        for(PipelineStage<?, ?> stage : stages) {
            obj = apply(stage, obj, ctx);
        }
        return (O) obj;
    }

    @SuppressWarnings("unchecked")
    private <T> Object apply(PipelineStage<T, ?> stage, Object message, PipelineContext ctx) {
        return stage.apply((T) message, ctx);
    }

    public static <T> Builder<T, T> builder() {
        return new Builder<>();
    }

    public interface PipelineStage<I, O> {
        O apply(I message, PipelineContext ctx);
    }

    public static class Builder<I, O> {

        private final List<PipelineStage<?, ?>> stages = new ArrayList<>();

        @SuppressWarnings("unchecked")
        public <T> Builder<I, T> add(PipelineStage<? super O, ? extends T> stage) {
            stages.add(stage);
            return (Builder<I, T>) this;
        }

        public <T> Builder<I, T> add(Function<? super O, ? extends T> stage) {
            return add((message, ctx) -> stage.apply(message));
        }

        @SuppressWarnings("unchecked")
        public <T> Builder<I, T> add(MessagePipeline<? extends O, ? super T> pipeline) {
            stages.addAll(pipeline.stages);
            return (Builder<I, T>) this;
        }

        public MessagePipeline<I, O> build() {
            return new MessagePipeline<>(stages);
        }
    }

}
