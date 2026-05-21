package com.github.tbspoat.ff.client.gui;

import com.github.tbspoat.ff.client.Client;
import com.github.tbspoat.ff.client.module.Module;
import com.github.tbspoat.ff.client.module.ModuleCategory;
import com.github.tbspoat.ff.client.module.ModuleManager;
import com.github.tbspoat.ff.client.module.impl.movement.SprintReset;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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
    private Module settingsModule;
    private String draggingSetting;

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
        updateDraggedSetting(mouseX);
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
        boolean showingSettings = settingsModule instanceof SprintReset &&
                settingsModule.getCategory() == selectedCategory;
        int cardWidth = showingSettings ? 132 : 142;
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
            int x = showingSettings ? startX : startX + column * (cardWidth + gap);
            int y = showingSettings ? startY + i * (cardHeight + gap) : startY + row * (cardHeight + gap);

            drawModuleCard(module, x, y, cardWidth, cardHeight, mouseX, mouseY);
        }

        if (showingSettings) {
            drawSprintResetSettings((SprintReset) settingsModule, startX + cardWidth + gap, startY, 162, 174, mouseX, mouseY);
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
        if (module instanceof SprintReset) {
            fontRendererObj.drawString("R", x + cardWidth - 43, y + 25, settingsModule == module ? 0xFFFF8D6A : 0xFF5F536C);
        }

        int toggleX = x + cardWidth - 30;
        int toggleY = y + 10;
        Gui.drawRect(toggleX, toggleY, toggleX + 20, toggleY + 10, module.isEnabled() ? 0x995D302C : 0x66231E2B);
        Gui.drawRect(module.isEnabled() ? toggleX + 12 : toggleX + 2, toggleY + 2,
                module.isEnabled() ? toggleX + 18 : toggleX + 8, toggleY + 8,
                module.isEnabled() ? 0xFFFF8D6A : 0xFF403747);
    }

    private void drawSprintResetSettings(SprintReset sprintReset, int x, int y, int width, int height, int mouseX, int mouseY) {
        drawBorderedRect(x, y, x + width, y + height, 0xCC140D1C, 0x66FF8D6A);
        fontRendererObj.drawString("SprintReset", x + 9, y + 9, 0xFFFFF3ED);
        fontRendererObj.drawString("settings", x + 9, y + 21, 0xFFFFA081);

        int rowY = y + 40;
        drawSlider("Delay", "delay", sprintReset.getDelayUntilResetMs(), 0.0, 250.0, "ms", x + 9, rowY, width - 18, mouseX, mouseY);
        rowY += 22;
        drawSlider("Delay Dev", "delayDev", sprintReset.getDelayUntilResetDeviationMs(), 0.0, 100.0, "ms", x + 9, rowY, width - 18, mouseX, mouseY);
        rowY += 22;
        drawSlider("Restart", "unsprint", sprintReset.getNoStopBaseRestartMs(), 10.0, 500.0, "ms", x + 9, rowY, width - 18, mouseX, mouseY);
        rowY += 22;
        drawSlider("Restart Dev", "unsprintDev", sprintReset.getNoStopRestartDeviationMs(), 0.0, 200.0, "ms", x + 9, rowY, width - 18, mouseX, mouseY);
        rowY += 22;
        drawSlider("Chance", "chance", sprintReset.getTriggerChancePercent(), 0.0, 100.0, "%", x + 9, rowY, width - 18, mouseX, mouseY);
        rowY += 25;
        drawBooleanSetting("Forward Check", sprintReset.isCheckForwardMovement(), x + 9, rowY, width - 18);
        rowY += 18;
        drawBooleanSetting("Debug Chat", sprintReset.isDebugMode(), x + 9, rowY, width - 18);
    }

    private void drawSlider(String label, String id, double value, double min, double max, String suffix, int x, int y, int width, int mouseX, int mouseY) {
        double percent = Math.max(0.0, Math.min(1.0, (value - min) / (max - min)));
        int barY = y + 13;
        int fillRight = x + (int) Math.round(width * percent);
        boolean hovered = isInside(mouseX, mouseY, x, y, x + width, y + 18);
        int labelColor = hovered || id.equals(draggingSetting) ? 0xFFFFF3ED : 0xFFB7AABF;

        fontRendererObj.drawString(label, x, y, labelColor);
        fontRendererObj.drawString(formatValue(value, suffix), x + width - fontRendererObj.getStringWidth(formatValue(value, suffix)), y, 0xFFFF8D6A);
        Gui.drawRect(x, barY, x + width, barY + 3, 0x66362A40);
        Gui.drawRect(x, barY, fillRight, barY + 3, 0xFFFF8D6A);
    }

    private void drawBooleanSetting(String label, boolean enabled, int x, int y, int width) {
        fontRendererObj.drawString(label, x, y + 3, 0xFFB7AABF);
        int toggleX = x + width - 22;
        Gui.drawRect(toggleX, y + 1, toggleX + 20, y + 11, enabled ? 0x995D302C : 0x66231E2B);
        Gui.drawRect(enabled ? toggleX + 12 : toggleX + 2, y + 3,
                enabled ? toggleX + 18 : toggleX + 8, y + 9,
                enabled ? 0xFFFF8D6A : 0xFF403747);
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
        boolean showingSettings = settingsModule instanceof SprintReset &&
                settingsModule.getCategory() == selectedCategory;
        int cardWidth = showingSettings ? 132 : 142;
        int cardHeight = 42;
        int gap = 8;

        for (int i = 0; i < modules.size(); i++) {
            int column = i % 2;
            int row = i / 2;
            int x = showingSettings ? startX : startX + column * (cardWidth + gap);
            int y = showingSettings ? startY + i * (cardHeight + gap) : startY + row * (cardHeight + gap);

            if (mouseButton == 0 && isInside(mouseX, mouseY, x, y, x + cardWidth, y + cardHeight)) {
                modules.get(i).toggle();
            }

            if (mouseButton == 1 && isInside(mouseX, mouseY, x, y, x + cardWidth, y + cardHeight) &&
                    modules.get(i) instanceof SprintReset) {
                settingsModule = settingsModule == modules.get(i) ? null : modules.get(i);
            }
        }

        if (mouseButton == 0 && settingsModule instanceof SprintReset &&
                settingsModule.getCategory() == selectedCategory) {
            handleSprintResetSettingsClick((SprintReset) settingsModule, mouseX, mouseY);
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) {
            dragging = false;
            draggingSetting = null;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void onGuiClosed() {
        dragging = false;
        draggingSetting = null;
        settingsModule = null;
    }

    private void handleSprintResetSettingsClick(SprintReset sprintReset, int mouseX, int mouseY) {
        int startX = guiX + sidebarWidth + 12;
        int startY = guiY + 38;
        int cardWidth = 132;
        int gap = 8;
        int x = startX + cardWidth + gap;
        int y = startY;
        int settingX = x + 9;
        int settingWidth = 144;
        int rowY = y + 40;

        if (clickSlider("delay", mouseX, mouseY, settingX, rowY, settingWidth)) {
            applyDraggedSetting(sprintReset, mouseX);
            return;
        }
        rowY += 22;
        if (clickSlider("delayDev", mouseX, mouseY, settingX, rowY, settingWidth)) {
            applyDraggedSetting(sprintReset, mouseX);
            return;
        }
        rowY += 22;
        if (clickSlider("unsprint", mouseX, mouseY, settingX, rowY, settingWidth)) {
            applyDraggedSetting(sprintReset, mouseX);
            return;
        }
        rowY += 22;
        if (clickSlider("unsprintDev", mouseX, mouseY, settingX, rowY, settingWidth)) {
            applyDraggedSetting(sprintReset, mouseX);
            return;
        }
        rowY += 22;
        if (clickSlider("chance", mouseX, mouseY, settingX, rowY, settingWidth)) {
            applyDraggedSetting(sprintReset, mouseX);
            return;
        }
        rowY += 25;
        if (isInside(mouseX, mouseY, settingX, rowY, settingX + settingWidth, rowY + 14)) {
            sprintReset.setCheckForwardMovement(!sprintReset.isCheckForwardMovement());
            return;
        }
        rowY += 18;
        if (isInside(mouseX, mouseY, settingX, rowY, settingX + settingWidth, rowY + 14)) {
            sprintReset.setDebugMode(!sprintReset.isDebugMode());
        }
    }

    private boolean clickSlider(String id, int mouseX, int mouseY, int x, int y, int width) {
        if (!isInside(mouseX, mouseY, x, y, x + width, y + 18)) {
            return false;
        }

        draggingSetting = id;
        return true;
    }

    private void updateDraggedSetting(int mouseX) {
        if (draggingSetting == null || !(settingsModule instanceof SprintReset)) return;
        if (!Mouse.isButtonDown(0)) {
            draggingSetting = null;
            return;
        }

        applyDraggedSetting((SprintReset) settingsModule, mouseX);
    }

    private void applyDraggedSetting(SprintReset sprintReset, int mouseX) {
        int startX = guiX + sidebarWidth + 12;
        int settingX = startX + 132 + 8 + 9;
        int settingWidth = 144;

        if ("delay".equals(draggingSetting)) {
            sprintReset.setDelayUntilResetMs(Math.round(sliderValue(mouseX, settingX, settingWidth, 0.0, 250.0)));
        } else if ("delayDev".equals(draggingSetting)) {
            sprintReset.setDelayUntilResetDeviationMs(sliderValue(mouseX, settingX, settingWidth, 0.0, 100.0));
        } else if ("unsprint".equals(draggingSetting)) {
            sprintReset.setNoStopBaseRestartMs(Math.round(sliderValue(mouseX, settingX, settingWidth, 10.0, 500.0)));
        } else if ("unsprintDev".equals(draggingSetting)) {
            sprintReset.setNoStopRestartDeviationMs(sliderValue(mouseX, settingX, settingWidth, 0.0, 200.0));
        } else if ("chance".equals(draggingSetting)) {
            sprintReset.setTriggerChancePercent(sliderValue(mouseX, settingX, settingWidth, 0.0, 100.0));
        }
    }

    private double sliderValue(int mouseX, int x, int width, double min, double max) {
        double percent = (mouseX - x) / (double) width;
        percent = Math.max(0.0, Math.min(1.0, percent));
        return min + ((max - min) * percent);
    }

    private String formatValue(double value, String suffix) {
        if ("ms".equals(suffix)) {
            return Math.round(value) + suffix;
        }

        return String.format(Locale.US, "%.0f%s", value, suffix);
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
