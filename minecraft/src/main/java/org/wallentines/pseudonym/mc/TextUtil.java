package org.wallentines.pseudonym.mc;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.wallentines.pseudonym.*;
import org.wallentines.pseudonym.lang.LangManager;
import org.wallentines.pseudonym.lang.LangProvider;
import org.wallentines.pseudonym.lang.LangRegistry;

public final class TextUtil {

    public static final MessagePipeline<UnresolvedMessage<String>, Component> COMPONENT_RESOLVER =
            MessagePipeline.<UnresolvedMessage<String>>builder()
                    .add(PlaceholderResolver.STRING)
                    .add(MessageJoiner.STRING_PARTIAL)
                    .add(new SplitComponentParser(ConfigTextParser.INSTANCE))
                    .add(new HierarchicalAppenderResolver<>(
                            Component.class, (c1, c2) -> ((MutableComponent) c1).append(c2), Component::empty))
                    .build();

    public static LangManager<Component> createLangManager(LangRegistry defaults, LangProvider provider, PlaceholderManager placeholderManager) {
        return new LangManager<>(Component.class, defaults, provider, MessagePipeline.parser(placeholderManager), COMPONENT_RESOLVER);
    }

    public static void registerDefaultPlaceholders(PlaceholderManager manager) {

        manager.register(Placeholder.of("player_username", String.class,
                ctx -> ctx.context().getFirst(ServerPlayer.class).map(spl -> spl.getGameProfile().getName())));

        manager.register(Placeholder.of("player_uuid", String.class,
                ctx -> ctx.context().getFirst(ServerPlayer.class).map(Entity::getStringUUID)));

        manager.register(Placeholder.of("player_name", Component.class,
                ctx -> ctx.context().getFirst(ServerPlayer.class).map(ServerPlayer::getName)));


        manager.register(Placeholder.of("server_online_players", String.class,
                ctx -> ctx.context().getFirst(MinecraftServer.class).map(MinecraftServer::getPlayerCount).map(Object::toString)));

        manager.register(Placeholder.of("server_max_players", String.class,
                ctx -> ctx.context().getFirst(MinecraftServer.class).map(MinecraftServer::getMaxPlayers).map(Object::toString)));


    }

}
