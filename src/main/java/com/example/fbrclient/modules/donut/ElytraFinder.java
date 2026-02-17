package com.example.fbrclient.modules.donut;

import com.example.fbrclient.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ElytraFinder extends Module {
    private MinecraftClient mc;
    private List<BlockPos> foundElytras = new ArrayList<>();
    private boolean isScanning = false;
    private static final int SCAN_RADIUS = 300;
    
    public ElytraFinder() {
        super("Elytra Finder", "DONUT");
        this.mc = MinecraftClient.getInstance();
    }
    
    @Override
    public void onEnable() {
        System.out.println("Elytra Finder enabled - Scanning for elytras");
        foundElytras.clear();
        isScanning = true;
        startElytraScan();
    }
    
    @Override
    public void onDisable() {
        System.out.println("Elytra Finder disabled");
        isScanning = false;
    }
    
    @Override
    public void onTick() {
        if (!isScanning || mc.player == null || mc.world == null) return;
        
        // Continuous scan for entities (more efficient than area scan)
        scanNearbyEntities();
    }
    
    private void startElytraScan() {
        if (mc.player == null || mc.world == null) return;
        
        mc.player.sendMessage(Text.literal("§a[Elytra Finder] Starting scan..."), false);
        scanNearbyEntities();
    }
    
    private void scanNearbyEntities() {
        if (mc.player == null || mc.world == null) return;
        
        int elytrasFound = 0;
        
        // Scan all loaded entities
        for (Entity entity : mc.world.getEntities()) {
            double distance = mc.player.distanceTo(entity);
            
            // Only check entities within range
            if (distance > SCAN_RADIUS) continue;
            
            boolean hasElytra = false;
            BlockPos elytraPos = entity.getBlockPos();
            
            // Check armor stands
            if (entity instanceof ArmorStandEntity armorStand) {
                // Check chest slot for elytra
                if (armorStand.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
                    hasElytra = true;
                }
            }
            
            // Check item frames
            if (entity instanceof ItemFrameEntity itemFrame) {
                if (itemFrame.getHeldItemStack().getItem() == Items.ELYTRA) {
                    hasElytra = true;
                }
            }
            
            // Report if elytra found and not already reported
            if (hasElytra && !foundElytras.contains(elytraPos)) {
                foundElytras.add(elytraPos);
                elytrasFound++;
                
                final int dist = (int)distance;
                final BlockPos pos = elytraPos;
                
                mc.execute(() -> {
                    mc.player.sendMessage(
                        Text.literal(String.format("§d[Elytra Finder] Found elytra at %d, %d, %d (Distance: %d blocks)",
                            pos.getX(), pos.getY(), pos.getZ(), dist)),
                        false
                    );
                });
            }
        }
        
        // Report completion after first scan
        if (elytrasFound > 0) {
            final int total = elytrasFound;
            mc.execute(() -> {
                mc.player.sendMessage(
                    Text.literal(String.format("§a[Elytra Finder] Found %d elytra(s) in loaded chunks", total)),
                    false
                );
            });
        }
    }
    
    public List<BlockPos> getFoundElytras() {
        return new ArrayList<>(foundElytras);
    }
}
