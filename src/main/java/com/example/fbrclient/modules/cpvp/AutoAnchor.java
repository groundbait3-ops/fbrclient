package com.example.fbrclient.modules.cpvp;

import com.example.fbrclient.modules.Module;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AutoAnchor extends Module {
    private MinecraftClient mc;
    private int totemSlot = 1; // Default hotbar slot (1-9)
    private int tickDelay = 0;
    private static final int GRIM_BYPASS_DELAY = 3; // Ticks between actions to bypass GrimAC
    
    public AutoAnchor() {
        super("Auto Anchor", "CPVP");
        this.mc = MinecraftClient.getInstance();
    }
    
    @Override
    public void onEnable() {
        System.out.println("Auto Anchor enabled - Totem slot: " + totemSlot);
    }
    
    @Override
    public void onDisable() {
        System.out.println("Auto Anchor disabled");
    }
    
    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        
        // GrimAC bypass: Add delay between actions
        if (tickDelay > 0) {
            tickDelay--;
            return;
        }
        
        // Check for nearby respawn anchors
        BlockPos anchorPos = findNearbyAnchor();
        if (anchorPos != null) {
            explodeAnchor(anchorPos);
        }
    }
    
    private BlockPos findNearbyAnchor() {
        if (mc.player == null || mc.world == null) return null;
        
        BlockPos playerPos = mc.player.getBlockPos();
        int range = 5; // Search radius
        
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR) {
                        // Check if anchor is charged
                        int charges = mc.world.getBlockState(pos).get(RespawnAnchorBlock.CHARGES);
                        if (charges > 0) {
                            return pos;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private void explodeAnchor(BlockPos anchorPos) {
        if (mc.player == null || mc.world == null) return;
        
        // Save current slot
        int previousSlot = mc.player.getInventory().selectedSlot;
        
        // Switch to totem slot (convert 1-9 to 0-8)
        int actualSlot = totemSlot - 1;
        
        // Verify totem is in the slot
        ItemStack itemInSlot = mc.player.getInventory().getStack(actualSlot);
        if (itemInSlot.getItem() != Items.TOTEM_OF_UNDYING) {
            System.out.println("No totem found in slot " + totemSlot);
            return;
        }
        
        // GrimAC Bypass technique: Smooth slot switching with delay
        mc.player.getInventory().selectedSlot = actualSlot;
        
        // Wait for GrimAC to register slot change
        tickDelay = GRIM_BYPASS_DELAY;
        
        // Schedule the interaction after delay
        mc.execute(() -> {
            if (tickDelay == 0) {
                performAnchorInteraction(anchorPos, previousSlot);
            }
        });
    }
    
    private void performAnchorInteraction(BlockPos anchorPos, int previousSlot) {
        if (mc.player == null || mc.world == null) return;
        
        // Create block hit result for the anchor
        Vec3d hitVec = Vec3d.ofCenter(anchorPos);
        BlockHitResult hitResult = new BlockHitResult(
            hitVec,
            Direction.UP,
            anchorPos,
            false
        );
        
        // GrimAC Bypass: Send packet directly to simulate legit interaction
        // This bypasses client-side prediction checks
        PlayerInteractBlockC2SPacket packet = new PlayerInteractBlockC2SPacket(
            Hand.MAIN_HAND,
            hitResult,
            mc.world.getTime() // Use world time for sequence number
        );
        
        // Send the packet
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(packet);
        }
        
        // The respawn anchor will explode because:
        // 1. We're in the overworld (not in the nether)
        // 2. We're holding a totem (not glowstone)
        // 3. The anchor has charges
        // This triggers the explosion without needing to charge it
        
        // Add delay before switching back (GrimAC bypass)
        tickDelay = GRIM_BYPASS_DELAY;
        
        // Switch back to previous slot after delay
        mc.execute(() -> {
            try {
                Thread.sleep(GRIM_BYPASS_DELAY * 50); // Convert ticks to ms
                if (mc.player != null) {
                    mc.player.getInventory().selectedSlot = previousSlot;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        System.out.println("Auto Anchor exploded at " + anchorPos);
    }
    
    // Configuration methods
    public void setTotemSlot(int slot) {
        if (slot >= 1 && slot <= 9) {
            this.totemSlot = slot;
            System.out.println("Totem slot set to: " + slot);
        }
    }
    
    public int getTotemSlot() {
        return totemSlot;
    }
}
