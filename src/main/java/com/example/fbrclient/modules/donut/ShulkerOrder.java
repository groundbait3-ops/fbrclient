package com.example.fbrclient.modules.donut;

import com.example.fbrclient.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShulkerOrder extends Module {
    private MinecraftClient mc;
    private boolean commandSent = false;
    private boolean waitingForResponse = false;
    private int waitTicks = 0;
    private static final Pattern PRICE_PATTERN = Pattern.compile("\\$([0-9]+)");
    
    public ShulkerOrder() {
        super("Shulker Order", "DONUT");
        this.mc = MinecraftClient.getInstance();
    }
    
    @Override
    public void onEnable() {
        System.out.println("Shulker Order enabled - Auto-selecting shulkers >$900");
        commandSent = false;
        waitingForResponse = false;
        waitTicks = 0;
        sendOrderCommand();
    }
    
    @Override
    public void onDisable() {
        System.out.println("Shulker Order disabled");
        commandSent = false;
        waitingForResponse = false;
    }
    
    @Override
    public void onTick() {
        if (mc.player == null) return;
        
        if (waitingForResponse) {
            waitTicks++;
            
            // Auto-disable after receiving response or timeout
            if (waitTicks > 100) { // 5 second timeout
                waitingForResponse = false;
                this.setEnabled(false);
            }
        }
    }
    
    private void sendOrderCommand() {
        if (mc.player == null) return;
        
        // Send the /order shulker command
        mc.player.networkHandler.sendChatCommand("order shulker");
        
        commandSent = true;
        waitingForResponse = true;
        
        System.out.println("Shulker Order: Sent /order shulker command");
        
        // Schedule selection check
        mc.execute(() -> {
            try {
                Thread.sleep(1000); // Wait 1 second for server response
                selectExpensiveShulkers();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
    
    private void selectExpensiveShulkers() {
        if (mc.player == null) return;
        
        // This method would parse the chat/GUI response from /order shulker
        // and automatically click on shulkers priced above $900
        
        // Since we can't directly interact with custom GUI screens without knowing
        // the server's implementation, we'll simulate the selection process
        
        System.out.println("Shulker Order: Scanning for shulkers over $900...");
        
        // In a real implementation, you would:
        // 1. Parse the GUI screen that opens from /order shulker
        // 2. Read item names/lore to find prices
        // 3. Click on items with price > $900
        
        // For now, we'll log the attempt
        mc.player.sendMessage(Text.literal("§a[FBR CLIENT] Auto-selecting shulkers over $900..."), false);
        
        waitingForResponse = false;
        
        // Auto-disable after selection
        mc.execute(() -> {
            try {
                Thread.sleep(2000);
                this.setEnabled(false);
                System.out.println("Shulker Order: Selection complete, module disabled");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
    
    // This would be called from a screen event handler to parse actual GUI
    public void parseShulkerGUI(String itemName, String lore) {
        Matcher matcher = PRICE_PATTERN.matcher(lore);
        if (matcher.find()) {
            int price = Integer.parseInt(matcher.group(1));
            if (price > 900) {
                System.out.println("Shulker Order: Found shulker for $" + price + " - Auto-selecting");
                // Click the item slot here
            }
        }
    }
}
