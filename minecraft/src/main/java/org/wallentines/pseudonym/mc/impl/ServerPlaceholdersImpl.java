package org.wallentines.pseudonym.mc.impl;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.wallentines.pseudonym.*;

import java.util.List;

public class ServerPlaceholdersImpl {

    private static final PlaceholderManager GLOBAL_PLACEHOLDERS = new PlaceholderManager();

    public static PlaceholderManager getGlobalPlaceholders() {
        return GLOBAL_PLACEHOLDERS;
    }

    public static PlaceholderManager getServerPlaceholders(MinecraftServer server) {
        return ((ServerExtension) server).getPlaceholderManager();
    }

    public static final HierarchicalAppenderResolver.Appender<Component> APPENDER = new HierarchicalAppenderResolver.Appender<Component>() {
        @Override
        public Component append(Component to, Component value) {
            return ((MutableComponent) to).append(value);
        }

        @Override
        public List<Component> children(Component message) {
            return message.getSiblings();
        }

        @Override
        public Component empty() {
            return Component.empty();
        }

        @Override
        public boolean influencesChildren(Component message) {
            return message.getStyle() != Style.EMPTY || !message.getSiblings().isEmpty();
        }
    };

    static {

        ServerPlaceholdersImpl.GLOBAL_PLACEHOLDERS.register(Placeholder.of("player_username", String.class,
                ctx -> ctx.context().getFirst(ServerPlayer.class).map(spl -> spl.getGameProfile().getName())));

        ServerPlaceholdersImpl.GLOBAL_PLACEHOLDERS.register(Placeholder.of("player_uuid", String.class,
                ctx -> ctx.context().getFirst(ServerPlayer.class).map(Entity::getStringUUID)));

        ServerPlaceholdersImpl.GLOBAL_PLACEHOLDERS.register(Placeholder.of("player_name", Component.class,
                ctx -> ctx.context().getFirst(ServerPlayer.class).map(ServerPlayer::getDisplayName)));

        ServerPlaceholdersImpl.GLOBAL_PLACEHOLDERS.register(Placeholder.of("server_online_players", String.class,
                ctx -> ctx.context().getFirst(MinecraftServer.class).map(MinecraftServer::getPlayerCount).map(Object::toString)));

        ServerPlaceholdersImpl.GLOBAL_PLACEHOLDERS.register(Placeholder.of("server_max_players", String.class,
                ctx -> ctx.context().getFirst(MinecraftServer.class).map(MinecraftServer::getMaxPlayers).map(Object::toString)));

    }


}
