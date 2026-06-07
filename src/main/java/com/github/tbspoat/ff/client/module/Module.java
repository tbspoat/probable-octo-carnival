package com.github.tbspoat.ff.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.lang.reflect.Field;

public abstract class Module {

    private final String name;
    private final ModuleCategory category;
    private boolean enabled;

    public Module(String name, ModuleCategory category) {
        this.name = name;
        this.category = category;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;
        System.out.println("[Module Manager] " + name + " was turned " + (enabled ? "ON" : "OFF"));
        sendInGameToggleMessage(enabled);

        if (enabled) onEnable();
        else onDisable();
    }

    private void sendInGameToggleMessage(boolean enabled) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) {
            return;
        }

        String prefix = EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "ff" + EnumChatFormatting.GRAY + "] ";
        String moduleName = EnumChatFormatting.WHITE + name;
        String state = enabled ? EnumChatFormatting.GREEN + " enabled." : EnumChatFormatting.RED + " disabled.";
        mc.thePlayer.addChatMessage(new ChatComponentText(prefix + moduleName + state));
    }

    public void updateSetting(String fieldName, Object newValue) {
        try {
            Field field = this.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);

            if (!field.isAnnotationPresent(Setting.class)) {
                return;
            }

            Object oldValue = field.get(this);
            if (oldValue != null && oldValue.equals(newValue)) {
                return;
            }

            field.set(this, newValue);

            Setting setting = field.getAnnotation(Setting.class);
            String displayName = setting.name().isEmpty() ? field.getName() : setting.name();
            if (newValue instanceof Boolean) {
                announceSettingToggle(displayName, (Boolean) newValue);
            } else {
                announceSettingValueChange(displayName, newValue);
            }
        } catch (Exception e) {
            System.err.println("[Module Manager] Failed to automatically update setting: " + fieldName);
            e.printStackTrace();
        }
    }

    protected void announceSettingToggle(String settingName, boolean enabled) {
        System.out.println("[Module Manager] " + name + " setting " + settingName + " was turned " + (enabled ? "ON" : "OFF"));
        sendInGameSettingToggleMessage(settingName, enabled);
    }

    private void announceSettingValueChange(String settingName, Object newValue) {
        System.out.println("[Module Manager] " + name + " setting " + settingName + " was set to " + newValue);
        sendInGameSettingValueMessage(settingName, newValue);
    }

    private void sendInGameSettingToggleMessage(String settingName, boolean enabled) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) {
            return;
        }

        String prefix = EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "ff" + EnumChatFormatting.GRAY + "] ";
        String moduleName = EnumChatFormatting.WHITE + name;
        String setting = EnumChatFormatting.GRAY + " > " + EnumChatFormatting.WHITE + settingName;
        String state = enabled ? EnumChatFormatting.GREEN + " enabled." : EnumChatFormatting.RED + " disabled.";
        mc.thePlayer.addChatMessage(new ChatComponentText(prefix + moduleName + setting + state));
    }

    private void sendInGameSettingValueMessage(String settingName, Object newValue) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) {
            return;
        }

        String prefix = EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "ff" + EnumChatFormatting.GRAY + "] ";
        String moduleName = EnumChatFormatting.WHITE + name;
        String setting = EnumChatFormatting.GRAY + " > " + EnumChatFormatting.WHITE + settingName;
        String state = EnumChatFormatting.GRAY + " set to " + EnumChatFormatting.AQUA + newValue.toString();
        mc.thePlayer.addChatMessage(new ChatComponentText(prefix + moduleName + setting + state));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
}
