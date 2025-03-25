package org.wallentines.pseudonym;

import org.wallentines.mdcfg.Either;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A specialized combination of a PlaceholderResolver, A PlaceholderStripper and a MessageJoiner.
 * This class is highly specialized for use with hierarchical-style messages which are parsed inline. (i.e. legacy-text
 * parsing of Minecraft text components.) This class is probably not very useful in other circumstances.
 * @param <T> The message type
 */
public class HierarchicalAppenderResolver<T> implements MessagePipeline.PipelineStage<UnresolvedMessage<List<T>>, T> {

    private final Class<T> clazz;
    private final Appender<T> appender;
    private final Supplier<T> empty;

    public HierarchicalAppenderResolver(Class<T> clazz, Appender<T> appender, Supplier<T> empty) {
        this.clazz = clazz;
        this.appender = appender;
        this.empty = empty;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T apply(UnresolvedMessage<List<T>> message, PipelineContext ctx) {

        T out = null;
        T appendTo = null;
        for(Either<List<T>, PlaceholderInstance<?, ?>> part : message.parts()) {

            if(part.hasLeft()) {

                List<T> parsed = part.leftOrThrow();
                if(parsed.isEmpty()) continue;

                T first = parsed.getFirst();
                if(out == null) {
                    out = first;
                    appendTo = first;
                } else if(!first.equals(empty.get())) {
                    appendTo = appender.append(appendTo, first);
                }

                // Back to the top of the hierarchy
                if(parsed.size() > 1) {

                    if(!out.equals(empty.get())) {
                        out = appender.append(empty.get(), out);
                        appendTo = out;
                    }

                    for(int i = 1 ; i < parsed.size() ; i++) {

                        T next = parsed.get(i);
                        if(!next.equals(empty.get())) {
                            appendTo = appender.append(out, next);
                        }
                    }

                }

            } else if(part.rightOrThrow().parent().canResolve(clazz, ctx)) {

                PlaceholderInstance<T, ?> inst = (PlaceholderInstance<T, ?>) part.rightOrThrow();
                Optional<T> pl = resolve(ctx, inst);

                if(pl.isPresent()) {
                    if(appendTo == null) {
                        appendTo = empty.get();
                        out = appendTo;
                    }
                    appender.append(appendTo, pl.get());
                }
            }
        }

        return out;
    }

    private <O> Optional<T> resolve(PipelineContext ctx, PlaceholderInstance<T, O> cmp) {
        return cmp.parent().resolve(new ResolveContext<>(ctx, cmp.param()));
    }

    public interface Appender<T> {

        T append(T to, T value);
    }

}
