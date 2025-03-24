package org.wallentines.plib.mc;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.wallentines.mdcfg.Either;
import org.wallentines.plib.*;

import java.util.List;
import java.util.Optional;

public record ComponentResolver(ConfigTextParser parser) implements MessagePipeline.PipelineStage<UnresolvedMessage<String>, Component> {

    @Override
    @SuppressWarnings("unchecked")
    public Component apply(UnresolvedMessage<String> message, PlaceholderContext ctx) {

        MutableComponent out = null;
        MutableComponent lastLiteral = null;
        for(Either<String, PlaceholderInstance<?, ?>> part : message.parts()) {

            if(part.hasLeft()) {

                List<Component> parsed = parser.parseSplit(part.leftOrThrow());
                if(parsed.isEmpty()) continue;

                Component first = parsed.getFirst();
                if(out == null) {
                    out = (MutableComponent) first;
                } else if(!first.equals(Component.empty())) {
                    lastLiteral.append(first);
                }
                lastLiteral = (MutableComponent) first;

                // Component was reset
                if(parsed.size() > 1) {

                    if(out.getStyle() != Style.EMPTY) {
                        out = Component.empty().append(out);
                        lastLiteral = out;
                    }

                    for(int i = 1 ; i < parsed.size() ; i++) {
                        Component cmp = parsed.get(i);
                        if(!cmp.equals(Component.empty())) {
                            out.append(cmp);
                            lastLiteral = (MutableComponent) cmp;
                        }
                    }

                }

            } else if(part.rightOrThrow().parent().canResolve(Component.class)) {

                PlaceholderInstance<Component, ?> inst = (PlaceholderInstance<Component, ?>) part.rightOrThrow();
                Optional<Component> pl = resolve(ctx, inst);

                if(pl.isPresent()) {
                    if(lastLiteral == null) {
                        lastLiteral = Component.empty();
                        out = lastLiteral;
                    }
                    lastLiteral.append(pl.get());
                }
            }
        }

        return out;
    }

    private <T> Optional<Component> resolve(PlaceholderContext ctx, PlaceholderInstance<Component, T> cmp) {
        return cmp.parent().resolve(new ResolveContext<>(ctx, cmp));
    }

}
