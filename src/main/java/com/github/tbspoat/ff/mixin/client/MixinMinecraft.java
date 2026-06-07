package com.github.tbspoat.ff.mixin.client;

import com.github.tbspoat.ff.client.Client;
import com.github.tbspoat.ff.client.module.impl.misc.Fixes;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Shadow private int leftClickCounter;

    @Inject(method = "clickMouse", at = @At("HEAD"))
    private void clickMouseHead(CallbackInfo ci) {
        Fixes fixes = Client.INSTANCE.moduleManager.getModule(Fixes.class);

        if (fixes != null && fixes.isEnabled() && fixes.isNoHitDelay()) {
            this.leftClickCounter = 0;
        }
    }
}
