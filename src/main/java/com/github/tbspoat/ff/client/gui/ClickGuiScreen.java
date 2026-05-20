package com.github.tbspoat.ff.client.gui;

import com.github.tbspoat.ff.client.Client;
import com.github.tbspoat.ff.client.module.Module;
import com.github.tbspoat.ff.client.module.ModuleCategory;
import com.github.tbspoat.ff.client.module.ModuleManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

public class ClickGuiScreen extends GuiScreen {

    private final ModuleManager moduleManager = Client.INSTANCE.moduleManager;

    private int guiX;
    private int guiY;
    private final int guiWidth = 360;
    private final int guiHeight = 230;
    private final int sidebarWidth = 44;

    private int dragX;
    private int dragY;
    private boolean dragging;
    private ModuleCategory selectedCategory = ModuleCategory.MOVEMENT;

    @Override
    public void initGui() {
        guiX = (width - guiWidth) / 2;
        guiY = (height - guiHeight) / 2;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (dragging) {
            guiX = mouseX - dragX;
            guiY = mouseY - dragY;
        }

        drawDefaultBackground();

        drawWindow();
        drawSidebar(mouseX, mouseY);
        drawModules(mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawWindow() {
        drawBorderedRect(guiX, guiY, guiX + guiWidth, guiY + guiHeight, 0xF20C0815, 0x662B1730);
        Gui.drawRect(guiX + sidebarWidth, guiY + 8, guiX + sidebarWidth + 1, guiY + guiHeight - 8, 0x55382740);
        Gui.drawRect(guiX + sidebarWidth + 8, guiY + 8, guiX + guiWidth - 10, guiY + 28, 0x331E1427);

        fontRendererObj.drawString(selectedCategory.name(), guiX + sidebarWidth + 18, guiY + 15, 0xFFFFEAE3);
        fontRendererObj.drawString("FF", guiX + 16, guiY + 16, 0xFFFF8D6A);
    }

    private void drawSidebar(int mouseX, int mouseY) {
        int y = guiY + 48;

        for (ModuleCategory category : ModuleCategory.values()) {
            boolean selected = category == selectedCategory;
            boolean hovered = mouseX >= guiX + 8 && mouseX <= guiX + sidebarWidth - 8 &&
                    mouseY >= y - 6 && mouseY <= y + 18;

            int color = selected ? 0x33FF8D6A : hovered ? 0x221F1828 : 0x00000000;
            Gui.drawRect(guiX + 8, y - 6, guiX + sidebarWidth - 8, y + 18, color);

            String icon = category == ModuleCategory.COMBAT ? "C" : "M";
            int textColor = selected ? 0xFFFF8D6A : 0xFF5F536C;
            fontRendererObj.drawString(icon, guiX + 19, y + 2, textColor);
            y += 34;
        }
    }

    private void drawModules(int mouseX, int mouseY) {
        List<Module> modules = moduleManager.getModulesByCategory(selectedCategory);

        int startX = guiX + sidebarWidth + 12;
        int startY = guiY + 38;
        int cardWidth = 142;
        int cardHeight = 42;
        int gap = 8;

        if (modules.isEmpty()) {
            fontRendererObj.drawString("No modules", startX + 6, startY + 8, 0xFF6B5F73);
            return;
        }

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int column = i % 2;
            int row = i / 2;
            int x = startX + column * (cardWidth + gap);
            int y = startY + row * (cardHeight + gap);

            drawModuleCard(module, x, y, cardWidth, cardHeight, mouseX, mouseY);
        }
    }

    private void drawModuleCard(Module module, int x, int y, int cardWidth, int cardHeight, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + cardWidth && mouseY >= y && mouseY <= y + cardHeight;
        int background = module.isEnabled() ? 0xCC1F1019 : hovered ? 0xAA17111F : 0x99120E1A;
        int border = module.isEnabled() ? 0x88FF8D6A : 0x44362640;

        drawBorderedRect(x, y, x + cardWidth, y + cardHeight, background, border);
        Gui.drawRect(x + 1, y + 1, x + cardWidth - 1, y + 2, module.isEnabled() ? 0x66FF8D6A : 0x22362640);

        fontRendererObj.drawString(module.getName(), x + 9, y + 10, module.isEnabled() ? 0xFFFFF3ED : 0xFF7A7080);
        fontRendererObj.drawString(module.getCategory().name(), x + 9, y + 25, module.isEnabled() ? 0xFFFFA081 : 0xFF51485A);

        int toggleX = x + cardWidth - 30;
        int toggleY = y + 10;
        Gui.drawRect(toggleX, toggleY, toggleX + 20, toggleY + 10, module.isEnabled() ? 0x995D302C : 0x66231E2B);
        Gui.drawRect(module.isEnabled() ? toggleX + 12 : toggleX + 2, toggleY + 2,
                module.isEnabled() ? toggleX + 18 : toggleX + 8, toggleY + 8,
                module.isEnabled() ? 0xFFFF8D6A : 0xFF403747);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && isInside(mouseX, mouseY, guiX, guiY, guiX + guiWidth, guiY + 28)) {
            dragging = true;
            dragX = mouseX - guiX;
            dragY = mouseY - guiY;
        }

        int categoryY = guiY + 48;
        for (ModuleCategory category : ModuleCategory.values()) {
            if (mouseButton == 0 && isInside(mouseX, mouseY, guiX + 8, categoryY - 6, guiX + sidebarWidth - 8, categoryY + 18)) {
                selectedCategory = category;
            }
            categoryY += 34;
        }

        List<Module> modules = moduleManager.getModulesByCategory(selectedCategory);
        int startX = guiX + sidebarWidth + 12;
        int startY = guiY + 38;
        int cardWidth = 142;
        int cardHeight = 42;
        int gap = 8;

        for (int i = 0; i < modules.size(); i++) {
            int column = i % 2;
            int row = i / 2;
            int x = startX + column * (cardWidth + gap);
            int y = startY + row * (cardHeight + gap);

            if (mouseButton == 0 && isInside(mouseX, mouseY, x, y, x + cardWidth, y + cardHeight)) {
                modules.get(i).toggle();
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) {
            dragging = false;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void onGuiClosed() {
        dragging = false;
    }

    private void drawBorderedRect(int left, int top, int right, int bottom, int fillColor, int borderColor) {
        Gui.drawRect(left, top, right, bottom, fillColor);
        Gui.drawRect(left, top, right, top + 1, borderColor);
        Gui.drawRect(left, bottom - 1, right, bottom, borderColor);
        Gui.drawRect(left, top, left + 1, bottom, borderColor);
        Gui.drawRect(right - 1, top, right, bottom, borderColor);
    }

    private boolean isInside(int mouseX, int mouseY, int left, int top, int right, int bottom) {
        return mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
    }
}
