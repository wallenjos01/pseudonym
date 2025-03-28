package org.wallentines.pseudonym;

import org.wallentines.mdcfg.Either;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A specialized combination of a PlaceholderResolver, A PlaceholderStripper and a MessageJoiner.
 * This class is highly specialized for use with hierarchical-style messages which are parsed inline. (i.e. legacy-text
 * parsing of Minecraft text components.) This class is probably not very useful in other circumstances.
 * @param <T> The message type
 */
public class HierarchicalAppenderResolver<T> implements MessagePipeline.PipelineStage<UnresolvedMessage<List<T>>, T> {

    private final Class<T> clazz;
    private final Appender<T> appender;

    public HierarchicalAppenderResolver(Class<T> clazz, Appender<T> appender) {
        this.clazz = clazz;
        this.appender = appender;
    }

    private T getLastChild(T message) {
        List<T> children = this.appender.children(message);
        if(!children.isEmpty()) {
            for (int i = children.size() - 1; i >= 0; i--) {
                T child = children.get(i);
                if (appender.influencesChildren(child)) {
                    return getLastChild(child);
                }
            }
        }

        return message;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T apply(UnresolvedMessage<List<T>> message, PipelineContext context) {

        PipelineContext ctx = message.context().and(context);
        Map<PlaceholderManager, PipelineContext> contexts = new HashMap<>();

        T out = null;
        T appendTo = null;
        for(Either<List<T>, PlaceholderInstance<?, ?>> part : message.parts()) {

            if(part.hasLeft()) {

                List<T> parsed = part.leftOrThrow();
                if(parsed.isEmpty()) continue;

                T first = parsed.getFirst();
                if(out == null) {
                    out = first;
                    appendTo = getLastChild(first);
                } else if(!first.equals(appender.empty())) {
                    appendTo = getLastChild(appender.append(appendTo, first));
                }

                // Back to the top of the hierarchy
                if(parsed.size() > 1) {

                    if(!out.equals(appender.empty())) {
                        T newEmpty = appender.empty();
                        appender.append(newEmpty, out);
                        out = newEmpty;
                    }

                    for(int i = 1 ; i < parsed.size() ; i++) {
                        T next = parsed.get(i);
                        if(next.equals(appender.empty())) {
                            appendTo = out;
                        } else {
                            appendTo = getLastChild(appender.append(out, next));
                        }
                    }

                }

            } else if(part.rightOrThrow().parent().canResolve(clazz, ctx)) {

                PlaceholderInstance<T, ?> inst = (PlaceholderInstance<T, ?>) part.rightOrThrow();
                PipelineContext finalContext = contexts.computeIfAbsent(inst.holder(), man -> ctx.and(man.getContext()));

                Optional<T> pl = resolve(finalContext, inst);

                if(pl.isPresent()) {
                    if(appendTo == null) {
                        appendTo = appender.empty();
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

        List<T> children(T message);

        T empty();

        boolean influencesChildren(T message);

    }

}
