package com.github.tbspoat.ff.client.module.impl.movement;

import com.github.tbspoat.ff.client.module.Module;
import com.github.tbspoat.ff.client.module.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;

public class Sprint extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    public Sprint() {
        super("Sprint", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!isEnabled()) return;

        // Enhanced safe checks (adds Hunger and Blindness validation)
        boolean canSprint = mc.thePlayer.movementInput.moveForward > 0 &&
                !mc.thePlayer.isSneaking() &&
                !mc.thePlayer.isCollidedHorizontally &&
                mc.thePlayer.getFoodStats().getFoodLevel() > 6 &&
                !mc.thePlayer.isPotionActive(Potion.blindness);

        if (canSprint) {
            mc.thePlayer.setSprinting(true);
        } else {
            mc.thePlayer.setSprinting(false);
        }
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            mc.thePlayer.setSprinting(false);
        }
    }
}