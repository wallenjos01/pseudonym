package org.wallentines.pseudonym.mc.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.wallentines.pseudonym.lang.LocaleHolder;

@Mixin(ServerPlayer.class)
@Implements(@Interface(iface = LocaleHolder.class, prefix="pseudonym$"))
public class MixinServerPlayer {

    @Shadow private String language;

    public String pseudonym$getLanguage() {

        return language;
    }


}
