package com.example.fbrclient.modules.donut;

import com.example.fbrclient.modules.Module;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class StashFinder extends Module {
    private MinecraftClient mc;
    private List<BlockPos> foundStashes = new ArrayList<>();
    private boolean isScanning = false;
    private static final int SCAN_RADIUS = 500;
    
    public StashFinder() {
        super("Stash Finder", "DONUT");
        this.mc = MinecraftClient.getInstance();
    }
    
    @Override
    public void onEnable() {
        System.out.println("Stash Finder enabled - Scanning for hidden stashes");
        foundStashes.clear();
        isScanning = true;
        startStashScan();
    }
    
    @Override
    public void onDisable() {
        System.out.println("Stash Finder disabled");
        isScanning = false;
    }
    
    @Override
    public void onTick() {
        // Scanning handled in background thread
    }
    
    private void startStashScan() {
        if (mc.player == null || mc.world == null) return;
        
        mc.player.sendMessage(Text.literal("§a[Stash Finder] Scanning for stashes..."), false);
        
        new Thread(() -> {
            try {
                BlockPos playerPos = mc.player.getBlockPos();
                int stashesFound = 0;
                
                // Scan for stash indicators
                for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x += 4) {
                    for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z += 4) {
                        for (int y = -60; y <= 100; y += 4) {
                            if (!isScanning) break;
                            
                            BlockPos scanPos = playerPos.add(x, y, z);
                            
                            if (isStashLocation(scanPos)) {
                                foundStashes.add(scanPos);
                                stashesFound++;
                                
                                final int distance = (int)Math.sqrt(x * x + z * z);
                                mc.execute(() -> {
                                    mc.player.sendMessage(
                                        Text.literal(String.format("§c[Stash Finder] Found stash at %d, %d, %d (Distance: %d blocks)",
                                            scanPos.getX(), scanPos.getY(), scanPos.getZ(), distance)),
                                        false
                                    );
                                });
                            }
                            
                            Thread.sleep(3);
                        }
                    }
                }
                
                mc.execute(() -> {
                    mc.player.sendMessage(
                        Text.literal(String.format("§a[Stash Finder] Scan complete! Found %d stashes", stashesFound)),
                        false
                    );
                    isScanning = false;
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                isScanning = false;
            }
        }).start();
    }
    
    private boolean isStashLocation(BlockPos pos) {
        if (mc.world == null) return false;
        
        int valuableCount = 0;
        int storageCount = 0;
        
        // Check 3x3x3 area
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos checkPos = pos.add(x, y, z);
                    
                    // Check for storage blocks
                    if (mc.world.getBlockState(checkPos).getBlock() == Blocks.CHEST ||
                        mc.world.getBlockState(checkPos).getBlock() == Blocks.ENDER_CHEST ||
                        mc.world.getBlockState(checkPos).getBlock() == Blocks.BARREL) {
                        storageCount++;
                    }
                    
                    // Check for shulker boxes (all colors)
                    String blockName = mc.world.getBlockState(checkPos).getBlock().toString();
                    if (blockName.contains("shulker_box")) {
                        storageCount += 2; // Shulkers are more valuable
                    }
                    
                    // Check for valuable blocks
                    if (mc.world.getBlockState(checkPos).getBlock() == Blocks.DIAMOND_BLOCK ||
                        mc.world.getBlockState(checkPos).getBlock() == Blocks.EMERALD_BLOCK ||
                        mc.world.getBlockState(checkPos).getBlock() == Blocks.GOLD_BLOCK ||
                        mc.world.getBlockState(checkPos).getBlock() == Blocks.IRON_BLOCK ||
                        mc.world.getBlockState(checkPos).getBlock() == Blocks.NETHERITE_BLOCK ||
                        mc.world.getBlockState(checkPos).getBlock() == Blocks.BEACON ||
                        mc.world.getBlockState(checkPos).getBlock() == Blocks.ENCHANTING_TABLE ||
                        mc.world.getBlockState(checkPos).getBlock() == Blocks.ANVIL) {
                        valuableCount++;
                    }
                }
            }
        }
        
        // Stash criteria: 2+ storage blocks OR 3+ valuable blocks OR combination
        return storageCount >= 2 || valuableCount >= 3 || (storageCount >= 1 && valuableCount >= 2);
    }
    
    public List<BlockPos> getFoundStashes() {
        return new ArrayList<>(foundStashes);
    }
}
