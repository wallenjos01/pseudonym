package org.wallentines.pseudonym;

import java.util.function.Function;

public interface ParameterTransformer<T> {

    T transform(UnresolvedMessage<String> parameter);

    default <O> ParameterTransformer<O> then(Function<T, O> transformer) {
        return parameter -> transformer.apply(ParameterTransformer.this.transform(parameter));
    }

    ParameterTransformer<UnresolvedMessage<String>> IDENTITY = parameter -> parameter;
    ParameterTransformer<String> RESOLVE_EARLY = parameter -> UnresolvedMessage.resolve(parameter, new PipelineContext());

}
