package com.github.tbspoat.ff.client.module.impl.movement;

import com.github.tbspoat.ff.client.module.Module;
import com.github.tbspoat.ff.client.module.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.util.Random;

public class SprintReset extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Random random = new Random();

    // =========================================================================
    // CONFIGURABLE VALUES
    // =========================================================================

    // Setting 1: Delay after landing a hit before starting the NoStop sequence
    private long delayUntilResetMs = 90L;
    private double delayUntilResetDeviationMs = 10.0; // Gaussian variation (+/- ms)

    // Setting 2: The base restart delay (Keystrokes uses 24ms base + random 0-12ms variance)
    private long noStopBaseRestartMs = 24L;
    private double noStopRestartDeviationMs = 4.0; // Gaussian variation (+/- ms)

    // Setting 3: Check if the player is actively moving forward
    private boolean checkForwardMovement = true;

    // Setting 4: The percent chance that the sprint reset triggers on a hit (0.0 to 100.0)
    private double triggerChancePercent = 100.0;

    // Setting 5: Toggleable Chat Debug Mode
    private boolean debugMode = true;

    // ATTACK EVENT TRACKING
    private static final int MS_PER_TICK = 50;
    private static final int HIT_COOLDOWN_TICKS = 6; // 300ms at normal 20 TPS
    private int hitCooldownTicks = 0;

    // NOSTOP STATE ENGINE
    private int pendingResetTicks = -1;
    private int pendingNoStopRestartTicks = -1;

    // Global bridge state for your toggle-Sprint module
    public static boolean stopSprint = false;

    public SprintReset() {
        super("SprintReset", ModuleCategory.MOVEMENT);
    }

    /**
     * Compatibility Bridge: Informs your global Sprint module to stop overriding
     * keybind states while the NoStop sequence is actively processing.
     */
    public static boolean isCurrentlyResetting() {
        return stopSprint;
    }

    public void onAttack(EntityLivingBase target) {
        if (mc.thePlayer == null || target == null || target.isDead || target.getHealth() <= 0) return;
        if (!mc.thePlayer.isSprinting()) return;
        if (triggerChancePercent <= 0.0) return;
        if (pendingNoStopRestartTicks >= 0 || hitCooldownTicks > 0) return;

        if (checkForwardMovement && mc.thePlayer.movementInput.moveForward <= 0) {
            sendDebugMessage(EnumChatFormatting.RED + "[NoStop] Cancelled: Not moving forward.");
            return;
        }

        double roll = random.nextDouble() * 100.0;
        if (roll > triggerChancePercent) {
            sendDebugMessage(EnumChatFormatting.RED + String.format("[NoStop] Cancelled: Failed roll (Rolled %.1f%% / Needed <= %.1f%%)", roll, triggerChancePercent));
            return;
        }

        if (pendingResetTicks >= 0) {
            pendingResetTicks = -1;
            sendDebugMessage(EnumChatFormatting.YELLOW + "[NoStop] Pending delay overridden by new hit.");
            triggerNoStopReset();
            return;
        }

        scheduleNoStopReset(target.getName());
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;

        // Safety Cleanups
        if (mc.thePlayer == null || mc.theWorld == null || mc.thePlayer.isDead) {
            cleanupResetState();
            return;
        }

        if (hitCooldownTicks > 0) {
            hitCooldownTicks--;
        }

        if (pendingResetTicks >= 0) {
            if (pendingResetTicks == 0) {
                pendingResetTicks = -1;
                triggerNoStopReset();
            } else {
                pendingResetTicks--;
            }
        }

        if (stopSprint && pendingNoStopRestartTicks >= 0) {
            mc.thePlayer.setSprinting(false);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
        }

        if (pendingNoStopRestartTicks >= 0) {
            if (pendingNoStopRestartTicks > 0) {
                pendingNoStopRestartTicks--;
                return;
            }

            pendingNoStopRestartTicks = -1;
            stopSprint = false;
            hitCooldownTicks = HIT_COOLDOWN_TICKS;

            if ((mc.thePlayer.movementInput.moveForward != 0.0F || mc.thePlayer.movementInput.moveStrafe != 0.0F)
                    && !mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.thePlayer.setSprinting(true);
            }

            sendDebugMessage(EnumChatFormatting.GOLD + "[NoStop] Sprint Re-engaged successfully.");
        }
    }

    private void scheduleNoStopReset(String targetName) {
        if (pendingResetTicks >= 0 || pendingNoStopRestartTicks >= 0) return;

        long calculatedDelayUntilReset = Math.max(0L, Math.round(delayUntilResetMs + (random.nextGaussian() * delayUntilResetDeviationMs)));
        pendingResetTicks = msToTicks(calculatedDelayUntilReset);

        sendDebugMessage(EnumChatFormatting.GREEN + "[NoStop] Confirmed Hit on: " + targetName);
        sendDebugMessage(EnumChatFormatting.AQUA + "[NoStop] Scheduled Break in: " + pendingResetTicks + " tick(s) (" + calculatedDelayUntilReset + "ms configured)");
    }

    /**
     * Executes the direct NoStop sprint reset.
     */
    private void triggerNoStopReset() {
        stopSprint = true;
        mc.thePlayer.setSprinting(false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);

        long calculatedRestartDelay = Math.max(1L, Math.round(noStopBaseRestartMs + (random.nextGaussian() * noStopRestartDeviationMs)));
        pendingNoStopRestartTicks = Math.max(1, msToTicks(calculatedRestartDelay));

        sendDebugMessage(EnumChatFormatting.LIGHT_PURPLE + "[NoStop] Sprint break applied for " + pendingNoStopRestartTicks + " tick(s) (" + calculatedRestartDelay + "ms configured)");
    }

    private int msToTicks(long ms) {
        return (int) Math.ceil(ms / (double) MS_PER_TICK);
    }

    private void sendDebugMessage(String message) {
        if (debugMode && mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(message));
        }
    }

    @Override
    public void onEnable() {
        cleanupResetState();
    }

    @Override
    public void onDisable() {
        cleanupResetState();
    }

    private void cleanupResetState() {
        if (mc.gameSettings != null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(),
                    Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode()));
        }
        pendingResetTicks = -1;
        pendingNoStopRestartTicks = -1;
        hitCooldownTicks = 0;
        stopSprint = false;
    }

    // =========================================================================
    // GETTERS & SETTERS FOR YOUR CONFIG SYSTEM
    // =========================================================================

    public void setDelayUntilResetMs(long ms) { this.delayUntilResetMs = ms; }
    public long getDelayUntilResetMs() { return this.delayUntilResetMs; }

    public void setDelayUntilResetDeviationMs(double ms) { this.delayUntilResetDeviationMs = ms; }
    public double getDelayUntilResetDeviationMs() { return this.delayUntilResetDeviationMs; }

    public long getNoStopBaseRestartMs() { return noStopBaseRestartMs; }
    public void setNoStopBaseRestartMs(long ms) { this.noStopBaseRestartMs = ms; }

    public double getNoStopRestartDeviationMs() { return noStopRestartDeviationMs; }
    public void setNoStopRestartDeviationMs(double ms) { this.noStopRestartDeviationMs = ms; }

    public void setCheckForwardMovement(boolean check) { this.checkForwardMovement = check; }
    public boolean isCheckForwardMovement() { return this.checkForwardMovement; }

    public void setTriggerChancePercent(double percent) { this.triggerChancePercent = Math.max(0.0, Math.min(100.0, percent)); }
    public double getTriggerChancePercent() { return this.triggerChancePercent; }

    public void setDebugMode(boolean enabled) { this.debugMode = enabled; }
    public boolean isDebugMode() { return this.debugMode; }
}
