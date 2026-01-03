package org.wallentines.pseudonym.mc.impl;

import net.minecraft.core.ClientAsset.ResourceTexture;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;

import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.mc.api.ConfigOps;
import org.wallentines.pseudonym.*;
import org.wallentines.pseudonym.lang.LangManager;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

        GLOBAL_PLACEHOLDERS.register(Placeholder.of("player_face", Component.class,
                ctx -> ctx.context().getFirst(Player.class).map(player -> 
                (Component) Component.object(
                    new PlayerSprite(ResolvableProfile.createResolved(player.getGameProfile()), true)
                ))
                .or(() -> ctx.context().getFirst(UUID.class).map(uuid -> 
                Component.object(
                    new PlayerSprite(ResolvableProfile.createUnresolved(uuid), true)
                )))
        ));

        GLOBAL_PLACEHOLDERS.register(Placeholder.of("entity_uuid", String.class,
                ctx -> ctx.context().getFirst(Entity.class).map(Entity::getStringUUID)));

        GLOBAL_PLACEHOLDERS.register(Placeholder.of("entity_name", Component.class,
                ctx -> ctx.context().getFirst(Entity.class).map(Entity::getDisplayName)));

        GLOBAL_PLACEHOLDERS.register(Placeholder.of("entity_dimension", String.class, 
            ctx -> ctx.context().getFirst(Entity.class).map(ent -> ent.level().dimension().identifier().toString())));

        GLOBAL_PLACEHOLDERS.register(Placeholder.of("server_online_players", String.class,
                ctx -> ctx.context().getFirst(MinecraftServer.class).map(MinecraftServer::getPlayerCount).map(Object::toString)));

        GLOBAL_PLACEHOLDERS.register(Placeholder.of("server_max_players", String.class,
                ctx -> ctx.context().getFirst(MinecraftServer.class).map(MinecraftServer::getMaxPlayers).map(Object::toString)));

        GLOBAL_PLACEHOLDERS.register(Placeholder.of("translate", Component.class,
                ctx -> Optional.of(
                Component.translatable(
                    MessagePipeline.RESOLVE_STRING.accept(ctx.param(), ctx.context())
                )), ParameterTransformer.IDENTITY));

        GLOBAL_PLACEHOLDERS.register(Placeholder.of("keybind", Component.class,
                ctx -> Optional.of(
                Component.keybind(
                    MessagePipeline.RESOLVE_STRING.accept(ctx.param(), ctx.context())
                )), ParameterTransformer.IDENTITY));

        GLOBAL_PLACEHOLDERS.register(Placeholder.of("item", Component.class,
                ctx -> {
                    try {
                        return Optional.of(
                            Component.object(
                                new AtlasSprite(
                                    AtlasIds.ITEMS,
                                    Identifier.tryParse(MessagePipeline.RESOLVE_STRING.accept(ctx.param(), ctx.context()))
                                )
                            )
                        );
                    } catch(IllegalArgumentException ex) {
                        return Optional.empty();
                    }
                }, ParameterTransformer.IDENTITY));
        
        GLOBAL_PLACEHOLDERS.register(Placeholder.of("block", Component.class,
                ctx -> {
                    try {
                        return Optional.of(
                            Component.object(
                                new AtlasSprite(
                                    AtlasIds.BLOCKS,
                                    Identifier.tryParse(MessagePipeline.RESOLVE_STRING.accept(ctx.param(), ctx.context()))
                                )
                            )
                        );
                    } catch(IllegalArgumentException ex) {
                        return Optional.empty();
                    }
                }, ParameterTransformer.IDENTITY));

        GLOBAL_PLACEHOLDERS.register(Placeholder.of("face_texture", Component.class,
                ctx -> { 
                try {
                    ConfigSection encoded = new ConfigSection()
                        .with("texture", MessagePipeline.RESOLVE_STRING.accept(ctx.param(), ctx.context()));
                    ResolvableProfile profile = ResolvableProfile.CODEC.decode(ConfigOps.INSTANCE, encoded).getOrThrow().getFirst();
                    return Optional.of(
                        Component.object(
                            new PlayerSprite(profile, false)
                        )
                    );
                } catch(IllegalArgumentException ex) {
                    return Optional.empty();
                }
            }, ParameterTransformer.IDENTITY));

        LangManager.registerPlaceholders(GLOBAL_PLACEHOLDERS);
    }


}
