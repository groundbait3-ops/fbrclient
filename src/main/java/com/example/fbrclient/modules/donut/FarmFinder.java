package com.example.fbrclient.modules.donut;

import com.example.fbrclient.modules.Module;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class FarmFinder extends Module {
    private MinecraftClient mc;
    private List<BlockPos> foundFarms = new ArrayList<>();
    private boolean isScanning = false;
    private static final int SCAN_RADIUS = 500; // blocks
    
    public FarmFinder() {
        super("Farm Finder", "DONUT");
        this.mc = MinecraftClient.getInstance();
    }
    
    @Override
    public void onEnable() {
        System.out.println("Farm Finder enabled - Scanning for farms");
        foundFarms.clear();
        isScanning = true;
        startFarmScan();
    }
    
    @Override
    public void onDisable() {
        System.out.println("Farm Finder disabled");
        isScanning = false;
    }
    
    @Override
    public void onTick() {
        // Scanning handled in background thread
    }
    
    private void startFarmScan() {
        if (mc.player == null || mc.world == null) return;
        
        mc.player.sendMessage(Text.literal("§a[Farm Finder] Scanning for farms..."), false);
        
        new Thread(() -> {
            try {
                BlockPos playerPos = mc.player.getBlockPos();
                int farmsFound = 0;
                
                // Scan for farm indicators
                for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x += 5) {
                    for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z += 5) {
                        for (int y = -20; y <= 20; y += 5) {
                            if (!isScanning) break;
                            
                            BlockPos scanPos = playerPos.add(x, y, z);
                            
                            if (isFarmLocation(scanPos)) {
                                foundFarms.add(scanPos);
                                farmsFound++;
                                
                                final int distance = (int)Math.sqrt(x * x + z * z);
                                mc.execute(() -> {
                                    mc.player.sendMessage(
                                        Text.literal(String.format("§e[Farm Finder] Found farm at %d, %d, %d (Distance: %d blocks)",
                                            scanPos.getX(), scanPos.getY(), scanPos.getZ(), distance)),
                                        false
                                    );
                                });
                            }
                            
                            Thread.sleep(5);
                        }
                    }
                }
                
                mc.execute(() -> {
                    mc.player.sendMessage(
                        Text.literal(String.format("§a[Farm Finder] Scan complete! Found %d farms", farmsFound)),
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
    
    private boolean isFarmLocation(BlockPos pos) {
        if (mc.world == null) return false;
        
        int farmBlockCount = 0;
        
        // Check 5x5 area around position
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos checkPos = pos.add(x, 0, z);
                
                // Count farm-related blocks
                if (mc.world.getBlockState(checkPos).getBlock() == Blocks.WHEAT ||
                    mc.world.getBlockState(checkPos).getBlock() == Blocks.CARROTS ||
                    mc.world.getBlockState(checkPos).getBlock() == Blocks.POTATOES ||
                    mc.world.getBlockState(checkPos).getBlock() == Blocks.BEETROOTS ||
                    mc.world.getBlockState(checkPos).getBlock() == Blocks.PUMPKIN ||
                    mc.world.getBlockState(checkPos).getBlock() == Blocks.MELON ||
                    mc.world.getBlockState(checkPos).getBlock() == Blocks.SUGAR_CANE ||
                    mc.world.getBlockState(checkPos).getBlock() == Blocks.CACTUS ||
                    mc.world.getBlockState(checkPos).getBlock() == Blocks.BAMBOO ||
                    mc.world.getBlockState(checkPos).getBlock() == Blocks.FARMLAND ||
                    mc.world.getBlockState(checkPos).getBlock() == Blocks.COMPOSTER ||
                    mc.world.getBlockState(checkPos).getBlock() == Blocks.HOPPER) {
                    farmBlockCount++;
                }
            }
        }
        
        // If 5 or more farm blocks in area, likely a farm
        return farmBlockCount >= 5;
    }
}
