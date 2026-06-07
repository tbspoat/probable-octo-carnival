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

    public boolean isNoHitDelay() {
        return noHitDelay;
    }

    public void setNoHitDelay(boolean noHitDelay) {
        updateSetting("noHitDelay", noHitDelay);
    }

    public boolean isMouseDelayFix() {
        return mouseDelayFix;
    }

    public void setMouseDelayFix(boolean mouseDelayFix) {
        updateSetting("mouseDelayFix", mouseDelayFix);
    }

    public boolean isProjectL() {
        return projectL;
    }

    public void setProjectL(boolean projectL) {
        updateSetting("projectL", projectL);
    }

    @Override
    public void onTick() {
        if (!projectL) {
            return;
        }

        EntityPlayerSP player = mc.thePlayer;
        if (player == null || mc.theWorld == null) {
            return;
        }

        if (player.rotationYaw != player.prevRotationYaw ||
                player.rotationPitch != player.prevRotationPitch) {
            player.prevRenderYawOffset = player.renderYawOffset;
            player.prevRotationYawHead = player.rotationYawHead;
            player.prevRotationYaw = player.rotationYaw;
            player.prevRotationPitch = player.rotationPitch;
        }
    }
}
