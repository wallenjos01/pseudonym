package org.wallentines.pseudonym.text;

import org.wallentines.pseudonym.MessageConverter;
import org.wallentines.pseudonym.PipelineContext;
import org.wallentines.pseudonym.PartialMessage;

public class ComponentParser implements MessageConverter<String, Component> {

    private final ConfigTextParser parser;

    public ComponentParser(ConfigTextParser parser) {
        this.parser = parser;
    }

    @Override
    public PartialMessage<Component> apply(PartialMessage<String> message, PipelineContext ctx) {
        return message.map(parser::parse);
    }
}
