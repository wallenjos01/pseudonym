package org.wallentines.pseudonym.mc.api;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import org.wallentines.pseudonym.MessagePipeline;
import org.wallentines.pseudonym.PipelineContext;
import org.wallentines.pseudonym.UnresolvedMessage;

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
