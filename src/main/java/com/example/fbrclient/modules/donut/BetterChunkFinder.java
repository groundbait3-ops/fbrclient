package com.example.fbrclient.modules.donut;

import com.example.fbrclient.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;

public class BetterChunkFinder extends Module {
    private MinecraftClient mc;
    private List<ChunkPos> interestingChunks = new ArrayList<>();
    private boolean isScanning = false;
    private int scanProgress = 0;
    private static final int SCAN_RADIUS = 1000; // blocks
    private static final int BEDROCK_LEVEL = -64;
    
    public BetterChunkFinder() {
        super("Better Chunk Finder", "DONUT");
        this.mc = MinecraftClient.getInstance();
    }
    
    @Override
    public void onEnable() {
        System.out.println("Better Chunk Finder enabled - Scanning 1000 block radius");
        interestingChunks.clear();
        isScanning = true;
        scanProgress = 0;
        startChunkScan();
    }
    
    @Override
    public void onDisable() {
        System.out.println("Better Chunk Finder disabled");
        isScanning = false;
    }
    
    @Override
    public void onTick() {
        if (!isScanning || mc.player == null || mc.world == null) return;
        
        // Continue scanning process
        // This is handled in the async thread
    }
    
    private void startChunkScan() {
        if (mc.player == null || mc.world == null) return;
        
        mc.player.sendMessage(Text.literal("§a[Chunk Finder] Starting scan of 1000 block radius..."), false);
        
        // Run scan in background thread to avoid freezing game
        new Thread(() -> {
            try {
                BlockPos playerPos = mc.player.getBlockPos();
                int chunksScanned = 0;
                int totalChunks = (SCAN_RADIUS / 16) * (SCAN_RADIUS / 16);
                
                // Scan chunks in radius
                for (int chunkX = -SCAN_RADIUS / 16; chunkX <= SCAN_RADIUS / 16; chunkX++) {
                    for (int chunkZ = -SCAN_RADIUS / 16; chunkZ <= SCAN_RADIUS / 16; chunkZ++) {
                        if (!isScanning) break;
                        
                        ChunkPos chunkPos = new ChunkPos(
                            (playerPos.getX() >> 4) + chunkX,
                            (playerPos.getZ() >> 4) + chunkZ
                        );
                        
                        // Analyze chunk
                        if (isInterestingChunk(chunkPos)) {
                            interestingChunks.add(chunkPos);
                            mc.execute(() -> {
                                mc.player.sendMessage(
                                    Text.literal(String.format("§e[Chunk Finder] Found interesting chunk at %d, %d", 
                                        chunkPos.x, chunkPos.z)),
                                    false
                                );
                            });
                        }
                        
                        chunksScanned++;
                        scanProgress = (chunksScanned * 100) / totalChunks;
                        
                        // Update progress every 100 chunks
                        if (chunksScanned % 100 == 0) {
                            final int progress = scanProgress;
                            mc.execute(() -> {
                                mc.player.sendMessage(
                                    Text.literal(String.format("§a[Chunk Finder] Scan progress: %d%%", progress)),
                                    false
                                );
                            });
                        }
                        
                        Thread.sleep(10); // Small delay to prevent lag
                    }
                }
                
                // Scan complete
                mc.execute(() -> {
                    mc.player.sendMessage(
                        Text.literal(String.format("§a[Chunk Finder] Scan complete! Found %d interesting chunks", 
                            interestingChunks.size())),
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
    
    private boolean isInterestingChunk(ChunkPos chunkPos) {
        if (mc.world == null) return false;
        
        // Load chunk if not loaded
        Chunk chunk = mc.world.getChunk(chunkPos.x, chunkPos.z);
        if (chunk == null) return false;
        
        // Criteria for "interesting" chunks:
        // 1. Chunks with unusual bedrock patterns
        // 2. Chunks at chunk borders (0, 0 in chunk coordinates)
        // 3. Chunks with multiple biome transitions
        
        boolean hasUnusualBedrock = checkBedrockPattern(chunk, chunkPos);
        boolean isChunkBorder = (chunkPos.x % 16 == 0) || (chunkPos.z % 16 == 0);
        
        return hasUnusualBedrock || isChunkBorder;
    }
    
    private boolean checkBedrockPattern(Chunk chunk, ChunkPos chunkPos) {
        int bedrockBlocks = 0;
        int totalChecked = 0;
        
        // Sample bedrock layer
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos pos = new BlockPos(
                    chunkPos.getStartX() + x,
                    BEDROCK_LEVEL,
                    chunkPos.getStartZ() + z
                );
                
                if (mc.world.getBlockState(pos).isAir()) {
                    bedrockBlocks++;
                }
                totalChecked++;
            }
        }
        
        // If more than 10% of bedrock layer is air, it's interesting
        return (bedrockBlocks * 100 / totalChecked) > 10;
    }
    
    public List<ChunkPos> getInterestingChunks() {
        return new ArrayList<>(interestingChunks);
    }
}
