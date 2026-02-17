package com.example.fbrclient;

import com.example.fbrclient.gui.ModListScreen;
import com.example.fbrclient.modules.Module;
import com.example.fbrclient.modules.cpvp.*;
import com.example.fbrclient.modules.donut.*;
import com.example.fbrclient.modules.performance.*;
import com.example.fbrclient.render.ESPRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class FBRClient implements ClientModInitializer {
    public static final String MOD_ID = "fbrclient";
    public static final String MOD_NAME = "FBR CLIENT";
    public static final String VERSION = "1.21.10";
    
    // Modules
    private static final List<Module> modules = new ArrayList<>();
    
    // CPVP Modules
    private static AutoAnchor autoAnchor;
    private static CrystalMacro crystalMacro;
    private static AutoTotem autoTotem;
    
    // DONUT Modules
    private static ShulkerOrder shulkerOrder;
    private static BetterChunkFinder chunkFinder;
    private static FarmFinder farmFinder;
    private static StashFinder stashFinder;
    private static ElytraFinder elytraFinder;
    private static PlayerESP playerESP;
    
    // PERFORMANCE Modules
    private static UltimateCulling ultimateCulling;
    private static FPSBoost fpsBoost;
    
    // Keybinding
    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        System.out.println("[FBR CLIENT] Initializing v" + VERSION);
        
        // Initialize modules
        initializeModules();
        
        // Initialize ESP renderer
        ESPRenderer.initialize();
        if (playerESP != null) {
            ESPRenderer.setPlayerESPModule(playerESP);
        }
        
        // Register keybinding
        registerKeybindings();
        
        // Register tick events
        registerTickEvents();
        
        System.out.println("[FBR CLIENT] Initialization complete! " + modules.size() + " modules loaded.");
    }
    
    private void initializeModules() {
        // CPVP Modules
        autoAnchor = new AutoAnchor();
        modules.add(autoAnchor);
        
        crystalMacro = new CrystalMacro();
        modules.add(crystalMacro);
        
        autoTotem = new AutoTotem();
        modules.add(autoTotem);
        
        // DONUT Modules
        shulkerOrder = new ShulkerOrder();
        modules.add(shulkerOrder);
        
        chunkFinder = new BetterChunkFinder();
        modules.add(chunkFinder);
        
        farmFinder = new FarmFinder();
        modules.add(farmFinder);
        
        stashFinder = new StashFinder();
        modules.add(stashFinder);
        
        elytraFinder = new ElytraFinder();
        modules.add(elytraFinder);
        
        playerESP = new PlayerESP();
        modules.add(playerESP);
        
        // PERFORMANCE Modules
        ultimateCulling = new UltimateCulling();
        modules.add(ultimateCulling);
        
        fpsBoost = new FPSBoost();
        modules.add(fpsBoost);
        
        System.out.println("[FBR CLIENT] Loaded " + modules.size() + " modules");
    }
    
    private void registerKeybindings() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fbrclient.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.fbrclient.keys"
        ));
    }
    
    private void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Check for GUI keybind
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ModListScreen());
                }
            }
            
            // Tick all enabled modules
            for (Module module : modules) {
                if (module.isEnabled()) {
                    module.onTick();
                }
            }
        });
    }
    
    // Getters for modules
    public static AutoAnchor getAutoAnchor() { return autoAnchor; }
    public static CrystalMacro getCrystalMacro() { return crystalMacro; }
    public static AutoTotem getAutoTotem() { return autoTotem; }
    public static ShulkerOrder getShulkerOrder() { return shulkerOrder; }
    public static BetterChunkFinder getChunkFinder() { return chunkFinder; }
    public static FarmFinder getFarmFinder() { return farmFinder; }
    public static StashFinder getStashFinder() { return stashFinder; }
    public static ElytraFinder getElytraFinder() { return elytraFinder; }
    public static PlayerESP getPlayerESP() { return playerESP; }
    public static UltimateCulling getUltimateCulling() { return ultimateCulling; }
    public static FPSBoost getFPSBoost() { return fpsBoost; }
    
    public static List<Module> getAllModules() {
        return new ArrayList<>(modules);
    }
}
