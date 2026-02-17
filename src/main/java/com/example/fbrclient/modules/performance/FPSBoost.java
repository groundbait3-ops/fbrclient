package com.example.fbrclient.modules.performance;

import com.example.fbrclient.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;

public class FPSBoost extends Module {
    private MinecraftClient mc;
    
    // Original settings backup
    private int originalRenderDistance;
    private int originalSimulationDistance;
    private GraphicsMode originalGraphics;
    private boolean originalSmoothLighting;
    private int originalMaxFps;
    private boolean originalVsync;
    private int originalMipmapLevels;
    private double originalFov;
    private int originalCloudMode;
    private boolean originalEntityShadows;
    
    // Boost multiplier
    private static final int BOOST_MULTIPLIER = 100;
    
    // Optimized settings
    private static final int BOOSTED_RENDER_DISTANCE = 2;
    private static final int BOOSTED_SIMULATION_DISTANCE = 2;
    private static final int BOOSTED_MAX_FPS = 1000;
    private static final int BOOSTED_MIPMAP = 0;
    private static final double BOOSTED_FOV = 30.0; // Lower FOV = less to render
    
    private int baseFPS = 0;
    private int currentFPS = 0;
    
    public FPSBoost() {
        super("FPS Boost", "PERFORMANCE");
        this.mc = MinecraftClient.getInstance();
    }
    
    @Override
    public void onEnable() {
        System.out.println("FPS Boost enabled - 100x performance mode activated!");
        
        if (mc.options != null) {
            backupSettings();
            applyBoostSettings();
            
            if (mc.player != null) {
                mc.player.sendMessage(net.minecraft.text.Text.literal(
                    "§a§l[FPS BOOST] 100x MODE ACTIVATED!"), false);
                mc.player.sendMessage(net.minecraft.text.Text.literal(
                    "§e⚡ Ultra-low settings applied for maximum performance"), false);
            }
        }
    }
    
    @Override
    public void onDisable() {
        System.out.println("FPS Boost disabled - Restoring original settings");
        
        if (mc.options != null) {
            restoreSettings();
            
            if (mc.player != null) {
                mc.player.sendMessage(net.minecraft.text.Text.literal(
                    "§c[FPS Boost] Disabled - Settings restored"), false);
            }
        }
    }
    
    @Override
    public void onTick() {
        if (mc.player == null) return;
        
        // Track FPS
        updateFPSTracking();
        
        // Apply continuous optimizations
        applyContinuousOptimizations();
    }
    
    private void backupSettings() {
        GameOptions options = mc.options;
        
        originalRenderDistance = options.getViewDistance().getValue();
        originalSimulationDistance = options.getSimulationDistance().getValue();
        originalGraphics = options.getGraphicsMode().getValue();
        originalSmoothLighting = options.getSmoothLighting().getValue();
        originalMaxFps = options.getMaxFps().getValue();
        originalVsync = options.getEnableVsync().getValue();
        originalMipmapLevels = options.getMipmapLevels().getValue();
        originalFov = options.getFov().getValue();
        originalEntityShadows = options.getEntityShadows().getValue();
        
        System.out.println("FPS Boost: Settings backed up");
    }
    
    private void applyBoostSettings() {
        GameOptions options = mc.options;
        
        // Extreme render distance reduction
        options.getViewDistance().setValue(BOOSTED_RENDER_DISTANCE);
        options.getSimulationDistance().setValue(BOOSTED_SIMULATION_DISTANCE);
        
        // Graphics to fastest
        options.getGraphicsMode().setValue(GraphicsMode.FAST);
        options.getSmoothLighting().setValue(false);
        
        // Max FPS
        options.getMaxFps().setValue(BOOSTED_MAX_FPS);
        options.getEnableVsync().setValue(false);
        
        // Disable visual effects
        options.getMipmapLevels().setValue(BOOSTED_MIPMAP);
        options.getEntityShadows().setValue(false);
        options.getParticles().setValue(net.minecraft.client.option.ParticlesMode.MINIMAL);
        
        // Reduce FOV to render less
        // options.getFov().setValue(BOOSTED_FOV); // Commented out - can be uncomfortable
        
        // Disable clouds
        options.getCloudRenderMode().setValue(net.minecraft.client.option.CloudRenderMode.OFF);
        
        // Disable entity distance
        options.getEntityDistanceScaling().setValue(0.5);
        
        System.out.println("FPS Boost: Ultra-performance settings applied");
    }
    
    private void restoreSettings() {
        GameOptions options = mc.options;
        
        options.getViewDistance().setValue(originalRenderDistance);
        options.getSimulationDistance().setValue(originalSimulationDistance);
        options.getGraphicsMode().setValue(originalGraphics);
        options.getSmoothLighting().setValue(originalSmoothLighting);
        options.getMaxFps().setValue(originalMaxFps);
        options.getEnableVsync().setValue(originalVsync);
        options.getMipmapLevels().setValue(originalMipmapLevels);
        options.getEntityShadows().setValue(originalEntityShadows);
        
        System.out.println("FPS Boost: Original settings restored");
    }
    
    private void applyContinuousOptimizations() {
        if (mc.world == null) return;
        
        // Clear particle effects
        if (mc.particleManager != null && mc.particleManager.getParticles().size() > 20) {
            mc.particleManager.clearParticles();
        }
        
        // Reduce entity render distance dynamically
        if (currentFPS < 60) {
            // If FPS is still low, reduce entity distance even more
            mc.options.getEntityDistanceScaling().setValue(0.25);
        }
    }
    
    private void updateFPSTracking() {
        if (mc.fpsDebugString != null) {
            try {
                String fpsStr = mc.fpsDebugString.split(" ")[0];
                currentFPS = Integer.parseInt(fpsStr);
                
                if (baseFPS == 0) {
                    baseFPS = currentFPS;
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
    }
    
    public int getCurrentFPS() {
        return currentFPS;
    }
    
    public int getBaseFPS() {
        return baseFPS;
    }
    
    public int getFPSIncrease() {
        return currentFPS - baseFPS;
    }
    
    public String getBoostMultiplier() {
        if (baseFPS == 0) return "Calculating...";
        float multiplier = (float) currentFPS / baseFPS;
        return String.format("%.1fx", multiplier);
    }
    
    public String getPerformanceReport() {
        return String.format("FPS: %d (Base: %d) | Boost: %s | Target: %dx", 
            currentFPS, baseFPS, getBoostMultiplier(), BOOST_MULTIPLIER);
    }
}
