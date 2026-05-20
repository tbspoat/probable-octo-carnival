package com.github.tbspoat.ff;

import com.github.tbspoat.ff.client.Client;
import com.github.tbspoat.ff.client.event.AttackHandler;
import com.github.tbspoat.ff.client.event.TickHandler;
import com.github.tbspoat.ff.client.event.KeyHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "ff")
public class ExampleMod {

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

        // Initialize modules
        Client.INSTANCE.init();

        // Register Forge event handlers
        MinecraftForge.EVENT_BUS.register(new TickHandler());
        MinecraftForge.EVENT_BUS.register(new KeyHandler());
        MinecraftForge.EVENT_BUS.register(new AttackHandler());
    }
}
