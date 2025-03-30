package org.wallentines.pseudonym.mc.api;

import net.minecraft.network.chat.Component;
import org.wallentines.pseudonym.MessageConverter;
import org.wallentines.pseudonym.PipelineContext;
import org.wallentines.pseudonym.PartialMessage;

import java.util.List;

public class SplitComponentParser implements MessageConverter<String, List<Component>> {

    private final ConfigTextParser parser;

    public SplitComponentParser(ConfigTextParser parser) {
        this.parser = parser;
    }

    @Override
    public PartialMessage<List<Component>> apply(PartialMessage<String> message, PipelineContext ctx) {
        return message.map(parser::parseSplit);
    }
}
