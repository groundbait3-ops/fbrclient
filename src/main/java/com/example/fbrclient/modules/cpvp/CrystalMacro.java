package com.example.fbrclient.modules.cpvp;

import com.example.fbrclient.modules.Module;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class CrystalMacro extends Module {
    private MinecraftClient mc;
    private int tickCounter = 0;
    private static final int TICKS_PER_CLICK = 1; // 20 ticks/sec = 20 CPS (50ms per action)
    private List<BlockPos> placedCrystals = new ArrayList<>();
    private int currentPhase = 0; // 0 = place, 1 = break
    
    public CrystalMacro() {
        super("Crystal Macro", "CPVP");
        this.mc = MinecraftClient.getInstance();
    }
    
    @Override
    public void onEnable() {
        System.out.println("Crystal Macro enabled - 20 CPS");
        tickCounter = 0;
        placedCrystals.clear();
        currentPhase = 0;
    }
    
    @Override
    public void onDisable() {
        System.out.println("Crystal Macro disabled");
        placedCrystals.clear();
    }
    
    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        
        tickCounter++;
        
        // Execute at 20 CPS (every tick = 20 TPS, so every tick for 20 CPS)
        if (tickCounter >= TICKS_PER_CLICK) {
            tickCounter = 0;
            
            if (currentPhase == 0) {
                // Place phase
                placeCrystal();
                currentPhase = 1;
            } else {
                // Break phase
                breakCrystal();
                currentPhase = 0;
            }
        }
    }
    
    private void placeCrystal() {
        if (mc.player == null || mc.world == null) return;
        
        // Check if holding end crystal
        if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL) {
            // Try to find and switch to end crystal
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getStack(i).getItem() == Items.END_CRYSTAL) {
                    mc.player.getInventory().selectedSlot = i;
                    break;
                }
            }
            return;
        }
        
        // Find valid obsidian/bedrock position
        BlockPos placePos = findPlacePosition();
        if (placePos == null) return;
        
        // Create hit result
        BlockHitResult hitResult = new BlockHitResult(
            Vec3d.ofCenter(placePos).add(0, 1, 0),
            Direction.UP,
            placePos,
            false
        );
        
        // Send place packet
        PlayerInteractBlockC2SPacket packet = new PlayerInteractBlockC2SPacket(
            Hand.MAIN_HAND,
            hitResult,
            mc.world.getTime()
        );
        
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(packet);
            placedCrystals.add(placePos.up());
        }
    }
    
    private void breakCrystal() {
        if (mc.player == null || mc.world == null) return;
        
        // Find nearest crystal
        EndCrystalEntity nearestCrystal = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity crystal) {
                double distance = mc.player.squaredDistanceTo(crystal);
                if (distance < nearestDistance && distance < 36) { // 6 block range
                    nearestDistance = distance;
                    nearestCrystal = crystal;
                }
            }
        }
        
        if (nearestCrystal != null) {
            // Attack the crystal
            PlayerInteractEntityC2SPacket attackPacket = PlayerInteractEntityC2SPacket.attack(
                nearestCrystal,
                mc.player.isSneaking()
            );
            
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(attackPacket);
            }
        }
    }
    
    private BlockPos findPlacePosition() {
        if (mc.player == null || mc.world == null) return null;
        
        BlockPos playerPos = mc.player.getBlockPos();
        
        // Search for valid crystal placement positions
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -2; y <= 2; y++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    
                    // Check if position is obsidian or bedrock
                    if ((mc.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN ||
                         mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK) &&
                        mc.world.isAir(pos.up()) &&
                        mc.world.isAir(pos.up(2))) {
                        
                        // Check if no crystal already exists
                        boolean hasCrystal = false;
                        for (Entity entity : mc.world.getEntities()) {
                            if (entity instanceof EndCrystalEntity) {
                                BlockPos crystalPos = entity.getBlockPos();
                                if (crystalPos.equals(pos.up())) {
                                    hasCrystal = true;
                                    break;
                                }
                            }
                        }
                        
                        if (!hasCrystal) {
                            return pos;
                        }
                    }
                }
            }
        }
        
        return null;
    }
}
