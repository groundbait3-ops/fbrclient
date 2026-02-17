package com.example.fbrclient.modules.donut;

import com.example.fbrclient.modules.Module;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerESP extends Module {
    private MinecraftClient mc;
    private static final int RANGE = 1000; // 1000 block range
    private Map<PlayerEntity, Float> playerHues = new HashMap<>();
    private float globalHue = 0.0f;
    
    public PlayerESP() {
        super("Player ESP", "DONUT");
        this.mc = MinecraftClient.getInstance();
    }
    
    @Override
    public void onEnable() {
        System.out.println("Player ESP enabled - Rainbow highlighting within 1000 blocks");
        playerHues.clear();
        globalHue = 0.0f;
    }
    
    @Override
    public void onDisable() {
        System.out.println("Player ESP disabled");
        playerHues.clear();
    }
    
    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        
        // Update rainbow hue (cycles through full spectrum)
        globalHue += 0.5f; // Speed of rainbow cycle
        if (globalHue >= 360.0f) {
            globalHue = 0.0f;
        }
        
        // Assign unique hues to players
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue; // Don't highlight self
            
            double distance = mc.player.distanceTo(player);
            if (distance <= RANGE) {
                if (!playerHues.containsKey(player)) {
                    // Assign a hue offset based on player UUID
                    float hueOffset = (player.getUuid().hashCode() % 360);
                    playerHues.put(player, hueOffset);
                }
            } else {
                playerHues.remove(player); // Remove if out of range
            }
        }
    }
    
    // This method should be called from a render event
    public void renderESP(MatrixStack matrices, float tickDelta) {
        if (mc.player == null || mc.world == null) return;
        
        // Setup rendering
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        // Get camera position
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        
        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        
        // Render ESP for each player
        for (Map.Entry<PlayerEntity, Float> entry : playerHues.entrySet()) {
            PlayerEntity player = entry.getKey();
            float hueOffset = entry.getValue();
            
            if (player == mc.player) continue;
            
            double distance = mc.player.distanceTo(player);
            if (distance > RANGE) continue;
            
            // Calculate rainbow color
            float currentHue = (globalHue + hueOffset) % 360.0f;
            Color color = Color.getHSBColor(currentHue / 360.0f, 1.0f, 1.0f);
            float red = color.getRed() / 255.0f;
            float green = color.getGreen() / 255.0f;
            float blue = color.getBlue() / 255.0f;
            float alpha = 0.5f;
            
            // Get player bounding box
            Box box = player.getBoundingBox();
            
            // Render filled box
            renderFilledBox(matrices, buffer, box, red, green, blue, alpha * 0.3f);
            
            // Render outline box
            renderOutlineBox(matrices, buffer, box, red, green, blue, alpha);
            
            // Render line from player to target
            renderTracerLine(matrices, buffer, player, red, green, blue, alpha);
        }
        
        matrices.pop();
        
        // Cleanup
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
    
    private void renderFilledBox(MatrixStack matrices, BufferBuilder buffer, Box box, float r, float g, float b, float a) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        
        // Bottom face
        buffer.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(r, g, b, a).next();
        
        Tessellator.getInstance().draw();
        
        buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        
        // Top face
        buffer.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(r, g, b, a).next();
        
        Tessellator.getInstance().draw();
        
        buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        
        // North face
        buffer.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(r, g, b, a).next();
        
        Tessellator.getInstance().draw();
        
        buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        
        // South face
        buffer.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(r, g, b, a).next();
        
        Tessellator.getInstance().draw();
        
        buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        
        // West face
        buffer.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(r, g, b, a).next();
        
        Tessellator.getInstance().draw();
        
        buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        
        // East face
        buffer.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(r, g, b, a).next();
        
        Tessellator.getInstance().draw();
    }
    
    private void renderOutlineBox(MatrixStack matrices, BufferBuilder buffer, Box box, float r, float g, float b, float a) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        // Bottom square
        buffer.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(r, g, b, a).next();
        
        buffer.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(r, g, b, a).next();
        
        buffer.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(r, g, b, a).next();
        
        buffer.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(r, g, b, a).next();
        
        // Top square
        buffer.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(r, g, b, a).next();
        
        buffer.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(r, g, b, a).next();
        
        buffer.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(r, g, b, a).next();
        
        buffer.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(r, g, b, a).next();
        
        // Vertical lines
        buffer.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(r, g, b, a).next();
        
        buffer.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(r, g, b, a).next();
        
        buffer.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(r, g, b, a).next();
        
        buffer.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(r, g, b, a).next();
        buffer.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(r, g, b, a).next();
        
        Tessellator.getInstance().draw();
    }
    
    private void renderTracerLine(MatrixStack matrices, BufferBuilder buffer, PlayerEntity player, float r, float g, float b, float a) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        Vec3d playerPos = player.getPos();
        Vec3d eyePos = mc.player.getEyePos();
        
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        buffer.vertex(matrix, (float)eyePos.x, (float)eyePos.y, (float)eyePos.z).color(r, g, b, a * 0.7f).next();
        buffer.vertex(matrix, (float)playerPos.x, (float)(playerPos.y + player.getHeight() / 2), (float)playerPos.z).color(r, g, b, a * 0.7f).next();
        
        Tessellator.getInstance().draw();
    }
    
    public Map<PlayerEntity, Float> getTrackedPlayers() {
        return new HashMap<>(playerHues);
    }
    
    public int getRange() {
        return RANGE;
    }
}
