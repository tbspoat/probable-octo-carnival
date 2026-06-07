package com.github.tbspoat.ff.mixin.client;

import com.github.tbspoat.ff.client.Client;
import com.github.tbspoat.ff.client.module.impl.misc.Fixes;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {

    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "getLook(F)Lnet/minecraft/util/Vec3;", at = @At("HEAD"), cancellable = true)
    private void mouseDelayFix(float partialTicks, CallbackInfoReturnable<Vec3> cir) {
        if (isMouseDelayFixEnabled() && (Object) this instanceof EntityPlayerSP) {
            cir.setReturnValue(super.getLook(partialTicks));
        }
    }

    private boolean isMouseDelayFixEnabled() {
        Fixes fixes = Client.INSTANCE.moduleManager.getModule(Fixes.class);
        return fixes != null && fixes.isEnabled() && fixes.isMouseDelayFix();
    }
}
