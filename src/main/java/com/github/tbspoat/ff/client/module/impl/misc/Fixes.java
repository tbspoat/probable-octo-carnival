package com.github.tbspoat.ff.client.module.impl.misc;

import com.github.tbspoat.ff.client.module.Module;
import com.github.tbspoat.ff.client.module.ModuleCategory;
import com.github.tbspoat.ff.client.module.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public class Fixes extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    @Setting(name = "NoHitDelay")
    private boolean noHitDelay = true;

    @Setting(name = "MouseDelayFix")
    private boolean mouseDelayFix = true;

    @Setting(name = "ProjectL")
    private boolean projectL = true;

    public Fixes() {
        super("Fixes", ModuleCategory.MISC);
    }

    // --- Getters and Setters ---
    public boolean isNoHitDelay() { return noHitDelay; }
    public void setNoHitDelay(boolean noHitDelay) { updateSetting("NoHitDelay", noHitDelay); }

    public boolean isMouseDelayFix() { return mouseDelayFix; }
    public void setMouseDelayFix(boolean mouseDelayFix) { updateSetting("MouseDelayFix", mouseDelayFix); }

    public boolean isProjectL() { return projectL; }
    public void setProjectL(boolean projectL) { updateSetting("ProjectL", projectL); }


    /**
     * Rename this hook or call it from your client's Main Render Loop / Frame event
     * rather than the standard 20-tick loop.
     */
    public void onRenderUpdate() {
        if (!projectL) {
            return;
        }

        EntityPlayerSP player = mc.thePlayer;
        if (player == null || mc.theWorld == null) {
            return;
        }

        // Running this at frame-rate removes visual stuttering entirely
        if (player.rotationYaw != player.prevRotationYaw || player.rotationPitch != player.prevRotationPitch) {
            player.prevRenderYawOffset = player.renderYawOffset;
            player.prevRotationYawHead = player.rotationYawHead;
            player.prevRotationYaw = player.rotationYaw;
            player.prevRotationPitch = player.rotationPitch;
        }
    }

    @Override
    public void onTick() {
        // Leave empty if NoHitDelay and MouseDelayFix are handled via Mixins/Hooks
        // referencing the isNoHitDelay() / isMouseDelayFix() booleans directly.
    }
}