package com.github.tbspoat.ff.mixin.client;

import net.minecraft.network.play.server.S19PacketEntityStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(S19PacketEntityStatus.class)
public interface S19PacketEntityStatusAccessor {
    @Accessor("entityId")
    int getEntityId();
}