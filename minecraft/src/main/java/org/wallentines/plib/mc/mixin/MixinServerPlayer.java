package org.wallentines.plib.mc.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.wallentines.plib.lang.LocaleHolder;

@Mixin(ServerPlayer.class)
@Implements(@Interface(iface= LocaleHolder.class, prefix="plib$"))
public class MixinServerPlayer {

    @Shadow private String language;

    String plib$getLanguage() {

        return language;
    }


}
