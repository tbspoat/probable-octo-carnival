package com.github.tbspoat.ff.mixin.client;

import com.github.tbspoat.ff.client.Client;
import com.github.tbspoat.ff.client.module.impl.combat.SprintReset;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    /**
     * Outbound Hook: Catches your attack packets instantly.
     */
    @Inject(method = "addToSendQueue", at = @At("HEAD"))
    private void onPacketSendInject(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof C02PacketUseEntity) {
            C02PacketUseEntity useEntityPacket = (C02PacketUseEntity) packet;

            if (useEntityPacket.getAction() == C02PacketUseEntity.Action.ATTACK) {
                SprintReset sprintReset = Client.INSTANCE.moduleManager.getModule(SprintReset.class);
                if (sprintReset != null && sprintReset.isEnabled()) {
                    int targetId = ((C02PacketUseEntityAccessor) useEntityPacket).getEntityId();
                    sprintReset.onAttackPacketSent(targetId);
                }
            }
        }
    }

    /**
     * Inbound Hook: Catches server-side entity status updates (e.g., Hurt animation).
     */
    @Inject(method = "handleEntityStatus", at = @At("HEAD"))
    private void onHandleEntityStatusInject(S19PacketEntityStatus packet, CallbackInfo ci) {
        SprintReset sprintReset = Client.INSTANCE.moduleManager.getModule(SprintReset.class);
        if (sprintReset != null && sprintReset.isEnabled()) {
            // Status code 2 = Entity hurt/damaged animation
            if (packet.getOpCode() == 2) {
                // FIXED: Cast the packet to our new accessor interface to fetch the hidden ID
                int hurtTargetId = ((S19PacketEntityStatusAccessor) packet).getEntityId();
                sprintReset.onServerEntityHurt(hurtTargetId);
            }
        }
    }
}