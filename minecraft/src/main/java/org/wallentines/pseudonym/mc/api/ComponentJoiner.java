package org.wallentines.pseudonym.mc.api;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.wallentines.pseudonym.MessagePipeline;
import org.wallentines.pseudonym.PipelineContext;

import java.util.List;

public class ComponentJoiner implements MessagePipeline.PipelineStage<List<Component>, Component> {
    @Override
    public Component apply(List<Component> message, PipelineContext ctx) {

        if(message.isEmpty()) return Component.empty();
        MutableComponent out = message.getFirst().copy();
        for(int i = 1; i < message.size(); i++) {
            out.append(message.get(i));
        }

        return out;
    }
}
