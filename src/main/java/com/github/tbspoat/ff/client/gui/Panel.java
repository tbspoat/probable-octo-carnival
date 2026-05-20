package com.github.tbspoat.ff.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class Panel {

    public int x, y;
    private int dragX, dragY;
    private boolean dragging;
    private final String title;

    public Panel(String title, int x, int y) {
        this.title = title;
        this.x = x;
        this.y = y;
    }

    public void draw(int mouseX, int mouseY) {

        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }

        Gui.drawRect(x, y, x + 100, y + 20, 0xCC202020);
        Gui.drawRect(x, y + 20, x + 100, y + 120, 0xAA101010);

        Minecraft.getMinecraft().fontRendererObj.drawString(title, x + 5, y + 6, 0xFFFFFFFF);
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {

        if (button == 0 && isHovered(mouseX, mouseY)) {
            dragging = true;
            dragX = mouseX - x;
            dragY = mouseY - y;
        }
    }

    public void mouseReleased(int button) {
        if (button == 0) {
            dragging = false;
        }
    }

    public void onGuiClosed() {
        dragging = false;
    }

    private boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + 100 &&
                mouseY >= y && mouseY <= y + 20;
    }
}
