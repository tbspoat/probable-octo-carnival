package com.github.tbspoat.ff.client.module.impl.movement;

import com.github.tbspoat.ff.client.module.Module;
import com.github.tbspoat.ff.client.module.ModuleCategory;
import com.github.tbspoat.ff.client.module.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class SaveMoveKeys extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    @Setting(name = "Delay", min = 0.0D, max = 1000.0D, step = 10.0D)
    private long delayMs;
    private boolean lastInGui;
    private long restoreAtMs;

    public SaveMoveKeys() {
        super("SaveMoveKeys", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.currentScreen != null) {
            lastInGui = true;
            restoreAtMs = 0L;
            return;
        }

        long now = System.currentTimeMillis();

        if (lastInGui) {
            restoreAtMs = now + delayMs;
            lastInGui = false;
        }

        if (restoreAtMs > 0L && now >= restoreAtMs) {
            restoreAtMs = 0L;
            restoreMovementKeys();
        }
    }

    @Override
    public void onDisable() {
        lastInGui = false;
        restoreAtMs = 0L;
    }

    public long getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(long delayMs) {
        updateSetting("delayMs", clamp(delayMs, 0L, 1000L));
    }

    private void restoreMovementKeys() {
        if (mc.gameSettings == null) {
            return;
        }

        restoreKey(mc.gameSettings.keyBindForward);
        restoreKey(mc.gameSettings.keyBindBack);
        restoreKey(mc.gameSettings.keyBindLeft);
        restoreKey(mc.gameSettings.keyBindRight);
        restoreKey(mc.gameSettings.keyBindSprint);
        restoreKey(mc.gameSettings.keyBindSneak);
        restoreKey(mc.gameSettings.keyBindJump);
    }

    private void restoreKey(KeyBinding keyBinding) {
        int keyCode = keyBinding.getKeyCode();
        KeyBinding.setKeyBindState(keyCode, Keyboard.isKeyDown(keyCode));
        KeyBinding.onTick(keyCode);
    }

    private long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }
}
