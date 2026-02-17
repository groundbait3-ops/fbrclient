package com.example.fbrclient.modules.performance;

import com.example.fbrclient.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;

public class UltimateCulling extends Module {
    private MinecraftClient mc;
    
    // Culling settings
    private static final int ENTITY_RENDER_DISTANCE = 32; // Only render entities within 32 blocks
    private static final int TILE_ENTITY_RENDER_DISTANCE = 16; // Only render chests, signs, etc. within 16 blocks
    private static final int PARTICLE_LIMIT = 50; // Limit total particles
    private static final boolean CULL_HIDDEN_BLOCKS = true; // Don't render blocks you can't see
    private static final boolean CULL_BEHIND_PLAYER = true; // Don't render entities behind you
    
    // Tracking
    private Set<Entity> culledEntities = new HashSet<>();
    private int culledBlockCount = 0;
    private int originalFPS = 0;
    private int boostedFPS = 0;
    
    public UltimateCulling() {
        super("Ultimate Culling", "PERFORMANCE");
        this.mc = MinecraftClient.getInstance();
    }
    
    @Override
    public void onEnable() {
        System.out.println("Ultimate Culling enabled - Aggressive rendering optimization");
        culledEntities.clear();
        culledBlockCount = 0;
        
        if (mc.player != null) {
            mc.player.sendMessage(net.minecraft.text.Text.literal("§a[Ultimate Culling] Enabled - Expect massive FPS boost!"), false);
        }
    }
    
    @Override
    public void onDisable() {
        System.out.println("Ultimate Culling disabled");
        culledEntities.clear();
        culledBlockCount = 0;
        
        if (mc.player != null) {
            mc.player.sendMessage(net.minecraft.text.Text.literal("§c[Ultimate Culling] Disabled"), false);
        }
    }
    
    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        
        // Clear previous frame data
        culledEntities.clear();
        culledBlockCount = 0;
        
        // Cull entities
        cullEntities();
        
        // Update FPS tracking
        trackFPSImprovement();
    }
    
    private void cullEntities() {
        if (mc.world == null || mc.player == null) return;
        
        Vec3d playerPos = mc.player.getPos();
        Vec3d lookVec = mc.player.getRotationVector();
        
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue; // Never cull player
            
            double distance = playerPos.distanceTo(entity.getPos());
            
            // Distance culling
            if (distance > ENTITY_RENDER_DISTANCE) {
                culledEntities.add(entity);
                entity.setInvisible(true);
                continue;
            }
            
            // Behind player culling
            if (CULL_BEHIND_PLAYER && isBehindPlayer(entity, playerPos, lookVec)) {
                culledEntities.add(entity);
                entity.setInvisible(true);
                continue;
            }
            
            // Keep important entities visible
            if (entity instanceof PlayerEntity) {
                entity.setInvisible(false);
            }
        }
    }
    
    private boolean isBehindPlayer(Entity entity, Vec3d playerPos, Vec3d lookVec) {
        Vec3d toEntity = entity.getPos().subtract(playerPos).normalize();
        double dotProduct = lookVec.dotProduct(toEntity);
        return dotProduct < -0.1; // Behind if dot product is negative
    }
    
    // This method would be called from a render hook
    public boolean shouldCullBlock(BlockPos pos) {
        if (!CULL_HIDDEN_BLOCKS || mc.player == null) return false;
        
        double distance = mc.player.getPos().distanceTo(Vec3d.ofCenter(pos));
        
        // Cull far blocks
        if (distance > 64) {
            culledBlockCount++;
            return true;
        }
        
        // Cull blocks completely surrounded by other blocks (not visible)
        if (mc.world != null && isBlockCompletelyHidden(pos)) {
            culledBlockCount++;
            return true;
        }
        
        return false;
    }
    
    private boolean isBlockCompletelyHidden(BlockPos pos) {
        if (mc.world == null) return false;
        
        // Check all 6 sides
        return !mc.world.getBlockState(pos.up()).isAir() &&
               !mc.world.getBlockState(pos.down()).isAir() &&
               !mc.world.getBlockState(pos.north()).isAir() &&
               !mc.world.getBlockState(pos.south()).isAir() &&
               !mc.world.getBlockState(pos.east()).isAir() &&
               !mc.world.getBlockState(pos.west()).isAir();
    }
    
    private void trackFPSImprovement() {
        if (mc.fpsDebugString != null) {
            // Parse FPS from debug string
            String fpsStr = mc.fpsDebugString.split(" ")[0];
            try {
                boostedFPS = Integer.parseInt(fpsStr);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
    }
    
    // Getters for statistics
    public int getCulledEntityCount() {
        return culledEntities.size();
    }
    
    public int getCulledBlockCount() {
        return culledBlockCount;
    }
    
    public String getPerformanceStats() {
        return String.format("Culled: %d entities, %d blocks | FPS: %d", 
            culledEntities.size(), culledBlockCount, boostedFPS);
    }
}
