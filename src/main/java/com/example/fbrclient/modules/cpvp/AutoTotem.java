package com.example.fbrclient.modules.cpvp;

import com.example.fbrclient.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
    private MinecraftClient mc;
    private int pauseTicks = 0;
    private static final int PAUSE_DURATION = 40; // 2 seconds = 40 ticks (GrimAC bypass)
    private boolean isPaused = false;
    private double pausedX, pausedY, pausedZ;
    private float pausedYaw, pausedPitch;
    
    public AutoTotem() {
        super("Auto Totem", "CPVP");
        this.mc = MinecraftClient.getInstance();
    }
    
    @Override
    public void onEnable() {
        System.out.println("Auto Totem enabled - 2s movement pause for GrimAC bypass");
    }
    
    @Override
    public void onDisable() {
        System.out.println("Auto Totem disabled");
        isPaused = false;
        pauseTicks = 0;
    }
    
    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        
        // Handle pause duration
        if (isPaused) {
            pauseTicks--;
            
            // Force player to stay in position (GrimAC bypass)
            if (mc.player != null) {
                mc.player.setPosition(pausedX, pausedY, pausedZ);
                mc.player.setYaw(pausedYaw);
                mc.player.setPitch(pausedPitch);
                
                // Cancel all movement
                mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
            }
            
            if (pauseTicks <= 0) {
                isPaused = false;
                System.out.println("Auto Totem: Movement resumed");
            }
            return;
        }
        
        // Check if totem is in offhand
        ItemStack offhandItem = mc.player.getOffHandStack();
        if (offhandItem.getItem() == Items.TOTEM_OF_UNDYING) {
            return; // Already have totem equipped
        }
        
        // Find totem in inventory
        int totemSlot = findTotemInInventory();
        if (totemSlot == -1) {
            return; // No totem found
        }
        
        // Start GrimAC bypass pause
        startPause();
        
        // Swap totem to offhand
        swapToOffhand(totemSlot);
    }
    
    private void startPause() {
        if (mc.player == null) return;
        
        isPaused = true;
        pauseTicks = PAUSE_DURATION;
        
        // Save current position and rotation
        pausedX = mc.player.getX();
        pausedY = mc.player.getY();
        pausedZ = mc.player.getZ();
        pausedYaw = mc.player.getYaw();
        pausedPitch = mc.player.getPitch();
        
        System.out.println("Auto Totem: Pausing movement for 2 seconds (GrimAC bypass)");
    }
    
    private int findTotemInInventory() {
        if (mc.player == null) return -1;
        
        PlayerInventory inventory = mc.player.getInventory();
        
        // Check hotbar first (slots 0-8)
        for (int i = 0; i < 9; i++) {
            if (inventory.getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        
        // Check rest of inventory (slots 9-35)
        for (int i = 9; i < 36; i++) {
            if (inventory.getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        
        return -1;
    }
    
    private void swapToOffhand(int slot) {
        if (mc.player == null || mc.interactionManager == null) return;
        
        // GrimAC Bypass: Use proper inventory interaction
        // This simulates a legitimate inventory click
        
        // Slot IDs for proper packet sending:
        // 0-8: Hotbar
        // 9-35: Main inventory
        // 45: Offhand
        
        int windowSlot = slot < 9 ? slot + 36 : slot; // Convert to window slot
        
        // Pick up the totem
        mc.interactionManager.clickSlot(
            mc.player.currentScreenHandler.syncId,
            windowSlot,
            0,
            SlotActionType.PICKUP,
            mc.player
        );
        
        // Place in offhand (slot 45)
        mc.interactionManager.clickSlot(
            mc.player.currentScreenHandler.syncId,
            45,
            0,
            SlotActionType.PICKUP,
            mc.player
        );
        
        // If something was in offhand, put it back
        if (mc.player.currentScreenHandler.getCursorStack().getItem() != Items.AIR) {
            mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                windowSlot,
                0,
                SlotActionType.PICKUP,
                mc.player
            );
        }
        
        System.out.println("Auto Totem: Equipped totem from slot " + slot);
    }
}
