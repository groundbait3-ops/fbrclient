package com.example.fbrclient.render;

import com.example.fbrclient.modules.donut.PlayerESP;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.util.math.MatrixStack;

public class ESPRenderer {
    private static PlayerESP playerESPModule = null;
    
    public static void initialize() {
        // Register world render event
        WorldRenderEvents.AFTER_ENTITIES.register(ESPRenderer::renderESP);
    }
    
    private static void renderESP(WorldRenderContext context) {
        if (playerESPModule != null && playerESPModule.isEnabled()) {
            MatrixStack matrices = context.matrixStack();
            float tickDelta = context.tickDelta();
            
            playerESPModule.renderESP(matrices, tickDelta);
        }
    }
    
    public static void setPlayerESPModule(PlayerESP module) {
        playerESPModule = module;
    }
    
    public static PlayerESP getPlayerESPModule() {
        return playerESPModule;
    }
}
