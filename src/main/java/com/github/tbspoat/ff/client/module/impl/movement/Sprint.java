package com.github.tbspoat.ff.client.module.impl.movement;

import com.github.tbspoat.ff.client.module.Module;
import com.github.tbspoat.ff.client.module.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class Sprint extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    public Sprint() {
        super("Sprint", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onTick() {
        // Essential safety check: Do nothing if the player is not fully loaded in a world
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        // Force the sprint key to be registered as "pressed".
        // Minecraft's internal loop handles all vanilla laws (hunger, blindness) natively.
        KeyBinding.setKeyBindState(
                mc.gameSettings.keyBindSprint.getKeyCode(),
                true
        );
    }

    @Override
    public void onDisable() {
        // Prevent NullPointerExceptions if the module is disabled while disconnecting
        if (mc.thePlayer == null) {
            return;
        }

        int sprintKeyCode = mc.gameSettings.keyBindSprint.getKeyCode();
        boolean isPressed = false;

        if (sprintKeyCode < 0) {
            // Convert Minecraft's internal mouse code back to standard LWJGL mouse mappings
            int mouseButton = sprintKeyCode + 100;

            // Ensure the mouse system is initialized and the index is strictly within the hardware bounds
            if (Mouse.isCreated()
                    && mouseButton >= 0
                    && mouseButton < Mouse.getButtonCount()) {
                isPressed = Mouse.isButtonDown(mouseButton);
            }
        } else if (sprintKeyCode != 0) {
            // Standard keyboard keycheck
            isPressed = Keyboard.isKeyDown(sprintKeyCode);
        }

        // Safely restore the state so the player doesn't get stuck sprinting
        KeyBinding.setKeyBindState(sprintKeyCode, isPressed);
    }
}