package com.github.tbspoat.ff.mixin.client.entity;

import com.github.tbspoat.ff.client.Client;
import com.github.tbspoat.ff.client.module.impl.movement.Sprint;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {

    /**
     * Injects into onLivingUpdate immediately AFTER the player's movement inputs
     * and item usage states (blocking, eating, etc.) are processed by the vanilla client.
     * This eliminates 1-tick state fighting and prevents visual hand/sword jitter.
     */
    @Inject(
            method = "onLivingUpdate",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;movementInput:Lnet/minecraft/util/MovementInput;",
                    shift = At.Shift.AFTER
            )
    )
    private void onLivingUpdate(CallbackInfo ci) {
        Sprint sprint = Client.INSTANCE.moduleManager.getModule(Sprint.class);

        // Safety check to ensure the module manager is initialized and the module is active
        if (sprint == null || !sprint.isEnabled()) {
            return;
        }

        EntityPlayerSP player = (EntityPlayerSP) (Object) this;

        // Safely apply sprint state only if vanilla conditions permit it at this exact moment
        if (Sprint.canSprint(player)) {
            player.setSprinting(true);
        }
    }
}