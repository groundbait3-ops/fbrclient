package com.example.fbrclient.modules;

public abstract class Module {
    private String name;
    private String category;
    private boolean enabled;
    
    public Module(String name, String category) {
        this.name = name;
        this.category = category;
        this.enabled = false;
    }
    
    public abstract void onEnable();
    public abstract void onDisable();
    public abstract void onTick();
    
    public void toggle() {
        enabled = !enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCategory() {
        return category;
    }
}
