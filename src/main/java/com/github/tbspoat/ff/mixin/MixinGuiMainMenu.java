package com.github.tbspoat.ff.mixin;

import com.github.tbspoat.ff.client.Client; // Import your main Client class
import net.minecraft.client.gui.GuiMainMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMainMenu.class)
public class MixinGuiMainMenu {

    @Inject(method = "initGui", at = @At("HEAD"))
    public void onInitGui(CallbackInfo ci) {
        // Boot up your module manager and event buses!
        Client.INSTANCE.init();

        System.out.println("[Forge Client] Successfully initialized module and event layers!");
    }
}