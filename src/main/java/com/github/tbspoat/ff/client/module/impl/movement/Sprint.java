package com.github.tbspoat.ff.client.module.impl.movement;

import com.github.tbspoat.ff.client.module.Module;
import com.github.tbspoat.ff.client.module.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class Sprint extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    public Sprint() {
        super("Sprint", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!isEnabled()) return;

        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            // Un-press the key, but check if the player is physically holding it down
            // so we don't accidentally stop them if they are playing legitimately.
            int sprintKeyCode = mc.gameSettings.keyBindSprint.getKeyCode();
            boolean isPhysicallyHoldingKey = Keyboard.isKeyDown(sprintKeyCode);

            KeyBinding.setKeyBindState(sprintKeyCode, isPhysicallyHoldingKey);
        }
    }
}
