package org.wallentines.pseudonym;

import java.util.function.Function;

public interface ParameterTransformer<T> {

    T transform(PartialMessage<String> parameter);

    default <O> ParameterTransformer<O> then(Function<T, O> transformer) {
        return parameter -> transformer.apply(ParameterTransformer.this.transform(parameter));
    }

    ParameterTransformer<PartialMessage<String>> IDENTITY = parameter -> parameter;
    ParameterTransformer<String> RESOLVE_EARLY = parameter -> PartialMessage.resolve(parameter, new PipelineContext());

}
