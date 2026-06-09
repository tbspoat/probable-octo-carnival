package com.github.tbspoat.ff.client.module.impl.combat;

import com.github.tbspoat.ff.client.module.Module;
import com.github.tbspoat.ff.client.module.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class SprintReset extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    private int hitCounter = 0;

    // Hybrid Synchronization Fields
    private int trackedEntityId = -1;
    private long lastAttackTimestamp = 0;
    private static final long VALIDATION_WINDOW_MS = 500; // 500ms window to catch server feedback

    public SprintReset() {
        super("SprintReset", ModuleCategory.COMBAT);
    }

    /**
     * Pathway A: Outbound C02 Packet (Instant Intention Capture)
     */
    public void onAttackPacketSent(int entityId) {
        if (!this.isEnabled() || mc.thePlayer == null || mc.objectMouseOver == null) return;

        // Verify our crosshair is actually targeting a living entity
        if (mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
            this.trackedEntityId = entityId;
            this.lastAttackTimestamp = System.currentTimeMillis();
        }
    }

    /**
     * Pathway B: Inbound S19 Status Packet (Server-Verified Damage)
     */
    public void onServerEntityHurt(int entityId) {
        if (!this.isEnabled() || this.trackedEntityId == -1) return;

        // Verify the packet arrived within our validation timeframe
        long timeSinceAttack = System.currentTimeMillis() - this.lastAttackTimestamp;
        if (timeSinceAttack > VALIDATION_WINDOW_MS) {
            this.trackedEntityId = -1; // Window closed, clear tracker
            return;
        }

        // HYBRID MATCH: If the server confirms a hurt state for the EXACT entity we just attacked
        if (entityId == this.trackedEntityId) {
            onHybridHitConfirmed();
            this.trackedEntityId = -1; // Reset tracker immediately to prevent double logging
        }
    }

    @Override
    public void onDisable() {
        this.hitCounter = 0;
        this.trackedEntityId = -1;
    }

    /**
     * Fires exactly once per unique combat interaction when local intention matches server verification.
     */
    private void onHybridHitConfirmed() {
        this.hitCounter++;

        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.GREEN + "[FF] "
                            + EnumChatFormatting.GRAY + "[Hybrid Hit Verified] "
                            + EnumChatFormatting.GOLD + "#" + this.hitCounter
            ));
        }
    }
}