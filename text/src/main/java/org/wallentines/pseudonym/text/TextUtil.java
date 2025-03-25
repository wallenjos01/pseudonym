package org.wallentines.pseudonym.text;

import org.wallentines.pseudonym.*;

public class TextUtil {

    public static final MessagePipeline<UnresolvedMessage<String>, Component> COMPONENT_RESOLVER =
            MessagePipeline.<UnresolvedMessage<String>>builder()
                    .add(PlaceholderResolver.STRING)
                    .add(MessageJoiner.STRING_PARTIAL)
                    .add(new SplitComponentParser(ConfigTextParser.INSTANCE))
                    .add(new HierarchicalAppenderResolver<>(
                            Component.class, (c1, c2) -> ((MutableComponent) c1).append(c2), Component::empty))
                    .build();

}
