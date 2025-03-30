package org.wallentines.pseudonym.mc.api;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.wallentines.pseudonym.*;
import org.wallentines.pseudonym.mc.impl.ServerPlaceholdersImpl;

public interface ServerPlaceholders {

    MessagePipeline<PartialMessage<String>, Component> COMPONENT_RESOLVER =
            MessagePipeline.<PartialMessage<String>>builder()
                    .add(PlaceholderResolver.STRING)
                    .add(MessageJoiner.STRING_PARTIAL)
                    .add(new SplitComponentParser(ConfigTextParser.INSTANCE))
                    .add(new HierarchicalAppenderResolver<>(Component.class, ServerPlaceholdersImpl.APPENDER))
                    .build();

    MessagePipeline<PartialMessage<Component>, Component> PARTIAL_RESOLVER =
            MessagePipeline.<PartialMessage<Component>>builder()
                    .add(new PlaceholderResolver<>(Component.class))
                    .add(new PlaceholderStripper<>())
                    .add(new ComponentJoiner())
                    .build();


    /**
     * A placeholder manager which is valid for the lifetime of the process.
     * @return The global placeholder manager.
     */
    static PlaceholderManager getGlobalPlaceholders() {
        return ServerPlaceholdersImpl.getGlobalPlaceholders();
    }

    /**
     * Gets a placeholder manager which is valid for the lifetime of the given server. This inherits from the global placeholder manager.
     * @return A server placeholder manager.
     */
    static PlaceholderManager getServerPlaceholders(MinecraftServer server) {
        return ServerPlaceholdersImpl.getServerPlaceholders(server);
    }

}
