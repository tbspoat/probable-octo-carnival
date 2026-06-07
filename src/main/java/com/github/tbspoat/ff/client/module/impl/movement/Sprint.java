package com.github.tbspoat.ff.client.module.impl.movement;

import com.github.tbspoat.ff.client.module.Module;
import com.github.tbspoat.ff.client.module.ModuleCategory;
import net.minecraft.client.Minecraft;

public class Sprint extends Module {

    public Sprint() {
        super("Sprint", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getMinecraft();
        // Return full control back to vanilla instantly on disable
        if (mc.thePlayer != null && !mc.gameSettings.keyBindSprint.isKeyDown()) {
            mc.thePlayer.setSprinting(false);
        }
    }
}