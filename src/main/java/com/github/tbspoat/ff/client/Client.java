package com.github.tbspoat.ff.client;

import com.github.tbspoat.ff.client.module.ModuleManager;
import com.github.tbspoat.ff.client.event.KeyHandler;
import com.github.tbspoat.ff.client.event.TickHandler;
import com.github.tbspoat.ff.client.module.impl.movement.Sprint;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class Client {

    public static final Client INSTANCE = new Client();

    public final ModuleManager moduleManager = new ModuleManager();

    public void init() {
        FMLCommonHandler.instance().bus().register(new KeyHandler());
        FMLCommonHandler.instance().bus().register(new TickHandler());
        moduleManager.add(new Sprint());
    }
}
