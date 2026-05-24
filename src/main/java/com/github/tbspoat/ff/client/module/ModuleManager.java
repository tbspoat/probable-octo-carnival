package com.github.tbspoat.ff.client.module;

import com.github.tbspoat.ff.client.module.impl.movement.Sprint;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList();

    public ModuleManager() {
        this.add(new Sprint());
    }

    public void add(Module m) {
        this.modules.add(m);
    }

    public List<Module> getModules() {
        return this.modules;
    }

    public <T extends Module> T getModule(Class<T> moduleClass) {
        for (Module m : this.modules) {
            if (moduleClass.isInstance(m)) {
                return moduleClass.cast(m);
            }
        }

        return null;
    }

    public List<Module> getModulesByCategory(ModuleCategory category) {
        List<Module> out = new ArrayList();

        for(Module m : this.modules) {
            if (m.getCategory() == category) {
                out.add(m);
            }
        }

        return out;
    }

    public void onTick() {
        for(Module m : this.modules) {
            if (m.isEnabled()) {
                m.onTick();
            }
        }

    }
}
