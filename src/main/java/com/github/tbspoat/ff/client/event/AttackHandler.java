package com.github.tbspoat.ff.client.event;

import com.github.tbspoat.ff.client.Client;
import com.github.tbspoat.ff.client.module.impl.movement.SprintReset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.EntityLivingBase;

public class AttackHandler {

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || event.entityPlayer != player) return;
        if (event.entityPlayer.worldObj == null || !event.entityPlayer.worldObj.isRemote) return;

        if (event.target == null) return;
        if (!(event.target instanceof EntityLivingBase)) return;

        SprintReset module =
                (SprintReset) Client.INSTANCE.moduleManager.getModules()
                        .stream()
                        .filter(m -> m instanceof SprintReset)
                        .findFirst()
                        .orElse(null);

        if (module == null) return;
        if (!module.isEnabled()) return;

        module.onAttack((EntityLivingBase) event.target);
    }
}
