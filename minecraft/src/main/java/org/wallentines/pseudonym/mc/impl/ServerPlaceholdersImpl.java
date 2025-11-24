package org.wallentines.pseudonym.mc.impl;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import org.wallentines.pseudonym.*;
import org.wallentines.pseudonym.lang.LangManager;

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
        public Component copy(Component other) {
            return other.copy();
        }

        @Override
        public boolean influencesChildren(Component message) {
            return message.getStyle() != Style.EMPTY || !message.getSiblings().isEmpty();
        }
    };

    static {

        GLOBAL_PLACEHOLDERS.register(Placeholder.of("player_username", String.class,
                ctx -> ctx.context().getFirst(Player.class).map(spl -> spl.getGameProfile().name())));

        GLOBAL_PLACEHOLDERS.register(Placeholder.of("player_uuid", String.class,
                ctx -> ctx.context().getFirst(Player.class).map(Entity::getStringUUID)));

        GLOBAL_PLACEHOLDERS.register(Placeholder.of("player_name", Component.class,
                ctx -> ctx.context().getFirst(Player.class).map(Player::getDisplayName)));

        GLOBAL_PLACEHOLDERS.register(Placeholder.of("entity_uuid", String.class,
                ctx -> ctx.context().getFirst(Entity.class).map(Entity::getStringUUID)));

        GLOBAL_PLACEHOLDERS.register(Placeholder.of("entity_name", Component.class,
                ctx -> ctx.context().getFirst(Entity.class).map(Entity::getDisplayName)));

        GLOBAL_PLACEHOLDERS.register(Placeholder.of("entity_dimension", String.class, 
            ctx -> ctx.context().getFirst(Entity.class).map(ent -> ent.level().dimension().location().toString())));

        GLOBAL_PLACEHOLDERS.register(Placeholder.of("server_online_players", String.class,
                ctx -> ctx.context().getFirst(MinecraftServer.class).map(MinecraftServer::getPlayerCount).map(Object::toString)));

        GLOBAL_PLACEHOLDERS.register(Placeholder.of("server_max_players", String.class,
                ctx -> ctx.context().getFirst(MinecraftServer.class).map(MinecraftServer::getMaxPlayers).map(Object::toString)));

        LangManager.registerPlaceholders(GLOBAL_PLACEHOLDERS);
    }


}
