package org.wallentines.pseudonym.text;

import org.wallentines.pseudonym.*;

import java.util.List;

public class TextUtil {

    private static final HierarchicalAppenderResolver.Appender<Component> APPENDER = new HierarchicalAppenderResolver.Appender<Component>() {
        @Override
        public Component append(Component to, Component value) {
            return ((MutableComponent) to).append(value);
        }

        @Override
        public List<Component> children(Component message) {
            return message.children();
        }

        @Override
        public Component empty() {
            return Component.empty();
        }

        @Override
        public Component copy(Component other) {
            return other.copy();
        }

        @Override
        public boolean influencesChildren(Component message) {
            return message.style() != Style.EMPTY || !message.children().isEmpty();
        }
    };

    public static final MessagePipeline<PartialMessage<String>, Component> COMPONENT_RESOLVER =
            MessagePipeline.<PartialMessage<String>>builder()
                    .add(PlaceholderResolver.STRING)
                    .add(MessageJoiner.STRING_PARTIAL)
                    .add(new SplitComponentParser(ConfigTextParser.INSTANCE))
                    .add(new HierarchicalAppenderResolver<>(Component.class, APPENDER))
                    .build();

}
