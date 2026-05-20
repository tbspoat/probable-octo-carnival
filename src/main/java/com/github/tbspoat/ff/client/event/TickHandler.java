package com.github.tbspoat.ff.client.event;

import com.github.tbspoat.ff.client.Client;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TickHandler {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Client.INSTANCE.moduleManager.onTick();
    }
}
