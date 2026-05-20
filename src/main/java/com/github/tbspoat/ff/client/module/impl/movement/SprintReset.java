package com.github.tbspoat.ff.client.module.impl.movement;

import com.github.tbspoat.ff.client.module.Module;
import com.github.tbspoat.ff.client.module.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;

import java.util.Random;

public class SprintReset extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Random random = new Random();

    // ==========================================
    // CONFIGURABLE VALUES
    // ==========================================

    // Setting 1: Delay after landing a hit before dropping sprint (Mean and Deviation)
    private long delayAfterHitMs = 20L;
    private double delayAfterHitDeviationMs = 5.0;

    // Setting 2: How long to stay unsprinted before turning it back on (Mean and Deviation)
    private long timeSpentUnprintedMs = 100L;
    private double timeSpentUnprintedDeviationMs = 15.0;

    // Setting 3: Check if the player is actively pressing W/moving forward
    private boolean checkForwardMovement = true;

    // Setting 4: The percent chance that the sprint reset triggers on a hit (0.0 to 100.0)
    private double triggerChancePercent = 100.0;

    // Setting 5: Toggleable Chat Debug Mode
    private boolean debugMode = true;

    // HIT DETECTION INTERNAL STATE
    private EntityLivingBase lastTarget;
    private int lastHurtTime;
    private long lastHitTime;
    private static final long HIT_COOLDOWN_MS = 50L;

    // SPRINT RESET INTERNAL STATE
    private boolean resetting = false;
    private long disableSprintAt = 0L;
    private long enableSprintAt = 0L;

    // COMPATIBILITY BRIDGE: Allows the Sprint module to read our active reset state
    private static boolean currentlyResetting = false;

    public SprintReset() {
        super("SprintReset", ModuleCategory.MOVEMENT);
    }

    public static boolean isCurrentlyResetting() {
        return currentlyResetting;
    }

    public void onAttack(EntityLivingBase target) {
        if (mc.thePlayer == null || target == null || target.isDead || target.getHealth() <= 0) return;
        if (resetting) return;

        long now = System.currentTimeMillis();
        if (now - lastHitTime < HIT_COOLDOWN_MS) return;

        if (checkForwardMovement && mc.thePlayer.movementInput.moveForward <= 0) {
            sendDebugMessage(EnumChatFormatting.RED + "[SprintReset] Cancelled: Player is not moving forward.");
            return;
        }

        double roll = random.nextDouble() * 100.0;
        if (roll > triggerChancePercent) {
            sendDebugMessage(EnumChatFormatting.RED + String.format("[SprintReset] Cancelled: Failed chance roll (Rolled %.1f%% / Needed <= %.1f%%)", roll, triggerChancePercent));
            return;
        }

        lastHitTime = now;
        sendDebugMessage(EnumChatFormatting.GREEN + "[AttackHandler] Confirmed Attack on: " + target.getName());
        triggerResetSequence(now);
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        if (mc.thePlayer == null || mc.theWorld == null) {
            resetHitTracking();
            cleanupResetState();
            return;
        }

        if (resetting) {
            long now = System.currentTimeMillis();

            // PHASE 1: The "Sprint Off" Window
            if (now >= disableSprintAt && now < enableSprintAt) {
                currentlyResetting = true;

                mc.thePlayer.setSprinting(false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
            }

            // PHASE 2: The "Sprint On" Milestone
            if (now >= enableSprintAt) {
                currentlyResetting = false;

                // Re-sprint safely if moving forward
                if (mc.thePlayer.movementInput.moveForward > 0 && !mc.thePlayer.isCollidedHorizontally) {
                    mc.thePlayer.setSprinting(true);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
                }

                resetting = false;
            }
        }
    }

    private void runHitDetector() {
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.entityHit == null) {
            resetTargetTracking();
            return;
        }

        Entity entity = mop.entityHit;
        if (!(entity instanceof EntityLivingBase)) {
            resetTargetTracking();
            return;
        }

        EntityLivingBase target = (EntityLivingBase) entity;
        if (target.isDead || target.getHealth() <= 0) {
            resetTargetTracking();
            return;
        }

        if (lastTarget != target) {
            lastTarget = target;
            lastHurtTime = target.hurtTime;
            return;
        }

        boolean freshDamage = target.hurtTime > 0 && target.hurtTime > lastHurtTime;

        if (freshDamage) {
            long now = System.currentTimeMillis();
            if (now - lastHitTime >= HIT_COOLDOWN_MS) {
                lastHitTime = now;

                sendDebugMessage(EnumChatFormatting.GREEN + "[HitDetector] Confirmed Hit on: " + target.getName());

                // NEW CHECK 1: Ensure forward movement if the configuration is enabled
                if (checkForwardMovement && mc.thePlayer.movementInput.moveForward <= 0) {
                    sendDebugMessage(EnumChatFormatting.RED + "[SprintReset] Cancelled: Player is not moving forward.");
                    lastHurtTime = target.hurtTime;
                    return;
                }

                // NEW CHECK 2: Trigger chance validation (random value between 0.0 and 100.0)
                double roll = random.nextDouble() * 100.0;
                if (roll > triggerChancePercent) {
                    sendDebugMessage(EnumChatFormatting.RED + String.format("[SprintReset] Cancelled: Failed chance roll (Rolled %.1f%% / Needed <= %.1f%%)", roll, triggerChancePercent));
                    lastHurtTime = target.hurtTime;
                    return;
                }

                triggerResetSequence(now);
            }
        }

        lastHurtTime = target.hurtTime;
    }

    private void triggerResetSequence(long currentTimeMs) {
        resetting = true;

        // Calculate Gaussian randomized offsets
        long calculatedDelayAfterHit = Math.max(0L, Math.round(delayAfterHitMs + (random.nextGaussian() * delayAfterHitDeviationMs)));
        long calculatedUnprintedTime = Math.max(10L, Math.round(timeSpentUnprintedMs + (random.nextGaussian() * timeSpentUnprintedDeviationMs)));

        // Set the exact system time milestones using our randomized durations
        disableSprintAt = currentTimeMs + calculatedDelayAfterHit;
        enableSprintAt = disableSprintAt + calculatedUnprintedTime;

        // Print final chosen calculations to client chat
        sendDebugMessage(EnumChatFormatting.AQUA + "[SprintReset] Scheduled: Drop Sprint in "
                + calculatedDelayAfterHit + "ms | Keep off for " + calculatedUnprintedTime + "ms");
    }

    private void sendDebugMessage(String message) {
        if (debugMode && mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(message));
        }
    }

    @Override
    public void onDisable() {
        resetHitTracking();
        cleanupResetState();
    }

    private void resetTargetTracking() {
        lastTarget = null;
        lastHurtTime = 0;
    }

    private void resetHitTracking() {
        resetTargetTracking();
        lastHitTime = 0L;
    }

    private void cleanupResetState() {
        resetting = false;
        currentlyResetting = false;
    }

    // ==========================================
    // GETTERS & SETTERS FOR YOUR CONFIG SYSTEM
    // ==========================================

    public void setDelayAfterHitMs(long ms) { this.delayAfterHitMs = ms; }
    public long getDelayAfterHitMs() { return this.delayAfterHitMs; }

    public void setDelayAfterHitDeviationMs(double ms) { this.delayAfterHitDeviationMs = ms; }
    public double getDelayAfterHitDeviationMs() { return this.delayAfterHitDeviationMs; }

    public void setTimeSpentUnprintedMs(long ms) { this.timeSpentUnprintedMs = ms; }
    public long getTimeSpentUnprintedMs() { return this.timeSpentUnprintedMs; }

    public void setTimeSpentUnprintedDeviationMs(double ms) { this.timeSpentUnprintedDeviationMs = ms; }
    public double getTimeSpentUnprintedDeviationMs() { return this.timeSpentUnprintedDeviationMs; }

    public void setCheckForwardMovement(boolean check) { this.checkForwardMovement = check; }
    public boolean isCheckForwardMovement() { return this.checkForwardMovement; }

    public void setTriggerChancePercent(double percent) { this.triggerChancePercent = Math.max(0.0, Math.min(100.0, percent)); }
    public double getTriggerChancePercent() { return this.triggerChancePercent; }

    public void setDebugMode(boolean enabled) { this.debugMode = enabled; }
    public boolean isDebugMode() { return this.debugMode; }
}
