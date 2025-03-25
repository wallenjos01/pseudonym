package org.wallentines.plib.mc;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import org.wallentines.plib.MessagePipeline;
import org.wallentines.plib.PipelineContext;
import org.wallentines.plib.UnresolvedMessage;

public class ComponentFlattener implements MessagePipeline.PipelineStage<UnresolvedMessage<String>, UnresolvedMessage<String>> {

    public static String flatten(Component component) {

        StringBuilder builder = new StringBuilder();
        if(component.getContents() instanceof PlainTextContents text) {
            builder.append(text.text());
        }

        for(Component c : component.getSiblings()) {
            builder.append(flatten(c));
        }

        return builder.toString();
    }

    @Override
    public UnresolvedMessage<String> apply(UnresolvedMessage<String> message, PipelineContext ctx) {
        return message.mapPlaceholders(Component.class, String.class, ComponentFlattener::flatten);
    }
}
