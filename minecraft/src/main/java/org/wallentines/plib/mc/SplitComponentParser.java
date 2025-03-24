package org.wallentines.plib.mc;

import net.minecraft.network.chat.Component;
import org.wallentines.plib.MessageConverter;
import org.wallentines.plib.PipelineContext;
import org.wallentines.plib.UnresolvedMessage;

import java.util.List;

public class SplitComponentParser implements MessageConverter<String, List<Component>> {

    private final ConfigTextParser parser;

    public SplitComponentParser(ConfigTextParser parser) {
        this.parser = parser;
    }

    @Override
    public UnresolvedMessage<List<Component>> apply(UnresolvedMessage<String> message, PipelineContext ctx) {
        return message.map(parser::parseSplit);
    }
}
