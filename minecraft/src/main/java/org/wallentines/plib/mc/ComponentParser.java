package org.wallentines.plib.mc;

import net.minecraft.network.chat.Component;
import org.wallentines.plib.MessageConverter;
import org.wallentines.plib.PipelineContext;
import org.wallentines.plib.UnresolvedMessage;

public class ComponentParser implements MessageConverter<String, Component> {

    private final ConfigTextParser parser;

    public ComponentParser(ConfigTextParser parser) {
        this.parser = parser;
    }

    @Override
    public UnresolvedMessage<Component> apply(UnresolvedMessage<String> message, PipelineContext ctx) {
        return message.map(parser::parse);
    }
}
