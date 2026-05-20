package com.github.tbspoat.ff.client.event;

import com.github.tbspoat.ff.client.Client;
import com.github.tbspoat.ff.client.gui.ClickGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeyHandler {

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {

        if (Keyboard.getEventKey() == Keyboard.KEY_RSHIFT && Keyboard.getEventKeyState()) {

            mc.displayGuiScreen(new ClickGuiScreen());
        }
    }
}