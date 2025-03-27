package org.wallentines.pseudonym.mc.api;

import net.minecraft.network.chat.Component;
import org.wallentines.pseudonym.MessageConverter;
import org.wallentines.pseudonym.PipelineContext;
import org.wallentines.pseudonym.UnresolvedMessage;
import org.wallentines.pseudonym.mc.impl.ConfigTextParserImpl;

public class ComponentParser implements MessageConverter<String, Component> {

    private final ConfigTextParserImpl parser;

    public ComponentParser(ConfigTextParserImpl parser) {
        this.parser = parser;
    }

    @Override
    public UnresolvedMessage<Component> apply(UnresolvedMessage<String> message, PipelineContext ctx) {
        return message.map(parser::parse);
    }
}
