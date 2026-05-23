package com.github.tbspoat.ff.client.gui;

import com.github.tbspoat.ff.client.Client;
import com.github.tbspoat.ff.client.module.Module;
import com.github.tbspoat.ff.client.module.ModuleCategory;
import com.github.tbspoat.ff.client.module.ModuleManager;
import com.github.tbspoat.ff.client.module.impl.movement.SprintReset;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ClickGuiScreen extends GuiScreen {

    private final ModuleManager moduleManager = Client.INSTANCE.moduleManager;
    private int guiX;
    private int guiY;
    private final int guiWidth = 450;
    private final int guiHeight = 330;
    private final int sidebarWidth = 54;

    private int dragX;
    private int dragY;
    private boolean dragging;
    private ModuleCategory selectedCategory = ModuleCategory.MOVEMENT;
    private Module settingsModule;
    private String draggingSetting;
    private int contentScroll;
    private boolean draggingScrollbar;

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

        drawReferenceBackdrop();

        drawWindow();
        drawSidebar(mouseX, mouseY);
        updateScrollbarDrag(mouseY);
        updateContentScroll(mouseX, mouseY);
        updateDraggedSetting(mouseX);
        drawModules(mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawReferenceBackdrop() {
        drawDefaultBackground();
        drawGradientRect(0, 0, width, height, 0xAA050911, 0xBB243B5F);
    }

    private void drawWindow() {
        drawSoftShadow(guiX, guiY, guiX + guiWidth, guiY + guiHeight);
        drawBorderedRect(guiX, guiY, guiX + guiWidth, guiY + guiHeight, 0xF2080712, 0x55221731);
        drawRoundedRect(guiX + 2, guiY + 2, guiX + guiWidth - 2, guiY + guiHeight - 2, 0xDA0B0813);
        Gui.drawRect(guiX + sidebarWidth, guiY + 14, guiX + sidebarWidth + 1, guiY + guiHeight - 14, 0x66311F39);

        drawTextScaled(formatCategoryName(selectedCategory), guiX + sidebarWidth + 22, guiY + 17, 0xFFFFF3ED, 1.2F);
        drawLogo(guiX + (sidebarWidth / 2) - 11, guiY + 17);
    }

    private void drawSidebar(int mouseX, int mouseY) {
        int y = guiY + 58;

        for (ModuleCategory category : ModuleCategory.values()) {
            boolean selected = category == selectedCategory;
            boolean hovered = mouseX >= guiX + 10 && mouseX <= guiX + sidebarWidth - 10 &&
                    mouseY >= y - 8 && mouseY <= y + 22;

            int color = selected ? 0x33FF8468 : hovered ? 0x221F1828 : 0x00000000;
            drawRoundedRect(guiX + 10, y - 8, guiX + sidebarWidth - 10, y + 22, color);
            if (selected) {
                drawRoundedRect(guiX + 13, y - 5, guiX + sidebarWidth - 13, y + 19, 0x224A221F);
            }

            int iconX = guiX + sidebarWidth / 2 + (category == ModuleCategory.COMBAT ? -2 : 0);
            int iconY = category == ModuleCategory.MOVEMENT ? y : y + 2;
            drawCategoryIcon(category, iconX, iconY, selected ? 0xFFFF8D6A : hovered ? 0xFF8E7182 : 0xFF463D4E);
            y += 38;
        }
    }

    private void drawModules(int mouseX, int mouseY) {
        List<Module> modules = moduleManager.getModulesByCategory(selectedCategory);

        int startX = guiX + sidebarWidth + 14;
        contentScroll = clampInt(contentScroll, 0, getMaxContentScroll());
        int startY = guiY + 48 - contentScroll;
        boolean showingSettings = settingsModule instanceof SprintReset &&
                settingsModule.getCategory() == selectedCategory;
        int cardWidth = 170;
        int cardHeight = 40;
        int gap = 10;

        if (modules.isEmpty()) {
            drawText("No modules", startX + 6, startY + 8, 0xFF6B5F73);
            return;
        }

        int settingsX = -1;
        int settingsY = -1;
        enableContentScissor();
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int column = i % 2;
            int row = i / 2;
            int x = startX + column * (cardWidth + gap);
            int y = startY + row * (cardHeight + gap);

            boolean expanded = module == settingsModule && module instanceof SprintReset;
            drawModuleCard(module, x, y, cardWidth, cardHeight, mouseX, mouseY, expanded);
            if (module == settingsModule) {
                settingsX = x;
                settingsY = y + cardHeight;
            }
        }

        if (showingSettings) {
            drawSprintResetSettings((SprintReset) settingsModule, settingsX, settingsY, 170, 326, mouseX, mouseY);
        }
        disableContentScissor();
        drawContentScrollbar();
    }

    private void drawModuleCard(Module module, int x, int y, int cardWidth, int cardHeight, int mouseX, int mouseY, boolean expanded) {
        if (y + cardHeight < guiY + 44 || y > guiY + guiHeight - 12) {
            return;
        }

        boolean hovered = mouseX >= x && mouseX <= x + cardWidth && mouseY >= y && mouseY <= y + cardHeight;
        int background = module.isEnabled() ? 0xCC21101A : hovered ? 0xBB17111F : 0x99110D19;
        int border = module.isEnabled() ? 0xAAFF8D6A : hovered ? 0x5545334A : 0x332A2230;

        if (module.isEnabled()) {
            drawModuleEnabledGlow(x, y, x + cardWidth, y + cardHeight);
        } else if (hovered) {
            drawCardGlow(x, y, x + cardWidth, y + cardHeight);
        }
        if (expanded) {
            drawAttachedHeaderRect(x, y, x + cardWidth, y + cardHeight, background, border);
        } else {
            drawBorderedRect(x, y, x + cardWidth, y + cardHeight, background, border);
        }
        drawGradientRect(x + 1, y + 1, x + cardWidth - 1, y + cardHeight - 1,
                module.isEnabled() ? 0x55351A24 : hovered ? 0x22181222 : 0x18100D17,
                module.isEnabled() ? 0x22181218 : 0x11110D19);
        if (expanded) {
            Gui.drawRect(x + 1, y + cardHeight - 3, x + cardWidth - 1, y + cardHeight, background);
        }

        int toggleX = x + cardWidth - 32;
        int toggleY = y + (cardHeight / 2) - 5;
        int toggleHeight = 11;
        int moduleTextY = toggleY + ((toggleHeight - getTextHeight()) / 2);
        drawText(module.getName(), x + 12, moduleTextY, module.isEnabled() ? 0xFFFFF3ED : 0xFF9D91A2);
        drawRoundedRect(toggleX, toggleY, toggleX + 22, toggleY + 11, module.isEnabled() ? 0x995D302C : 0x66231E2B);
        drawRoundedRect(module.isEnabled() ? toggleX + 12 : toggleX + 2, toggleY + 2,
                module.isEnabled() ? toggleX + 19 : toggleX + 9, toggleY + 9,
                module.isEnabled() ? 0xFFFF8D6A : 0xFF403747);
    }

    private void drawSprintResetSettings(SprintReset sprintReset, int x, int y, int width, int height, int mouseX, int mouseY) {
        boolean active = sprintReset.isEnabled();
        int border = active ? 0xAAFF8D6A : 0x332A2230;
        int fill = active ? 0xCC140D1C : 0xAA110D19;
        drawAttachedPanelFill(x, y, x + width, y + height, fill);
        Gui.drawRect(x + 1, y, x + width - 1, y + 36, fill);
        drawGradientRect(x + 3, y + 36, x + width - 3, y + 304,
                active ? 0x22271522 : 0x18120F18,
                active ? 0x11110D19 : 0x110F0C14);
        Gui.drawRect(x + 1, y + 304, x + width - 1, y + height, fill);
        drawAttachedPanelOutline(x, y, x + width, y + height, border);
        int rowY = y + 8;
        int contentLeft = x + 10;
        int contentWidth = width - 26;
        drawSlider("Delay", "delay", sprintReset.getDelayUntilResetMs(), 0.0, 250.0, "ms", contentLeft, rowY, contentWidth, mouseX, mouseY, active);
        rowY += 46;
        drawSlider("Delay Dev", "delayDev", sprintReset.getDelayUntilResetDeviationMs(), 0.0, 100.0, "ms", contentLeft, rowY, contentWidth, mouseX, mouseY, active);
        rowY += 46;
        drawSlider("Restart", "unsprint", sprintReset.getNoStopBaseRestartMs(), 10.0, 500.0, "ms", contentLeft, rowY, contentWidth, mouseX, mouseY, active);
        rowY += 46;
        drawSlider("Restart Dev", "unsprintDev", sprintReset.getNoStopRestartDeviationMs(), 0.0, 200.0, "ms", contentLeft, rowY, contentWidth, mouseX, mouseY, active);
        rowY += 46;
        drawSlider("Chance", "chance", sprintReset.getTriggerChancePercent(), 0.0, 100.0, "%", contentLeft, rowY, contentWidth, mouseX, mouseY, active);
        rowY += 50;
        drawBooleanSetting("Forward Check", sprintReset.isCheckForwardMovement(), contentLeft, rowY, contentWidth, active);
        rowY += 24;
        drawBooleanSetting("Server Confirm", sprintReset.isServerConfirmedHit(), contentLeft, rowY, contentWidth, active);
        rowY += 24;
        drawBooleanSetting("Debug Chat", sprintReset.isDebugMode(), contentLeft, rowY, contentWidth, active);
    }

    private void drawSlider(String label, String id, double value, double min, double max, String suffix, int x, int y, int width, int mouseX, int mouseY, boolean active) {
        if (!isVisibleSettingsRow(y, 42)) {
            return;
        }

        double percent = Math.max(0.0, Math.min(1.0, (value - min) / (max - min)));
        int textY = y + 2;
        int barY = y + 31;
        int fillRight = x + (int) Math.round(width * percent);
        boolean hovered = isInside(mouseX, mouseY, x, y, x + width, y + 42);
        int labelColor = active && (hovered || id.equals(draggingSetting)) ? 0xFFFFF3ED : active ? 0xFFB7AABF : 0xFF827789;
        int valueColor = active ? 0xFFFF8D6A : 0xFF8B7F92;
        int fillColor = active ? 0xFFFF8D6A : 0xFF6D6074;
        int knobColor = active ? 0xFFFFB09A : 0xFF91849A;
        String formattedValue = formatValue(value, suffix);

        drawTextScaled(label, x, textY, labelColor, 1.1F);
        drawTextScaled(formattedValue, x + width - getTextWidthScaled(formattedValue, 1.1F), textY, valueColor, 1.1F);
        drawRoundedRect(x, barY, x + width, barY + 4, 0x77433145);
        drawRoundedRect(x, barY, fillRight, barY + 4, fillColor);
        drawRoundedRect(fillRight - 2, barY - 2, fillRight + 3, barY + 6, knobColor);
    }

    private void drawBooleanSetting(String label, boolean enabled, int x, int y, int width, boolean active) {
        if (!isVisibleSettingsRow(y, 16)) {
            return;
        }

        int toggleX = x + width - 24;
        int toggleY = y + 2;
        int toggleHeight = 12;
        int labelY = toggleY + ((toggleHeight - getTextHeightScaled(1.1F)) / 2);
        drawTextScaled(label, x, labelY, active ? 0xFFB7AABF : 0xFF827789, 1.1F);
        drawRoundedRect(toggleX, toggleY, toggleX + 24, toggleY + toggleHeight,
                active && enabled ? 0x995D302C : 0x66231E2B);
        drawRoundedRect(enabled ? toggleX + 14 : toggleX + 3, toggleY + 3,
                enabled ? toggleX + 21 : toggleX + 10, toggleY + 9,
                active && enabled ? 0xFFFF8D6A : 0xFF5E5367);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && isInsideScrollbar(mouseX, mouseY)) {
            draggingScrollbar = true;
            updateScrollbarFromMouse(mouseY);
            return;
        }

        if (mouseButton == 0 && isInside(mouseX, mouseY, guiX, guiY, guiX + guiWidth, guiY + 28)) {
            dragging = true;
            dragX = mouseX - guiX;
            dragY = mouseY - guiY;
        }

        int categoryY = guiY + 58;
        for (ModuleCategory category : ModuleCategory.values()) {
            if (mouseButton == 0 && isInside(mouseX, mouseY, guiX + 10, categoryY - 8, guiX + sidebarWidth - 10, categoryY + 22)) {
                selectedCategory = category;
            }
            categoryY += 38;
        }

        List<Module> modules = moduleManager.getModulesByCategory(selectedCategory);
        int startX = guiX + sidebarWidth + 14;
        int startY = guiY + 48 - contentScroll;
        int cardWidth = 170;
        int cardHeight = 40;
        int gap = 10;

        for (int i = 0; i < modules.size(); i++) {
            int column = i % 2;
            int row = i / 2;
            int x = startX + column * (cardWidth + gap);
            int y = startY + row * (cardHeight + gap);

            if (mouseButton == 0 && isInsideContentViewport(mouseX, mouseY) && isInside(mouseX, mouseY, x, y, x + cardWidth, y + cardHeight)) {
                modules.get(i).toggle();
            }

            if (mouseButton == 1 && isInsideContentViewport(mouseX, mouseY) && isInside(mouseX, mouseY, x, y, x + cardWidth, y + cardHeight) &&
                    modules.get(i) instanceof SprintReset) {
                settingsModule = settingsModule == modules.get(i) ? null : modules.get(i);
            }
        }

        if (mouseButton == 0 && settingsModule instanceof SprintReset &&
                settingsModule.getCategory() == selectedCategory && isInsideContentViewport(mouseX, mouseY)) {
            handleSprintResetSettingsClick((SprintReset) settingsModule, mouseX, mouseY);
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) {
            dragging = false;
            draggingSetting = null;
            draggingScrollbar = false;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void onGuiClosed() {
        dragging = false;
        draggingSetting = null;
        draggingScrollbar = false;
        settingsModule = null;
    }

    private void handleSprintResetSettingsClick(SprintReset sprintReset, int mouseX, int mouseY) {
        int x = getSettingsPanelX();
        int y = getSettingsPanelY();
        int settingX = x + 10;
        int settingWidth = 144;
        int rowY = y + 8;

        if (clickSlider("delay", mouseX, mouseY, settingX, rowY, settingWidth)) {
            applyDraggedSetting(sprintReset, mouseX);
            return;
        }
        rowY += 46;
        if (clickSlider("delayDev", mouseX, mouseY, settingX, rowY, settingWidth)) {
            applyDraggedSetting(sprintReset, mouseX);
            return;
        }
        rowY += 46;
        if (clickSlider("unsprint", mouseX, mouseY, settingX, rowY, settingWidth)) {
            applyDraggedSetting(sprintReset, mouseX);
            return;
        }
        rowY += 46;
        if (clickSlider("unsprintDev", mouseX, mouseY, settingX, rowY, settingWidth)) {
            applyDraggedSetting(sprintReset, mouseX);
            return;
        }
        rowY += 46;
        if (clickSlider("chance", mouseX, mouseY, settingX, rowY, settingWidth)) {
            applyDraggedSetting(sprintReset, mouseX);
            return;
        }
        rowY += 50;
        if (isInside(mouseX, mouseY, settingX, rowY, settingX + settingWidth, rowY + 14)) {
            sprintReset.setCheckForwardMovement(!sprintReset.isCheckForwardMovement());
            return;
        }
        rowY += 24;
        if (isInside(mouseX, mouseY, settingX, rowY, settingX + settingWidth, rowY + 14)) {
            sprintReset.setServerConfirmedHit(!sprintReset.isServerConfirmedHit());
            return;
        }
        rowY += 24;
        if (isInside(mouseX, mouseY, settingX, rowY, settingX + settingWidth, rowY + 14)) {
            sprintReset.setDebugMode(!sprintReset.isDebugMode());
        }
    }

    private boolean clickSlider(String id, int mouseX, int mouseY, int x, int y, int width) {
        if (!isInside(mouseX, mouseY, x, y, x + width, y + 42)) {
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
        int settingX = getSettingsPanelX() + 10;
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

    private String formatCategoryName(ModuleCategory category) {
        String name = category.name().toLowerCase(Locale.US);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private void drawBorderedRect(int left, int top, int right, int bottom, int fillColor, int borderColor) {
        drawRoundedRect(left, top, right, bottom, borderColor);
        drawRoundedRect(left + 1, top + 1, right - 1, bottom - 1, fillColor);
    }

    private void drawAttachedPanelFill(int left, int top, int right, int bottom, int fillColor) {
        Gui.drawRect(left, top, right, bottom, fillColor);
    }

    private void drawAttachedPanelOutline(int left, int top, int right, int bottom, int borderColor) {
        Gui.drawRect(left, top, left + 1, bottom - 3, borderColor);
        Gui.drawRect(right - 1, top, right, bottom - 3, borderColor);
        drawRoundedBottomOutline(left, right, bottom, borderColor);
    }

    private void drawAttachedHeaderRect(int left, int top, int right, int bottom, int fillColor, int borderColor) {
        drawRoundedRect(left, top, right, bottom, borderColor);
        drawRoundedRect(left + 1, top + 1, right - 1, bottom, fillColor);
        Gui.drawRect(left + 1, bottom - 4, right - 1, bottom, fillColor);
        Gui.drawRect(left, top + 3, left + 1, bottom + 1, borderColor);
        Gui.drawRect(right - 1, top + 3, right, bottom + 1, borderColor);
    }

    private void drawRoundedBottomOutline(int left, int right, int bottom, int color) {
        Gui.drawRect(left, bottom - 3, left + 1, bottom - 1, color);
        Gui.drawRect(left + 1, bottom - 2, left + 3, bottom - 1, color);
        Gui.drawRect(left + 3, bottom - 1, right - 3, bottom, color);
        Gui.drawRect(right - 3, bottom - 2, right - 1, bottom - 1, color);
        Gui.drawRect(right - 1, bottom - 3, right, bottom - 1, color);
    }

    private void drawText(String text, int x, int y, int color) {
        mc.fontRendererObj.drawStringWithShadow(text, x, y, color);
    }

    private void drawTextScaled(String text, int x, int y, int color, float scale) {
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 1.0F);
        mc.fontRendererObj.drawStringWithShadow(text, Math.round(x / scale), Math.round(y / scale), color);
        GL11.glPopMatrix();
    }

    private int getTextWidth(String text) {
        return mc.fontRendererObj.getStringWidth(text);
    }

    private int getTextWidthScaled(String text, float scale) {
        return Math.round(getTextWidth(text) * scale);
    }

    private int getTextHeight() {
        return mc.fontRendererObj.FONT_HEIGHT;
    }

    private int getTextHeightScaled(float scale) {
        return Math.round(getTextHeight() * scale);
    }

    private void drawRoundedRect(int left, int top, int right, int bottom, int color) {
        Gui.drawRect(left + 3, top, right - 3, bottom, color);
        Gui.drawRect(left + 1, top + 1, right - 1, bottom - 1, color);
        Gui.drawRect(left, top + 3, right, bottom - 3, color);
    }

    private void drawSoftShadow(int left, int top, int right, int bottom) {
        Gui.drawRect(left + 8, bottom + 2, right - 8, bottom + 3, 0x18000000);
    }

    private void drawCardGlow(int left, int top, int right, int bottom) {
        Gui.drawRect(left + 6, top + 1, right - 6, top + 2, 0x22FF8D6A);
    }

    private void drawContentScrollbar() {
        int maxScroll = getMaxContentScroll();
        if (maxScroll <= 0) {
            return;
        }

        int trackX = getScrollbarTrackX();
        int trackTop = getScrollbarTrackTop();
        int trackBottom = getScrollbarTrackBottom();
        drawRoundedRect(trackX, trackTop, trackX + 3, trackBottom, 0x5533263B);

        int trackHeight = trackBottom - trackTop;
        int thumbHeight = getScrollbarThumbHeight();
        int thumbTop = trackTop + (int) Math.round((trackHeight - thumbHeight) * (contentScroll / (double) maxScroll));
        drawRoundedRect(trackX - 1, thumbTop, trackX + 4, thumbTop + thumbHeight, 0xFFFF8D6A);
    }

    private void updateScrollbarDrag(int mouseY) {
        if (!draggingScrollbar) {
            return;
        }

        if (!Mouse.isButtonDown(0)) {
            draggingScrollbar = false;
            return;
        }

        updateScrollbarFromMouse(mouseY);
    }

    private void updateScrollbarFromMouse(int mouseY) {
        int maxScroll = getMaxContentScroll();
        if (maxScroll <= 0) {
            contentScroll = 0;
            return;
        }

        int trackTop = getScrollbarTrackTop();
        int trackHeight = getScrollbarTrackBottom() - trackTop;
        int thumbHeight = getScrollbarThumbHeight();
        int usableTrack = Math.max(1, trackHeight - thumbHeight);
        double percent = (mouseY - trackTop - (thumbHeight / 2.0D)) / usableTrack;
        percent = Math.max(0.0D, Math.min(1.0D, percent));
        contentScroll = (int) Math.round(maxScroll * percent);
    }

    private boolean isInsideScrollbar(int mouseX, int mouseY) {
        return getMaxContentScroll() > 0 &&
                isInside(mouseX, mouseY, getScrollbarTrackX() - 6, getScrollbarTrackTop(),
                        getScrollbarTrackX() + 9, getScrollbarTrackBottom());
    }

    private int getScrollbarTrackX() {
        return guiX + guiWidth - 15;
    }

    private int getScrollbarTrackTop() {
        return guiY + 48;
    }

    private int getScrollbarTrackBottom() {
        return guiY + guiHeight - 16;
    }

    private int getScrollbarThumbHeight() {
        int trackHeight = getScrollbarTrackBottom() - getScrollbarTrackTop();
        return Math.max(22, trackHeight * getContentViewportHeight() / getContentHeight());
    }

    private void updateContentScroll(int mouseX, int mouseY) {
        int wheel = Mouse.getDWheel();
        if (wheel == 0) {
            return;
        }

        int contentX = guiX + sidebarWidth + 14;
        int contentY = guiY + 42;
        if (!isInside(mouseX, mouseY, contentX, contentY, guiX + guiWidth - 20, guiY + guiHeight - 10)) {
            return;
        }

        contentScroll += wheel < 0 ? 18 : -18;
        contentScroll = clampInt(contentScroll, 0, getMaxContentScroll());
    }

    private int getContentHeight() {
        List<Module> modules = moduleManager.getModulesByCategory(selectedCategory);
        int rows = Math.max(1, (modules.size() + 1) / 2);
        int height = rows * (40 + 10);
        if (settingsModule instanceof SprintReset && settingsModule.getCategory() == selectedCategory) {
            height += 326;
        }
        return height + 12;
    }

    private int getContentViewportHeight() {
        return guiHeight - 58;
    }

    private int getMaxContentScroll() {
        return Math.max(0, getContentHeight() - getContentViewportHeight());
    }

    private boolean isVisibleSettingsRow(int rowY, int rowHeight) {
        return rowY + rowHeight >= guiY + 44 && rowY <= guiY + guiHeight - 12;
    }

    private boolean isInsideContentViewport(int mouseX, int mouseY) {
        return isInside(mouseX, mouseY, guiX + sidebarWidth + 2, guiY + 42, guiX + guiWidth - 7, guiY + guiHeight - 12);
    }

    private void enableContentScissor() {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int scale = scaledResolution.getScaleFactor();
        int left = guiX + sidebarWidth + 2;
        int top = guiY + 42;
        int right = guiX + guiWidth - 7;
        int bottom = guiY + guiHeight - 12;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(left * scale, (scaledResolution.getScaledHeight() - bottom) * scale,
                (right - left) * scale, (bottom - top) * scale);
    }

    private void disableContentScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void drawModuleEnabledGlow(int left, int top, int right, int bottom) {
        Gui.drawRect(left + 6, top + 1, right - 6, top + 2, 0x66FF8D6A);
        Gui.drawRect(left + 2, top + 5, left + 3, bottom - 5, 0x22FF8D6A);
        Gui.drawRect(right - 3, top + 5, right - 2, bottom - 5, 0x18FF8D6A);
    }

    private void drawLogo(int x, int y) {
        drawRoundedRect(x - 5, y - 5, x + 23, y + 23, 0x221B1122);
        drawRoundedRect(x - 4, y - 4, x + 22, y + 22, 0x66311B2B);
        drawRoundedRect(x - 2, y - 2, x + 20, y + 20, 0xFF160D18);
        Gui.drawRect(x + 2, y + 2, x + 5, y + 17, 0xFFFF8D6A);
        Gui.drawRect(x + 5, y + 2, x + 15, y + 5, 0xFFFFB09A);
        Gui.drawRect(x + 5, y + 8, x + 13, y + 11, 0xFFFF8D6A);
        Gui.drawRect(x + 11, y + 2, x + 14, y + 17, 0xFFFF6F57);
        Gui.drawRect(x + 14, y + 2, x + 20, y + 5, 0xFFFFB09A);
        Gui.drawRect(x + 14, y + 8, x + 19, y + 11, 0xFFFF8D6A);
    }

    private void drawCategoryIcon(ModuleCategory category, int x, int y, int color) {
        if (category == ModuleCategory.COMBAT) {
            // Crossed sword glyph, matching the thin diagonal reference style.
            Gui.drawRect(x - 6, y + 9, x - 3, y + 12, color);
            Gui.drawRect(x - 4, y + 7, x - 1, y + 10, color);
            Gui.drawRect(x - 2, y + 5, x + 1, y + 8, color);
            Gui.drawRect(x, y + 3, x + 3, y + 6, color);
            Gui.drawRect(x + 2, y + 1, x + 5, y + 4, 0xFFFFB09A);
            Gui.drawRect(x + 4, y, x + 7, y + 2, 0xFFFF8D6A);
            Gui.drawRect(x + 5, y + 9, x + 8, y + 12, color);
            Gui.drawRect(x + 3, y + 7, x + 6, y + 10, color);
            Gui.drawRect(x + 1, y + 5, x + 4, y + 8, color);
            Gui.drawRect(x - 1, y + 3, x + 2, y + 6, color);
            Gui.drawRect(x - 3, y + 1, x, y + 4, 0xFFFFB09A);
            Gui.drawRect(x - 6, y, x - 3, y + 2, 0xFFFF8D6A);
        } else {
            // Double-chevron movement glyph from the reference rail.
            Gui.drawRect(x - 7, y + 2, x - 4, y + 5, color);
            Gui.drawRect(x - 5, y + 4, x - 2, y + 7, color);
            Gui.drawRect(x - 3, y + 6, x, y + 9, color);
            Gui.drawRect(x - 5, y + 8, x - 2, y + 11, color);
            Gui.drawRect(x - 7, y + 10, x - 4, y + 13, color);
            Gui.drawRect(x, y + 2, x + 3, y + 5, color);
            Gui.drawRect(x + 2, y + 4, x + 5, y + 7, color);
            Gui.drawRect(x + 4, y + 6, x + 7, y + 9, color);
            Gui.drawRect(x + 2, y + 8, x + 5, y + 11, color);
            Gui.drawRect(x, y + 10, x + 3, y + 13, color);
        }
    }

    private int getSettingsPanelX() {
        return getSettingsModuleCardX();
    }

    private int getSettingsPanelY() {
        return getSettingsModuleCardY() + 40;
    }

    private int getSettingsModuleCardX() {
        List<Module> modules = moduleManager.getModulesByCategory(selectedCategory);
        int index = modules.indexOf(settingsModule);
        if (index < 0) {
            index = 0;
        }

        int column = index % 2;
        return guiX + sidebarWidth + 14 + column * (170 + 10);
    }

    private int getSettingsModuleCardY() {
        List<Module> modules = moduleManager.getModulesByCategory(selectedCategory);
        int index = modules.indexOf(settingsModule);
        if (index < 0) {
            index = 0;
        }

        int row = index / 2;
        return guiY + 48 - contentScroll + row * (40 + 10);
    }

    private boolean isInside(int mouseX, int mouseY, int left, int top, int right, int bottom) {
        return mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
    }
}
