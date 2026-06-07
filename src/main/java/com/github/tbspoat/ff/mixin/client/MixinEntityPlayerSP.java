package com.github.tbspoat.ff.mixin.client;

import com.github.tbspoat.ff.client.Client;
import com.github.tbspoat.ff.client.module.impl.movement.Sprint;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovementInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP {

    @Shadow
    public MovementInput movementInput;

    @Inject(method = "onLivingUpdate", at = @At("HEAD"))
    private void handleSprint(CallbackInfo ci) {
        EntityPlayerSP player = (EntityPlayerSP) (Object) this;
        Sprint sprint = Client.INSTANCE.moduleManager.getModule(Sprint.class);

        // Standard structural safety checks
        if (sprint == null || !sprint.isEnabled() || movementInput == null) {
            return;
        }

        // Removed !player.isSprinting() so the mixin can forcefully hold the state across ticks
        boolean canSprint = !player.isSneaking()
                && !player.isUsingItem()
                && !player.isPotionActive(Potion.blindness)
                && !player.isCollidedHorizontally
                && !player.capabilities.isFlying
                && (player.getFoodStats().getFoodLevel() > 6 || player.capabilities.allowFlying);

        if (canSprint && movementInput.moveForward > 0.0F) {
            player.setSprinting(true);
        }
    }
}