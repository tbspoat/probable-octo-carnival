package com.github.tbspoat.ff.client.gui;

import com.github.tbspoat.ff.client.Client;
import com.github.tbspoat.ff.client.module.Module;
import com.github.tbspoat.ff.client.module.ModuleCategory;
import net.minecraft.client.gui.GuiScreen;
import com.github.tbspoat.ff.client.module.ModuleManager;

import java.io.IOException;

public class ClickGuiScreen extends GuiScreen {

    private final ModuleManager moduleManager = Client.INSTANCE.moduleManager;

    private final Panel combat = new Panel("Combat", 20, 20);
    private final Panel movement = new Panel("Movement", 140, 20);

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        drawDefaultBackground();

        combat.draw(mouseX, mouseY);
        movement.draw(mouseX, mouseY);

        int y = combat.y + 20;

        for (Module m : moduleManager.getModulesByCategory(ModuleCategory.COMBAT)) {
            fontRendererObj.drawString(
                    m.getName(),
                    combat.x + 5,
                    y,
                    m.isEnabled() ? 0x55FF55 : 0xFF5555
            );
            y += 12;
        }

        int y2 = movement.y + 20;

        for (Module m : moduleManager.getModulesByCategory(ModuleCategory.MOVEMENT)) {
            fontRendererObj.drawString(
                    m.getName(),
                    movement.x + 5,
                    y2,
                    m.isEnabled() ? 0x55FF55 : 0xFF5555
            );
            y2 += 12;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

        combat.mouseClicked(mouseX, mouseY, mouseButton);
        movement.mouseClicked(mouseX, mouseY, mouseButton);

        int y = combat.y + 20;

        for (Module m : moduleManager.getModulesByCategory(ModuleCategory.COMBAT)) {
            if (mouseX >= combat.x + 5 && mouseX <= combat.x + 90 &&
                    mouseY >= y && mouseY <= y + 10) {
                if (mouseButton == 0) m.toggle();
            }
            y += 12;
        }

        int y2 = movement.y + 20;

        for (Module m : moduleManager.getModulesByCategory(ModuleCategory.MOVEMENT)) {
            if (mouseX >= movement.x + 5 && mouseX <= movement.x + 90 &&
                    mouseY >= y2 && mouseY <= y2 + 10) {
                if (mouseButton == 0) m.toggle();
            }
            y2 += 12;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        combat.mouseReleased(state);
        movement.mouseReleased(state);
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void onGuiClosed() {
        combat.mouseReleased(0);
        movement.mouseReleased(0);
    }
}