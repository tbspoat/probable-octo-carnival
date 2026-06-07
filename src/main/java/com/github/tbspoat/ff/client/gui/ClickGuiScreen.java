package com.github.tbspoat.ff.client.gui;

import com.github.tbspoat.ff.client.Client;
import com.github.tbspoat.ff.client.module.Module;
import com.github.tbspoat.ff.client.module.ModuleCategory;
import com.github.tbspoat.ff.client.module.ModuleManager;
import com.github.tbspoat.ff.client.module.Setting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ClickGuiScreen extends GuiScreen {

    private static boolean savedPosition;
    private static int savedGuiX;
    private static int savedGuiY;
    private static ModuleCategory savedSelectedCategory = ModuleCategory.MOVEMENT;
    private static boolean savedCollapsedModulesInitialized;
    private static final Set<String> savedCollapsedModuleNames = new HashSet<String>();
    private static final Map<ModuleCategory, Integer> savedScrollByCategory = new EnumMap<ModuleCategory, Integer>(ModuleCategory.class);

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
    private int contentScroll;
    private boolean draggingScrollbar;
    private final Set<Module> collapsedModules = new HashSet<Module>();
    private Module draggingSettingModule;
    private Field draggingSettingField;
    private int draggingSliderX;
    private int draggingSliderWidth;

    @Override
    public void initGui() {
        if (savedPosition) {
            guiX = clampInt(savedGuiX, 0, Math.max(0, width - guiWidth));
            guiY = clampInt(savedGuiY, 0, Math.max(0, height - guiHeight));
        } else {
            guiX = (width - guiWidth) / 2;
            guiY = (height - guiHeight) / 2;
        }

        selectedCategory = savedSelectedCategory;
        restoreCollapsedModules();
        restoreContentScroll();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (dragging) {
            guiX = clampInt(mouseX - dragX, 0, Math.max(0, width - guiWidth));
            guiY = clampInt(mouseY - dragY, 0, Math.max(0, height - guiHeight));
        }

        drawReferenceBackdrop();

        drawWindow();
        drawSidebar(mouseX, mouseY);
        updateScrollbarDrag(mouseY);
        updateSettingSliderDrag(mouseX);
        updateContentScroll(mouseX, mouseY);
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
        int cardWidth = guiWidth - sidebarWidth - 42;
        int gap = 10;
        int y = startY;

        if (modules.isEmpty()) {
            drawText("No modules", startX + 6, startY + 8, 0xFF6B5F73);
            return;
        }

        enableContentScissor();
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int cardHeight = getModuleCardHeight(module);

            drawModuleCard(module, startX, y, cardWidth, cardHeight, mouseX, mouseY);
            y += cardHeight + gap;
        }

        disableContentScissor();
        drawContentScrollbar();
    }

    private void drawModuleCard(Module module, int x, int y, int cardWidth, int cardHeight, int mouseX, int mouseY) {
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
        drawBorderedRect(x, y, x + cardWidth, y + cardHeight, background, border);
        drawGradientRect(x + 1, y + 1, x + cardWidth - 1, y + cardHeight - 1,
                module.isEnabled() ? 0x55351A24 : hovered ? 0x22181222 : 0x18100D17,
                module.isEnabled() ? 0x22181218 : 0x11110D19);

        int toggleX = x + cardWidth - 32;
        int toggleY = y + 14;
        int toggleHeight = 11;
        int moduleTextY = toggleY + ((toggleHeight - getTextHeight()) / 2);
        drawText(module.getName(), x + 12, moduleTextY, module.isEnabled() ? 0xFFFFF3ED : 0xFF9D91A2);
        drawRoundedRect(toggleX, toggleY, toggleX + 22, toggleY + 11, module.isEnabled() ? 0x995D302C : 0x66231E2B);
        drawRoundedRect(module.isEnabled() ? toggleX + 12 : toggleX + 2, toggleY + 2,
                module.isEnabled() ? toggleX + 19 : toggleX + 9, toggleY + 9,
                module.isEnabled() ? 0xFFFF8D6A : 0xFF403747);

        if (hasSettings(module)) {
            drawText(collapsedModules.contains(module) ? "+" : "-", x + cardWidth - 48, moduleTextY, 0xFF8E7182);
        }

        if (!collapsedModules.contains(module)) {
            drawDynamicSettings(module, x + 12, y + 38, cardWidth - 24);
        }
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
            saveGuiState();
        }

        int categoryY = guiY + 58;
        for (ModuleCategory category : ModuleCategory.values()) {
            if (mouseButton == 0 && isInside(mouseX, mouseY, guiX + 10, categoryY - 8, guiX + sidebarWidth - 10, categoryY + 22)) {
                saveContentScroll();
                selectedCategory = category;
                restoreContentScroll();
                saveGuiState();
            }
            categoryY += 38;
        }

        List<Module> modules = moduleManager.getModulesByCategory(selectedCategory);
        int startX = guiX + sidebarWidth + 14;
        int startY = guiY + 48 - contentScroll;
        int cardWidth = guiWidth - sidebarWidth - 42;
        int gap = 10;
        int y = startY;

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int cardHeight = getModuleCardHeight(module);
            int x = startX;

            if (isInsideContentViewport(mouseX, mouseY) && isInside(mouseX, mouseY, x, y, x + cardWidth, y + cardHeight)) {
                if (mouseButton == 1 && hasSettings(module)) {
                    if (collapsedModules.contains(module)) {
                        collapsedModules.remove(module);
                    } else {
                        collapsedModules.add(module);
                    }
                    contentScroll = clampInt(contentScroll, 0, getMaxContentScroll());
                    saveGuiState();
                    return;
                } else if (mouseButton == 0) {
                    if (mouseY <= y + 36) {
                        module.toggle();
                        return;
                    } else if (hasSettings(module) && !collapsedModules.contains(module)) {
                        handleDynamicClick(module, x + 12, y + 38, cardWidth - 24, mouseX, mouseY);
                        return;
                    }
                }
            }

            y += cardHeight + gap;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) {
            dragging = false;
            draggingScrollbar = false;
            draggingSettingModule = null;
            draggingSettingField = null;
            saveGuiState();
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void onGuiClosed() {
        dragging = false;
        draggingScrollbar = false;
        draggingSettingModule = null;
        draggingSettingField = null;
        saveGuiState();
    }

    private int getModuleCardHeight(Module module) {
        if (collapsedModules.contains(module)) {
            return 40;
        }

        return 40 + getSettingCount(module) * 22;
    }

    private List<Field> getAllSettingFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<Field>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Setting.class)) {
                    fields.add(field);
                }
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    private void drawDynamicSettings(Module module, int x, int y, int width) {
        int currentY = y;

        for (Field field : getAllSettingFields(module.getClass())) {
            try {
                field.setAccessible(true);
                Setting setting = field.getAnnotation(Setting.class);
                String displayName = setting.name().isEmpty() ? field.getName() : setting.name();
                Object value = field.get(module);

                if (value instanceof Boolean) {
                    drawSettingToggle(displayName, (Boolean) value, module.isEnabled(), x, currentY, width);
                    currentY += 22;
                } else if (value instanceof Number) {
                    drawSlider(displayName, getSettingUnit(value), ((Number) value).doubleValue(), setting.min(), setting.max(), x, currentY, width);
                    currentY += 22;
                }
            } catch (Exception e) {
                System.err.println("[ClickGui] Failed to draw setting: " + field.getName());
                e.printStackTrace();
            }
        }
    }

    private void drawSettingToggle(String label, boolean enabled, boolean moduleEnabled, int x, int y, int width) {
        int toggleX = x + width - 28;
        boolean active = moduleEnabled && enabled;
        drawText(label, x, y + 3, 0xFFB8AEBE);
        drawRoundedRect(toggleX, y + 1, toggleX + 22, y + 12, active ? 0x995D302C : 0x66231E2B);
        drawRoundedRect(active ? toggleX + 12 : toggleX + 2, y + 3,
                active ? toggleX + 19 : toggleX + 9, y + 10,
                active ? 0xFFFF8D6A : 0xFF403747);
    }

    private void drawSlider(String label, String unit, double value, double min, double max, int x, int y, int width) {
        int trackX = x + 132;
        int trackWidth = width - 137;
        double percent = (value - min) / (max - min);
        percent = Math.max(0.0D, Math.min(1.0D, percent));
        int fillWidth = (int) Math.round(trackWidth * percent);

        drawText(label, x, y, 0xFFB8AEBE);
        drawText(formatSliderValue(value, unit), x + 102, y, 0xFF8E7182);
        drawRoundedRect(trackX, y + 4, trackX + trackWidth, y + 8, 0x6633263B);
        drawRoundedRect(trackX, y + 4, trackX + fillWidth, y + 8, 0xFFFF8D6A);
        drawRoundedRect(trackX + fillWidth - 2, y + 2, trackX + fillWidth + 2, y + 10, 0xFFFFB09A);
    }

    private String formatSliderValue(double value, String unit) {
        if (value == (long) value) {
            return String.format(Locale.US, "%d%s", (long) value, unit);
        }

        return String.format(Locale.US, "%.1f%s", value, unit);
    }

    private void handleDynamicClick(Module module, int x, int y, int width, int mouseX, int mouseY) {
        int currentY = y;

        for (Field field : getAllSettingFields(module.getClass())) {
            try {
                field.setAccessible(true);
                Object value = field.get(module);

                if (value instanceof Boolean) {
                    if (isInside(mouseX, mouseY, x, currentY, x + width, currentY + 16)) {
                        module.updateSetting(field.getName(), !((Boolean) value));
                        return;
                    }
                    currentY += 22;
                } else if (value instanceof Number) {
                    int trackX = x + 132;
                    int trackWidth = width - 137;
                    if (isInside(mouseX, mouseY, trackX - 4, currentY - 2, trackX + trackWidth + 4, currentY + 14)) {
                        draggingSettingModule = module;
                        draggingSettingField = field;
                        draggingSliderX = trackX;
                        draggingSliderWidth = trackWidth;
                        updateNumberSettingSlider(mouseX);
                        return;
                    }
                    currentY += 22;
                }
            } catch (Exception e) {
                System.err.println("[ClickGui] Failed to handle setting click: " + field.getName());
                e.printStackTrace();
            }
        }
    }

    private void updateSettingSliderDrag(int mouseX) {
        if (draggingSettingModule == null || draggingSettingField == null) {
            return;
        }

        if (!Mouse.isButtonDown(0)) {
            draggingSettingModule = null;
            draggingSettingField = null;
            return;
        }

        updateNumberSettingSlider(mouseX);
    }

    private void updateNumberSettingSlider(int mouseX) {
        double percent = (mouseX - draggingSliderX) / (double) Math.max(1, draggingSliderWidth);
        percent = Math.max(0.0D, Math.min(1.0D, percent));
        Setting setting = draggingSettingField.getAnnotation(Setting.class);
        double rawValue = setting.min() + percent * (setting.max() - setting.min());
        double finalValue = roundToStep(rawValue, setting.step());
        if (setting.step() > 0.0D) {
            double scale = 1.0D / setting.step();
            if (scale == (long) scale) {
                finalValue = Math.round(finalValue * scale) / scale;
            }
        }

        draggingSettingModule.updateSetting(draggingSettingField.getName(), getSliderValue(draggingSettingField, finalValue));
    }

    private double roundToStep(double value, double step) {
        return Math.round(value / step) * step;
    }

    private void drawTextCentered(String text, int x, int y, int width, int color) {
        int textWidth = mc.fontRendererObj.getStringWidth(text);
        drawText(text, x + (width - textWidth) / 2, y, color);
    }

    private boolean hasSettings(Module module) {
        return getSettingCount(module) > 0;
    }

    private int getSettingCount(Module module) {
        return getAllSettingFields(module.getClass()).size();
    }

    private Object getSliderValue(Field field, double value) {
        Class<?> type = field.getType();

        if (type == int.class || type == Integer.class) {
            return (int) value;
        }

        if (type == long.class || type == Long.class) {
            return (long) value;
        }

        if (type == float.class || type == Float.class) {
            return (float) value;
        }

        if (type == short.class || type == Short.class) {
            return (short) value;
        }

        if (type == byte.class || type == Byte.class) {
            return (byte) value;
        }

        return value;
    }

    private String getSettingUnit(Object value) {
        return value instanceof Long ? "ms" : "";
    }

    private void restoreCollapsedModules() {
        collapsedModules.clear();
        if (!savedCollapsedModulesInitialized) {
            collapseSettingsByDefault();
            saveCollapsedModules();
            savedCollapsedModulesInitialized = true;
            return;
        }

        for (Module module : moduleManager.getModules()) {
            if (savedCollapsedModuleNames.contains(module.getName())) {
                collapsedModules.add(module);
            }
        }
    }

    private void collapseSettingsByDefault() {
        for (Module module : moduleManager.getModules()) {
            if (hasSettings(module)) {
                collapsedModules.add(module);
            }
        }
    }

    private void saveGuiState() {
        savedPosition = true;
        savedGuiX = guiX;
        savedGuiY = guiY;
        savedSelectedCategory = selectedCategory;
        saveContentScroll();
        saveCollapsedModules();
    }

    private void saveContentScroll() {
        savedScrollByCategory.put(selectedCategory, contentScroll);
    }

    private void restoreContentScroll() {
        Integer savedScroll = savedScrollByCategory.get(selectedCategory);
        contentScroll = savedScroll == null ? 0 : clampInt(savedScroll, 0, getMaxContentScroll());
    }

    private void saveCollapsedModules() {
        savedCollapsedModuleNames.clear();
        for (Module module : collapsedModules) {
            savedCollapsedModuleNames.add(module.getName());
        }
        savedCollapsedModulesInitialized = true;
    }

    private String formatCategoryName(ModuleCategory category) {
        String name = category.name().toLowerCase(Locale.US);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private void drawBorderedRect(int left, int top, int right, int bottom, int fillColor, int borderColor) {
        drawRoundedRect(left, top, right, bottom, borderColor);
        drawRoundedRect(left + 1, top + 1, right - 1, bottom - 1, fillColor);
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

    private int getTextHeight() {
        return mc.fontRendererObj.FONT_HEIGHT;
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
        int height = 12;

        for (Module module : modules) {
            height += getModuleCardHeight(module) + 10;
        }

        return Math.max(52, height);
    }

    private int getContentViewportHeight() {
        return guiHeight - 58;
    }

    private int getMaxContentScroll() {
        return Math.max(0, getContentHeight() - getContentViewportHeight());
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
        } else if (category == ModuleCategory.MOVEMENT) {
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
        } else {
            Gui.drawRect(x - 5, y, x - 1, y + 3, 0xFFFFB09A);
            Gui.drawRect(x - 4, y + 3, x - 1, y + 6, color);
            Gui.drawRect(x - 2, y + 5, x + 1, y + 8, color);
            Gui.drawRect(x, y + 7, x + 3, y + 10, color);
            Gui.drawRect(x + 2, y + 9, x + 7, y + 12, color);
            Gui.drawRect(x + 5, y + 11, x + 8, y + 14, 0xFFFF8D6A);
        }
    }

    private boolean isInside(int mouseX, int mouseY, int left, int top, int right, int bottom) {
        return mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
    }
}