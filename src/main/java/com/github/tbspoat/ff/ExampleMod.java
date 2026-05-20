package com.github.tbspoat.ff;

import com.github.tbspoat.ff.client.Client;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "ff")
public class ExampleMod {

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Client.INSTANCE.init(); // 🔥 REQUIRED OR MODULES = EMPTY
    }
}