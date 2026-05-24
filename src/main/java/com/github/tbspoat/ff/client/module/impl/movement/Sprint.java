package com.github.tbspoat.ff.client.module.impl.movement;

import com.github.tbspoat.ff.client.module.Module;
import com.github.tbspoat.ff.client.module.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;

public class Sprint extends Module {

    public Sprint() {
        super("Sprint", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;

        // If the module is turned off, simply let vanilla regain total control.
        // We only force a stop if they aren't holding down their actual sprint key.
        if (player != null && player.isSprinting() && !mc.gameSettings.keyBindSprint.isKeyDown()) {
            player.setSprinting(false);
        }
    }

    public static boolean canSprint(EntityPlayerSP player) {
        return player.movementInput != null
                && player.movementInput.moveForward > 0.0F
                && !player.isSprinting()
                && !player.isSneaking()
                && !player.isUsingItem()
                && !player.isPotionActive(Potion.blindness)
                && !player.isCollidedHorizontally
                // Added check: If they are actively flying, let vanilla handle movement speed
                // so creative/spectator flight doesn't glitch out.
                && !player.capabilities.isFlying
                && (player.getFoodStats().getFoodLevel() > 6 || player.capabilities.allowFlying);
    }
}