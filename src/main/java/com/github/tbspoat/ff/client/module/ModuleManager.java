package com.github.tbspoat.ff.client.module;

import com.github.tbspoat.ff.client.module.impl.movement.Sprint;
import com.github.tbspoat.ff.client.module.impl.movement.SprintReset;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {

        // REGISTER MODULES HERE
        add(new Sprint());
        add(new SprintReset());
    }

    public void add(Module m) {
        modules.add(m);
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(ModuleCategory category) {

        List<Module> out = new ArrayList<>();

        for (Module m : modules) {
            if (m.getCategory() == category) {
                out.add(m);
            }
        }

        return out;
    }

    public void onTick() {

        for (Module m : modules) {

            if (m.isEnabled()) {
                m.onTick();
            }
        }
    }
}