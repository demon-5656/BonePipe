package com.bonepipe.gui;

import com.bonepipe.BonePipe;
import com.bonepipe.blocks.AdapterBlockEntity;
import com.bonepipe.gui.widgets.FrequencyTextField;
import com.bonepipe.gui.widgets.SideConfigWidget;
import com.bonepipe.gui.widgets.StatusIndicator;
import com.bonepipe.gui.widgets.ToggleButton;
import com.bonepipe.network.packets.NetworkHandler;
import com.bonepipe.network.packets.UpdateFrequencyPacket;
import com.bonepipe.network.packets.UpdateSideConfigPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Client-side GUI screen for Wireless Adapter
 * Renders tabs for Items, Fluids, Energy, and Network configuration
 */
public class AdapterScreen extends AbstractContainerScreen<AdapterMenu> {
    
    private static final ResourceLocation TEXTURE = 
        new ResourceLocation(BonePipe.MODID, "textures/gui/adapter.png");
    
    // Tab system
    private static final int TAB_WIDTH = 28;
    private static final int TAB_HEIGHT = 32;
    private enum Tab {
        ITEMS(0, Component.translatable("gui.bonepipe.tab.items")),
        FLUIDS(1, Component.translatable("gui.bonepipe.tab.fluids")),
        ENERGY(2, Component.translatable("gui.bonepipe.tab.energy")),
        GAS(3, Component.translatable("gui.bonepipe.tab.gas")),
        NETWORK(4, Component.translatable("gui.bonepipe.tab.network"));
        
        final int index;
        final Component label;
        
        Tab(int index, Component label) {
            this.index = index;
            this.label = label;
        }
    }
    
    private Tab currentTab = Tab.ITEMS;
    
    // Widgets
    private FrequencyTextField frequencyField;
    private ToggleButton enableButton;
    private StatusIndicator statusIndicator;
    private SideConfigWidget sideConfigWidget;
    
    public AdapterScreen(AdapterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // Initialize widgets based on current tab
        initializeWidgets(x, y);
    }
    
    /**
     * Initialize widgets for current tab
     */
    private void initializeWidgets(int x, int y) {
        // Clear existing widgets
        this.clearWidgets();
        
        AdapterBlockEntity be = menu.getBlockEntity();
        
        switch (currentTab) {
            case NETWORK -> {
                // Frequency text field
                frequencyField = new FrequencyTextField(
                    this.font, x + 50, y + 35, 100, 18,
                    Component.translatable("gui.bonepipe.frequency"),
                    freq -> {
                        // Validate frequency before sending
                        if (freq != null && !freq.isEmpty()) {
                            if (FrequencyTextField.isValidFrequency(freq)) {
                                // Send frequency update to server
                                NetworkHandler.CHANNEL.sendToServer(
                                    new UpdateFrequencyPacket(be.getBlockPos(), freq)
                                );
                            } else {
                                // Show error feedback
                                frequencyField.setTextColor(0xFF0000);
                                BonePipe.LOGGER.warn("Invalid frequency format: {}", freq);
                            }
                        }
                    }
                );
                String currentFreq = be.getFrequency();
                frequencyField.setValue(currentFreq != null ? currentFreq : "");
                this.addRenderableWidget(frequencyField);
                
                // Status indicator
                statusIndicator = new StatusIndicator(
                    x + 8, y + 60, 16, 16,
                    be.isEnabled() ? StatusIndicator.Status.CONNECTED : StatusIndicator.Status.DISCONNECTED
                );
                this.addRenderableWidget(statusIndicator);
            }
            case ITEMS, FLUIDS, GAS -> {
                // Add side configuration widget
                SideConfigWidget.ResourceType resourceType = switch (currentTab) {
                    case ITEMS -> SideConfigWidget.ResourceType.ITEMS;
                    case FLUIDS -> SideConfigWidget.ResourceType.FLUIDS;
                    case GAS -> SideConfigWidget.ResourceType.GAS;
                    default -> SideConfigWidget.ResourceType.ITEMS;
                };
                
                sideConfigWidget = new SideConfigWidget(
                    x + 8, y + 40, 160, 110,
                    be.getBlockPos(), be, resourceType
                );
                this.addRenderableWidget(sideConfigWidget);
            }
            case ENERGY -> {
                // Add side configuration widget for Energy
                sideConfigWidget = new SideConfigWidget(
                    x + 8, y + 40, 160, 110,
                    be.getBlockPos(), be, SideConfigWidget.ResourceType.ENERGY
                );
                this.addRenderableWidget(sideConfigWidget);
            }
        }
    }
    
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }
    
    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // Draw main background
        this.blit(poseStack, x, y, 0, 0, this.imageWidth, this.imageHeight);
        
        // Draw tabs
        renderTabs(poseStack, x, y);
    }
    
    /**
     * Render tab buttons
     */
    private void renderTabs(PoseStack poseStack, int x, int y) {
        for (Tab tab : Tab.values()) {
            int tabX = x - TAB_WIDTH + 3;
            int tabY = y + 4 + (tab.index * (TAB_HEIGHT + 2));
            
            boolean selected = tab == currentTab;
            int texY = selected ? 166 : 198;
            
            // Draw tab background
            this.blit(poseStack, tabX, tabY, 176, texY, TAB_WIDTH, TAB_HEIGHT);
            
            // Draw tab icon (16x16 from texture atlas)
            int iconTexX = 206 + (tab.index * 18);
            int iconTexY = 166;
            this.blit(poseStack, tabX + 6, tabY + 8, iconTexX, iconTexY, 16, 16);
        }
    }
    
    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // Draw title
        this.font.draw(poseStack, this.title, 8, 6, 0x404040);
        
        // Draw current tab content
        switch (currentTab) {
            case ITEMS -> renderItemsTab(poseStack);
            case FLUIDS -> renderFluidsTab(poseStack);
            case ENERGY -> renderEnergyTab(poseStack);
            case GAS -> renderGasTab(poseStack);
            case NETWORK -> renderNetworkTab(poseStack);
        }
    }
    
    /**
     * Render Items tab content
     */
    private void renderItemsTab(PoseStack poseStack) {
        this.font.draw(poseStack, Component.translatable("gui.bonepipe.items.title"), 
            8, 20, 0x404040);
        
        AdapterBlockEntity be = menu.getBlockEntity();
        
        // Show transfer status
        boolean enabled = be.isEnabled();
        this.font.draw(poseStack, 
            Component.translatable("gui.bonepipe.status").append(": " + (enabled ? "Enabled" : "Disabled")), 
            8, 35, enabled ? 0x00AA00 : 0xAA0000);
        
        // Show upgrade bonuses
        if (be.getSpeedMultiplier() > 1.0) {
            this.font.draw(poseStack, 
                Component.literal("Speed: " + String.format("%.1fx", be.getSpeedMultiplier())), 
                8, 50, 0x404040);
        }
        
        if (be.getStackBonus() > 0) {
            this.font.draw(poseStack, 
                Component.literal("Stack Bonus: +" + be.getStackBonus()), 
                8, 60, 0x404040);
        }
        
        if (be.hasFilterUpgrade()) {
            int filterSlots = be.getTotalFilterSlots();
            this.font.draw(poseStack, 
                Component.literal("Filter Slots: " + filterSlots), 
                8, 70, 0x00FFFF);
        }
    }
    
    /**
     * Render Fluids tab content
     */
    private void renderFluidsTab(PoseStack poseStack) {
        this.font.draw(poseStack, Component.translatable("gui.bonepipe.fluids.title"), 
            8, 20, 0x404040);
        
        AdapterBlockEntity be = menu.getBlockEntity();
        
        // Show transfer status
        boolean enabled = be.isEnabled();
        this.font.draw(poseStack, 
            Component.translatable("gui.bonepipe.status").append(": " + (enabled ? "Enabled" : "Disabled")), 
            8, 35, enabled ? 0x00AA00 : 0xAA0000);
        
        // Show transfer rate with upgrades
        long baseRate = 1000; // mB per tick
        long actualRate = (long)(baseRate * be.getSpeedMultiplier());
        this.font.draw(poseStack, 
            Component.literal("Transfer Rate: " + actualRate + " mB/tick"), 
            8, 50, 0x404040);
        
        if (be.hasFilterUpgrade()) {
            this.font.draw(poseStack, 
                Component.literal("Whitelist: Not configured"), 
                8, 65, 0x808080);
        }
    }
    
    /**
     * Render Energy tab content
     */
    private void renderEnergyTab(PoseStack poseStack) {
        this.font.draw(poseStack, Component.translatable("gui.bonepipe.energy.title"), 
            8, 20, 0x404040);
        
        AdapterBlockEntity be = menu.getBlockEntity();
        
        // Show transfer status
        boolean enabled = be.isEnabled();
        this.font.draw(poseStack, 
            Component.translatable("gui.bonepipe.status").append(": " + (enabled ? "Enabled" : "Disabled")), 
            8, 35, enabled ? 0x00AA00 : 0xAA0000);
        
        // Show transfer rate with upgrades
        long baseRate = 1000; // FE per tick
        long actualRate = (long)(baseRate * be.getSpeedMultiplier());
        this.font.draw(poseStack, 
            Component.literal("Transfer Rate: " + actualRate + " FE/tick"), 
            8, 50, 0x404040);
        
        // Show statistics
        this.font.draw(poseStack, 
            Component.literal("Total Transferred: " + be.getTotalTransferred()), 
            8, 65, 0x404040);
    }
    
    /**
     * Render Gas tab content (Mekanism integration)
     */
    private void renderGasTab(PoseStack poseStack) {
        this.font.draw(poseStack, Component.translatable("gui.bonepipe.gas.title"), 
            8, 20, 0x404040);
        
        AdapterBlockEntity be = menu.getBlockEntity();
        
        // Show transfer status
        boolean enabled = be.isEnabled();
        this.font.draw(poseStack, 
            Component.translatable("gui.bonepipe.status").append(": " + (enabled ? "Enabled" : "Disabled")), 
            8, 35, enabled ? 0x00AA00 : 0xAA0000);
        
        // Show transfer rate with upgrades
        long baseRate = 1000; // mB per tick
        long actualRate = (long)(baseRate * be.getSpeedMultiplier());
        this.font.draw(poseStack, 
            Component.literal("Transfer Rate: " + actualRate + " mB/tick"), 
            8, 50, 0x404040);
        
        // Show Mekanism status
        this.font.draw(poseStack, 
            Component.literal("Mekanism: Installed"), 
            8, 65, 0x00FFAA);
        
        if (be.hasFilterUpgrade()) {
            this.font.draw(poseStack, 
                Component.literal("Gas Filter: Not configured"), 
                8, 80, 0x808080);
        }
    }
    
    /**
     * Render Network tab content
     */
    private void renderNetworkTab(PoseStack poseStack) {
        this.font.draw(poseStack, Component.translatable("gui.bonepipe.network.title"), 
            8, 20, 0x404040);
        
        AdapterBlockEntity be = menu.getBlockEntity();
        String frequency = be.getFrequency();
        
        // Show frequency
        this.font.draw(poseStack, 
            Component.translatable("gui.bonepipe.frequency").append(": " + frequency), 
            8, 35, 0x404040);
        
        // Show access mode
        String accessMode = be.getAccessMode().name();
        this.font.draw(poseStack, 
            Component.translatable("gui.bonepipe.access_mode").append(": " + accessMode), 
            8, 50, 0x404040);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check tab clicks
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        for (Tab tab : Tab.values()) {
            int tabX = x - TAB_WIDTH + 3;
            int tabY = y + 4 + (tab.index * (TAB_HEIGHT + 2));
            
            if (mouseX >= tabX && mouseX < tabX + TAB_WIDTH &&
                mouseY >= tabY && mouseY < tabY + TAB_HEIGHT) {
                if (currentTab != tab) {
                    currentTab = tab;
                    // Reinitialize widgets for new tab
                    initializeWidgets(x, y);
                }
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
