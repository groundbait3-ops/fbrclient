package com.example.fbrclient;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ModListScreen extends Screen {
    private final List<ModInfo> mods = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int ITEM_HEIGHT = 35;
    private static final int VISIBLE_ITEMS = 8;
    private int tickCounter = 0;
    
    // Dropdown menu states
    private boolean cpvpExpanded = false;
    private boolean donutExpanded = false;
    private boolean performanceExpanded = false;
    private boolean changesExpanded = false;
    private final List<String> cpvpItems = new ArrayList<>();
    private final List<String> donutItems = new ArrayList<>();
    private final List<String> performanceItems = new ArrayList<>();
    private final List<String> changesItems = new ArrayList<>();
    
    // Module states and settings
    private boolean autoAnchorEnabled = false;
    private int autoAnchorSlot = 1; // Default totem slot
    private boolean showSlotMenu = false;
    private int slotMenuX = 0;
    private int slotMenuY = 0;

    public ModListScreen() {
        super(Text.literal("FBR CLIENT"));
        loadMods();
        initializeDropdownItems();
    }
    
    private void initializeDropdownItems() {
        // CPVP dropdown items
        cpvpItems.add("Kill Aura");
        cpvpItems.add("Velocity");
        cpvpItems.add("Auto Clicker");
        cpvpItems.add("Reach");
        cpvpItems.add("Criticals");
        cpvpItems.add("Auto Anchor");
        cpvpItems.add("Crystal Macro");
        cpvpItems.add("Auto Totem");
        
        // DONUT dropdown items
        donutItems.add("Flight");
        donutItems.add("Speed");
        donutItems.add("No Fall");
        donutItems.add("Jesus (Water Walk)");
        donutItems.add("Spider (Wall Climb)");
        donutItems.add("Shulker Order");
        donutItems.add("Better Chunk Finder");
        donutItems.add("Farm Finder");
        donutItems.add("Stash Finder");
        donutItems.add("Elytra Finder");
        donutItems.add("Player ESP");
        
        // PERFORMANCE dropdown items
        performanceItems.add("Ultimate Culling"); // NEW - Aggressive culling
        performanceItems.add("FPS Boost"); // NEW - 100x performance mode
        performanceItems.add("Chunk Optimizer");
        performanceItems.add("Entity Culling");
        performanceItems.add("Smooth Camera");
        performanceItems.add("Reduce Particles");
        
        // Changes & Bug Fixes items
        changesItems.add("v1.21.10 - Fixed velocity bug");
        changesItems.add("v1.21.9 - Added new Kill Aura modes");
        changesItems.add("v1.21.8 - Performance improvements");
    }

    private void loadMods() {
        // Get all loaded mods from Fabric Loader
        for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
            String id = container.getMetadata().getId();
            String name = container.getMetadata().getName();
            String version = container.getMetadata().getVersion().getFriendlyString();
            String description = container.getMetadata().getDescription();
            
            mods.add(new ModInfo(id, name, version, description));
        }
    }

    private void generateStars() {
        Random random = new Random();
        for (int i = 0; i < 150; i++) {
            stars.add(new Star(
                random.nextInt(10000) / 10000f,
                random.nextInt(10000) / 10000f,
                random.nextFloat() * 2 + 1,
                random.nextFloat() * 0.5f + 0.5f
            ));
        }
    }

    @Override
    protected void init() {
        // Krypton-style close button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Close"),
                button -> this.close()
        ).dimensions(this.width / 2 - 60, this.height - 35, 120, 24).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        tickCounter++;
        
        // Render animated galaxy background
        renderGalaxyBackground(context);
        
        // Render stars
        renderStars(context);
        
        // Render main panel with glass effect
        int panelWidth = this.width - 100;
        int panelHeight = this.height - 80;
        int panelX = 50;
        int panelY = 40;
        
        // Dark translucent background panel
        context.fillGradient(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 
            0xCC0A0520, 0xCC1A0F40);
        
        // Border with galaxy colors
        drawGlowingBorder(context, panelX, panelY, panelWidth, panelHeight);
        
        // Render "FBR CLIENT" in top left with glow effect
        String clientName = "FBR CLIENT";
        int nameX = panelX + 20;
        int nameY = panelY + 15;
        
        // Glow effect for title
        context.drawText(this.textRenderer, clientName, nameX + 1, nameY + 1, 0xFF8844FF, false);
        context.drawText(this.textRenderer, clientName, nameX, nameY, 0xFFFFAAFF, false);
        
        // Version number next to title
        String versionText = "v" + ModListMod.VERSION;
        context.drawText(this.textRenderer, versionText, 
            nameX + this.textRenderer.getWidth(clientName) + 10, nameY + 2, 0xFFAA88FF, false);
        
        // Subtitle
        context.drawText(this.textRenderer, "Mod Manager", nameX, nameY + 12, 0xFF6644AA, false);
        
        // Calculate dropdown area
        int dropdownWidth = panelWidth - 80;
        int dropdownX = panelX + 40;
        int startY = panelY + 60;
        
        // ===== RENDER DROPDOWN MENUS (2 COLUMN LAYOUT) =====
        
        // Calculate column widths with gap
        int columnGap = 15;
        int columnWidth = (dropdownWidth - columnGap) / 2;
        int leftColumnX = dropdownX;
        int rightColumnX = dropdownX + columnWidth + columnGap;
        
        // LEFT COLUMN
        // CPVP Dropdown
        renderDropdown(context, "CPVP", leftColumnX, startY, columnWidth, cpvpExpanded, cpvpItems, mouseX, mouseY);
        
        // Calculate next position in left column
        int cpvpHeight = 30 + (cpvpExpanded ? cpvpItems.size() * 25 : 0);
        
        // DONUT Dropdown (below CPVP)
        renderDropdown(context, "DONUT", leftColumnX, startY + cpvpHeight + 15, columnWidth, donutExpanded, donutItems, mouseX, mouseY);
        
        // RIGHT COLUMN
        // PERFORMANCE Dropdown
        renderDropdown(context, "PERFORMANCE", rightColumnX, startY, columnWidth, performanceExpanded, performanceItems, mouseX, mouseY);
        
        // Calculate next position in right column
        int performanceHeight = 30 + (performanceExpanded ? performanceItems.size() * 25 : 0);
        
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        tickCounter++;
        
        // Render dark gradient background (Krypton style)
        renderKryptonBackground(context);
        
        // Render main panel - modern flat design
        int panelWidth = this.width - 100;
        int panelHeight = this.height - 80;
        int panelX = 50;
        int panelY = 40;
        
        // Dark background with subtle gradient (Krypton style)
        context.fillGradient(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 
            0xE0101010, 0xE0181818);
        
        // Accent line at top (cyan/blue theme like Krypton)
        context.fillGradient(panelX, panelY, panelX + panelWidth, panelY + 3, 
            0xFF00D9FF, 0xFF0099FF);
        
        // Render "FBR CLIENT" header - Krypton style
        String clientName = "FBR CLIENT";
        int nameX = panelX + 20;
        int nameY = panelY + 15;
        
        // Modern gradient text effect
        context.drawText(this.textRenderer, clientName, nameX, nameY, 0xFF00D9FF, false);
        
        // Version number
        String versionText = "v" + ModListMod.VERSION;
        context.drawText(this.textRenderer, versionText, 
            nameX + this.textRenderer.getWidth(clientName) + 10, nameY + 2, 0xFF00A9DD, false);
        
        // Thin separator line
        context.fill(panelX + 15, panelY + 38, panelX + panelWidth - 15, panelY + 39, 0x40FFFFFF);
        
        // Calculate dropdown area
        int dropdownWidth = panelWidth - 60;
        int dropdownX = panelX + 30;
        int startY = panelY + 50;
        
        // ===== 3 COLUMNS IN A ROW =====
        int columnGap = 15;
        int columnWidth = (dropdownWidth - (columnGap * 2)) / 3; // Divide into 3 equal columns
        int cpvpX = dropdownX;
        int donutX = dropdownX + columnWidth + columnGap;
        int performanceX = dropdownX + (columnWidth + columnGap) * 2;
        
        // FIRST ROW - All three main dropdowns side by side
        renderKryptonDropdown(context, "CPVP", cpvpX, startY, columnWidth, cpvpExpanded, cpvpItems, mouseX, mouseY);
        renderKryptonDropdown(context, "DONUT", donutX, startY, columnWidth, donutExpanded, donutItems, mouseX, mouseY);
        renderKryptonDropdown(context, "PERFORMANCE", performanceX, startY, columnWidth, performanceExpanded, performanceItems, mouseX, mouseY);
        
        // Calculate tallest dropdown
        int cpvpHeight = 35 + (cpvpExpanded ? cpvpItems.size() * 28 : 0);
        int donutHeight = 35 + (donutExpanded ? donutItems.size() * 28 : 0);
        int performanceHeight = 35 + (performanceExpanded ? performanceItems.size() * 28 : 0);
        int maxHeight = Math.max(cpvpHeight, Math.max(donutHeight, performanceHeight));
        
        // SECOND ROW - Changelog (full width)
        int changelogY = startY + maxHeight + 15;
        renderKryptonSmallDropdown(context, "Changelog", dropdownX, changelogY, dropdownWidth, changesExpanded, changesItems, mouseX, mouseY);
        
        // Render slot selection menu if shown
        if (showSlotMenu) {
            renderKryptonSlotMenu(context, slotMenuX, slotMenuY, mouseX, mouseY);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void renderKryptonBackground(DrawContext context) {
        // Dark gradient background (Krypton style)
        context.fillGradient(0, 0, this.width, this.height, 0xFF0A0A0A, 0xFF141414);
    }
    
    private void renderKryptonDropdown(DrawContext context, String title, int x, int y, int width, boolean expanded, List<String> items, int mouseX, int mouseY) {
        int headerHeight = 35;
        boolean headerHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + headerHeight;
        
        // Header background - gradient (Krypton style)
        if (headerHovered) {
            context.fillGradient(x, y, x + width, y + headerHeight, 0xFF1A1A1A, 0xFF222222);
        } else {
            context.fillGradient(x, y, x + width, y + headerHeight, 0xFF151515, 0xFF1A1A1A);
        }
        
        // Accent line on left edge
        context.fill(x, y, x + 2, y + headerHeight, expanded ? 0xFF00D9FF : 0xFF006699);
        
        // Title
        context.drawText(this.textRenderer, title, x + 12, y + 12, 0xFFFFFFFF, false);
        
        // Arrow indicator
        String arrow = expanded ? "▼" : "▶";
        context.drawText(this.textRenderer, arrow, x + width - 20, y + 12, 0xFF00BBEE, false);
        
        // If expanded, draw items
        if (expanded) {
            int itemY = y + headerHeight;
            for (int i = 0; i < items.size(); i++) {
                String item = items.get(i);
                int itemHeight = 28;
                boolean itemHovered = mouseX >= x && mouseX <= x + width && 
                                    mouseY >= itemY && mouseY <= itemY + itemHeight;
                
                // Item background
                if (itemHovered) {
                    context.fillGradient(x, itemY, x + width, itemY + itemHeight, 0xFF1E1E1E, 0xFF262626);
                } else {
                    context.fill(x, itemY, x + width, itemY + itemHeight, 0xFF161616);
                }
                
                // Subtle separator
                context.fill(x, itemY, x + width, itemY + 1, 0x20FFFFFF);
                
                // Hover indicator line
                if (itemHovered) {
                    context.fill(x, itemY, x + 2, itemY + itemHeight, 0xFF00D9FF);
                }
                
                // Item text
                context.drawText(this.textRenderer, item, x + 12, itemY + 9, 
                    itemHovered ? 0xFFFFFFFF : 0xFFCCCCCC, false);
                
                itemY += itemHeight;
            }
        }
    }
    
    private void renderKryptonSmallDropdown(DrawContext context, String title, int x, int y, int width, boolean expanded, List<String> items, int mouseX, int mouseY) {
        int headerHeight = 28;
        boolean headerHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + headerHeight;
        
        // Smaller header for changelog
        if (headerHovered) {
            context.fillGradient(x, y, x + width, y + headerHeight, 0xFF1A1A1A, 0xFF202020);
        } else {
            context.fill(x, y, x + width, y + headerHeight, 0xFF151515);
        }
        
        // Accent line
        context.fill(x, y, x + 2, y + headerHeight, 0xFF006699);
        
        // Title
        context.drawText(this.textRenderer, title, x + 10, y + 9, 0xFFAAAAAA, false);
        
        // Arrow
        String arrow = expanded ? "▼" : "▶";
        context.drawText(this.textRenderer, arrow, x + width - 18, y + 9, 0xFF00AADD, false);
        
        if (expanded) {
            int itemY = y + headerHeight;
            for (String item : items) {
                int itemHeight = 24;
                boolean itemHovered = mouseX >= x && mouseX <= x + width && 
                                    mouseY >= itemY && mouseY <= itemY + itemHeight;
                
                context.fill(x, itemY, x + width, itemY + itemHeight, itemHovered ? 0xFF1A1A1A : 0xFF141414);
                context.fill(x, itemY, x + width, itemY + 1, 0x15FFFFFF);
                
                context.drawText(this.textRenderer, item, x + 10, itemY + 7, 0xFF999999, false);
                
                itemY += itemHeight;
            }
        }
    }
    
    private void renderKryptonSlotMenu(DrawContext context, int x, int y, int mouseX, int mouseY) {
        int menuWidth = 160;
        int menuHeight = 260;
        
        // Dark background
        context.fillGradient(x, y, x + menuWidth, y + menuHeight, 0xF0101010, 0xF0181818);
        
        // Accent border
        context.fill(x, y, x + menuWidth, y + 2, 0xFF00D9FF);
        context.fill(x, y, x + 2, y + menuHeight, 0xFF00D9FF);
        context.fill(x + menuWidth - 2, y, x + menuWidth, y + menuHeight, 0xFF00D9FF);
        context.fill(x, y + menuHeight - 2, x + menuWidth, y + menuHeight, 0xFF00D9FF);
        
        // Title
        context.drawText(this.textRenderer, "Totem Slot", x + 12, y + 12, 0xFFFFFFFF, false);
        
        // Separator
        context.fill(x + 10, y + 28, x + menuWidth - 10, y + 29, 0x40FFFFFF);
        
        // Slot buttons
        int slotY = y + 38;
        for (int slot = 1; slot <= 9; slot++) {
            int slotHeight = 24;
            boolean isCurrentSlot = (slot == autoAnchorSlot);
            boolean isHovered = mouseX >= x + 10 && mouseX <= x + menuWidth - 10 && 
                               mouseY >= slotY && mouseY <= slotY + slotHeight;
            
            // Button background
            if (isCurrentSlot) {
                context.fillGradient(x + 10, slotY, x + menuWidth - 10, slotY + slotHeight, 0xFF1A3A3A, 0xFF244444);
            } else if (isHovered) {
                context.fillGradient(x + 10, slotY, x + menuWidth - 10, slotY + slotHeight, 0xFF1E1E1E, 0xFF242424);
            } else {
                context.fill(x + 10, slotY, x + menuWidth - 10, slotY + slotHeight, 0xFF161616);
            }
            
            // Accent for current
            if (isCurrentSlot) {
                context.fill(x + 10, slotY, x + 12, slotY + slotHeight, 0xFF00FF88);
            }
            
            // Slot text
            String slotText = "Slot " + slot + (isCurrentSlot ? " ✓" : "");
            context.drawText(this.textRenderer, slotText, x + 20, slotY + 7, 
                isCurrentSlot ? 0xFF00FFAA : (isHovered ? 0xFFFFFFFF : 0xFFCCCCCC), false);
            
            slotY += slotHeight + 2;
        }
    }
    
    private void renderDropdown(DrawContext context, String title, int x, int y, int width, boolean expanded, List<String> items, int mouseX, int mouseY) {
        // Dropdown header
        int headerHeight = 30;
        boolean headerHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + headerHeight;
        
        // Header background
        int headerAlpha = headerHovered ? 0x70 : 0x50;
        int headerColor1 = (headerAlpha << 24) | 0x4A2A7A;
        int headerColor2 = (headerAlpha << 24) | 0x2A1A5A;
        context.fillGradient(x, y, x + width, y + headerHeight, headerColor1, headerColor2);
        
        // Header border
        context.fill(x, y, x + width, y + 2, 0xFF8844FF);
        context.fill(x, y + headerHeight - 2, x + width, y + headerHeight, 0xFF6644DD);
        
        // Draw title
        context.drawText(this.textRenderer, title, x + 10, y + 10, 0xFFDDCCFF, false);
        
        // Draw expand/collapse arrow
        String arrow = expanded ? "▼" : "▶";
        context.drawText(this.textRenderer, arrow, x + width - 20, y + 10, 0xFFAA88FF, false);
        
        // If expanded, draw items
        if (expanded) {
            int itemY = y + headerHeight;
            for (int i = 0; i < items.size(); i++) {
                String item = items.get(i);
                int itemHeight = 25;
                boolean itemHovered = mouseX >= x && mouseX <= x + width && 
                                    mouseY >= itemY && mouseY <= itemY + itemHeight;
                
                // Item background
                int itemAlpha = itemHovered ? 0x50 : 0x30;
                int itemColor = (itemAlpha << 24) | 0x1A0A3A;
                context.fill(x, itemY, x + width, itemY + itemHeight, itemColor);
                
                // Item separator
                context.fill(x, itemY, x + width, itemY + 1, 0x40FFFFFF);
                
                // Accent bar for hovered items
                if (itemHovered) {
                    context.fill(x, itemY, x + 3, itemY + itemHeight, getGalaxyAccentColor(i));
                }
                
                // Draw item text
                context.drawText(this.textRenderer, "  " + item, x + 10, itemY + 7, 0xFFBBAADD, false);
                
                itemY += itemHeight;
            }
        }
    }
    
    private void renderDropdown(DrawContext context, String title, int x, int y, int width, boolean expanded, List<String> items, int mouseX, int mouseY) {
        // This method is replaced by renderKryptonDropdown
        // Kept for compatibility but not used
    }
    
    private void renderSmallDropdown(DrawContext context, String title, int x, int y, int width, boolean expanded, List<String> items, int mouseX, int mouseY) {
        // This method is replaced by renderKryptonSmallDropdown  
        // Kept for compatibility but not used
    }
    
    private void renderSlotSelectionMenu(DrawContext context, int x, int y, int mouseX, int mouseY) {
        // This method is replaced by renderKryptonSlotMenu
        // Kept for compatibility but not used
    }

    private void renderGalaxyBackground(DrawContext context) {
        // Removed - using Krypton dark gradient background
    }

    private void renderStars(DrawContext context) {
        // Removed - Krypton style uses clean backgrounds
    }

    private void drawGlowingBorder(DrawContext context, int x, int y, int width, int height) {
        // Removed - Krypton uses accent lines instead
    }

    private int getGalaxyAccentColor(int index) {
        int[] colors = {
            0xFFAA44FF, 0xFF4488FF, 0xFFFF44AA, 0xFF44DDFF, 0xFFDD44FF
        };
        return colors[index % colors.length];
    }

    private int interpolateColor(int color1, int color2, float factor) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        
        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        
        int a = (int)(a1 + (a2 - a1) * factor);
        int r = (int)(r1 + (r2 - r1) * factor);
        int g = (int)(g1 + (g2 - g1) * factor);
        int b = (int)(b1 + (b2 - b1) * factor);
        
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private void renderGalaxyBackground(DrawContext context) {
        // Animated gradient background
        int time = tickCounter;
        int color1 = interpolateColor(0xFF0A0520, 0xFF1A0F40, (float)Math.sin(time * 0.01) * 0.5f + 0.5f);
        int color2 = interpolateColor(0xFF1A0F40, 0xFF2A1A5A, (float)Math.cos(time * 0.008) * 0.5f + 0.5f);
        context.fillGradient(0, 0, this.width, this.height, color1, color2);
    }

    private void renderStars(DrawContext context) {
        for (Star star : stars) {
            float twinkle = (float)Math.sin((tickCounter + star.offset) * 0.05) * 0.3f + 0.7f;
            int alpha = (int)(255 * twinkle * star.brightness);
            int color = (alpha << 24) | 0xFFFFFF;
            
            int x = (int)(star.x * this.width);
            int y = (int)(star.y * this.height);
            int size = (int)star.size;
            
            context.fill(x, y, x + size, y + size, color);
        }
    }

    private void drawGlowingBorder(DrawContext context, int x, int y, int width, int height) {
        int time = tickCounter;
        int glowColor = interpolateColor(0xFF6644FF, 0xFFAA88FF, (float)Math.sin(time * 0.03) * 0.5f + 0.5f);
        
        // Top border
        context.fill(x, y, x + width, y + 2, glowColor);
        // Bottom border
        context.fill(x, y + height - 2, x + width, y + height, glowColor);
        // Left border
        context.fill(x, y, x + 2, y + height, glowColor);
        // Right border
        context.fill(x + width - 2, y, x + width, y + height, glowColor);
    }

    private int getGalaxyAccentColor(int index) {
        int[] colors = {
            0xFFAA44FF, // Purple
            0xFF4488FF, // Blue
            0xFFFF44AA, // Pink
            0xFF44DDFF, // Cyan
            0xFFDD44FF  // Magenta
        };
        return colors[index % colors.length];
    }

    private int interpolateColor(int color1, int color2, float factor) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        
        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        
        int a = (int)(a1 + (a2 - a1) * factor);
        int r = (int)(r1 + (r2 - r1) * factor);
        int g = (int)(g1 + (g2 - g1) * factor);
        int b = (int)(b1 + (b2 - b1) * factor);
        
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle slot menu clicks first
        if (showSlotMenu && button == 0) {
            int menuWidth = 160;
            int menuHeight = 260;
            
            // Check if clicking outside menu to close it
            if (mouseX < slotMenuX || mouseX > slotMenuX + menuWidth ||
                mouseY < slotMenuY || mouseY > slotMenuY + menuHeight) {
                showSlotMenu = false;
                return true;
            }
            
            // Check slot button clicks
            int slotY = slotMenuY + 38;
            for (int slot = 1; slot <= 9; slot++) {
                int slotHeight = 24;
                if (mouseX >= slotMenuX + 10 && mouseX <= slotMenuX + menuWidth - 10 &&
                    mouseY >= slotY && mouseY <= slotY + slotHeight) {
                    autoAnchorSlot = slot;
                    showSlotMenu = false;
                    System.out.println("Auto Anchor totem slot set to: " + slot);
                    return true;
                }
                slotY += slotHeight + 2;
            }
        }
        
        if (button == 0) { // Left click
            // Calculate dropdown positions for 3-column layout
            int panelWidth = this.width - 100;
            int panelX = 50;
            int panelY = 40;
            int dropdownWidth = panelWidth - 60;
            int dropdownX = panelX + 30;
            int startY = panelY + 50;
            int headerHeight = 35;
            
            // Calculate 3 columns
            int columnGap = 15;
            int columnWidth = (dropdownWidth - (columnGap * 2)) / 3;
            int cpvpX = dropdownX;
            int donutX = dropdownX + columnWidth + columnGap;
            int performanceX = dropdownX + (columnWidth + columnGap) * 2;
            
            // Check CPVP dropdown click
            if (mouseX >= cpvpX && mouseX <= cpvpX + columnWidth && 
                mouseY >= startY && mouseY <= startY + headerHeight) {
                cpvpExpanded = !cpvpExpanded;
                return true;
            }
            
            // Check DONUT dropdown click
            if (mouseX >= donutX && mouseX <= donutX + columnWidth && 
                mouseY >= startY && mouseY <= startY + headerHeight) {
                donutExpanded = !donutExpanded;
                return true;
            }
            
            // Check PERFORMANCE dropdown click
            if (mouseX >= performanceX && mouseX <= performanceX + columnWidth && 
                mouseY >= startY && mouseY <= startY + headerHeight) {
                performanceExpanded = !performanceExpanded;
                return true;
            }
            
            // Check Changelog dropdown click
            int cpvpHeight = 35 + (cpvpExpanded ? cpvpItems.size() * 28 : 0);
            int donutHeight = 35 + (donutExpanded ? donutItems.size() * 28 : 0);
            int performanceHeight = 35 + (performanceExpanded ? performanceItems.size() * 28 : 0);
            int maxHeight = Math.max(cpvpHeight, Math.max(donutHeight, performanceHeight));
            int changelogY = startY + maxHeight + 15;
            int smallHeaderHeight = 28;
            
            if (mouseX >= dropdownX && mouseX <= dropdownX + dropdownWidth && 
                mouseY >= changelogY && mouseY <= changelogY + smallHeaderHeight) {
                changesExpanded = !changesExpanded;
                return true;
            }
            
            // Check if clicking on CPVP items (when expanded)
            if (cpvpExpanded) {
                int itemY = startY + headerHeight;
                for (int i = 0; i < cpvpItems.size(); i++) {
                    if (mouseX >= cpvpX && mouseX <= cpvpX + columnWidth && 
                        mouseY >= itemY && mouseY <= itemY + 28) {
                        String itemName = cpvpItems.get(i);
                        System.out.println("CPVP item clicked: " + itemName);
                        
                        if (itemName.equals("Auto Anchor")) {
                            autoAnchorEnabled = !autoAnchorEnabled;
                            System.out.println("Auto Anchor " + (autoAnchorEnabled ? "enabled" : "disabled"));
                        }
                        return true;
                    }
                    itemY += 28;
                }
            }
            
            // Check if clicking on DONUT items (when expanded)
            if (donutExpanded) {
                int itemY = startY + headerHeight;
                for (int i = 0; i < donutItems.size(); i++) {
                    if (mouseX >= donutX && mouseX <= donutX + columnWidth && 
                        mouseY >= itemY && mouseY <= itemY + 28) {
                        System.out.println("DONUT item clicked: " + donutItems.get(i));
                        return true;
                    }
                    itemY += 28;
                }
            }
            
            // Check if clicking on PERFORMANCE items (when expanded)
            if (performanceExpanded) {
                int itemY = startY + headerHeight;
                for (int i = 0; i < performanceItems.size(); i++) {
                    if (mouseX >= performanceX && mouseX <= performanceX + columnWidth && 
                        mouseY >= itemY && mouseY <= itemY + 28) {
                        System.out.println("PERFORMANCE item clicked: " + performanceItems.get(i));
                        return true;
                    }
                    itemY += 28;
                }
            }
            
            // Check if clicking on Changelog items (when expanded)
            if (changesExpanded) {
                int itemY = changelogY + smallHeaderHeight;
                for (int i = 0; i < changesItems.size(); i++) {
                    if (mouseX >= dropdownX && mouseX <= dropdownX + dropdownWidth && 
                        mouseY >= itemY && mouseY <= itemY + 24) {
                        System.out.println("Changelog item clicked: " + changesItems.get(i));
                        return true;
                    }
                    itemY += 24;
                }
            }
        } else if (button == 1) { // Right click
            // Calculate positions for right-click detection
            int panelWidth = this.width - 100;
            int panelX = 50;
            int panelY = 40;
            int dropdownWidth = panelWidth - 60;
            int dropdownX = panelX + 30;
            int startY = panelY + 50;
            int headerHeight = 35;
            int columnGap = 15;
            int columnWidth = (dropdownWidth - (columnGap * 2)) / 3;
            int cpvpX = dropdownX;
            
            // Check if right-clicking on Auto Anchor
            if (cpvpExpanded) {
                int itemY = startY + headerHeight;
                for (int i = 0; i < cpvpItems.size(); i++) {
                    if (mouseX >= cpvpX && mouseX <= cpvpX + columnWidth && 
                        mouseY >= itemY && mouseY <= itemY + 28) {
                        String itemName = cpvpItems.get(i);
                        
                        if (itemName.equals("Auto Anchor")) {
                            // Show slot selection menu
                            showSlotMenu = true;
                            slotMenuX = (int)mouseX + 10;
                            slotMenuY = (int)mouseY;
                            System.out.println("Opening Auto Anchor slot menu");
                            return true;
                        }
                    }
                    itemY += 28;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Scrolling removed since we removed the mod list
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean shouldPause() {
        return false; // Don't pause the game when this screen is open
    }

    // Helper class to store mod information
    private static class ModInfo {
        String id;
        String name;
        String version;
        String description;

        ModInfo(String id, String name, String version, String description) {
            this.id = id;
            this.name = name;
            this.version = version;
            this.description = description;
        }
    }
}
