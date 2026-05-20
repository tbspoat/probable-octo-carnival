package com.github.tbspoat.ff.client.module;

public abstract class Module {

    private final String name;
    private final ModuleCategory category;
    private boolean enabled;

    public Module(String name, ModuleCategory category) {
        this.name = name;
        this.category = category;
    }

    public void toggle() {
        enabled = !enabled;

        if (enabled) onEnable();
        else onDisable();
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