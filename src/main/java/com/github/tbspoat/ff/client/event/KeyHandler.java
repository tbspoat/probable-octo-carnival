package com.github.tbspoat.ff.client.event;

import com.github.tbspoat.ff.client.Client;
import com.github.tbspoat.ff.client.gui.ClickGuiScreen;
import com.github.tbspoat.ff.client.module.impl.misc.Fixes;
import com.github.tbspoat.ff.client.module.impl.movement.SaveMoveKeys;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class KeyHandler {

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {

        if (Keyboard.getEventKey() == Keyboard.KEY_RSHIFT && Keyboard.getEventKeyState()) {

            mc.displayGuiScreen(new ClickGuiScreen());
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        SaveMoveKeys saveMoveKeys = Client.INSTANCE.moduleManager.getModule(SaveMoveKeys.class);
        if (saveMoveKeys != null && saveMoveKeys.isEnabled()) {
            saveMoveKeys.onTick();
        }

        Fixes fixes = Client.INSTANCE.moduleManager.getModule(Fixes.class);
        if (fixes != null && fixes.isEnabled()) {
            fixes.onTick();
        }
    }
}
