package com.bonepipe.gui;

import com.bonepipe.BonePipe;
import com.bonepipe.blocks.AdapterBlockEntity;
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
        NETWORK(3, Component.translatable("gui.bonepipe.tab.network"));
        
        final int index;
        final Component label;
        
        Tab(int index, Component label) {
            this.index = index;
            this.label = label;
        }
    }
    
    private Tab currentTab = Tab.ITEMS;
    
    public AdapterScreen(AdapterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }
    
    @Override
    protected void init() {
        super.init();
        // TODO: Add buttons and widgets
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
            
            // TODO: Draw tab icon
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
        // TODO: Show item transfer configuration
        this.font.draw(poseStack, "TODO: Item filters", 8, 35, 0x808080);
    }
    
    /**
     * Render Fluids tab content
     */
    private void renderFluidsTab(PoseStack poseStack) {
        this.font.draw(poseStack, Component.translatable("gui.bonepipe.fluids.title"), 
            8, 20, 0x404040);
        // TODO: Show fluid transfer configuration
        this.font.draw(poseStack, "TODO: Fluid whitelist", 8, 35, 0x808080);
    }
    
    /**
     * Render Energy tab content
     */
    private void renderEnergyTab(PoseStack poseStack) {
        this.font.draw(poseStack, Component.translatable("gui.bonepipe.energy.title"), 
            8, 20, 0x404040);
        
        AdapterBlockEntity be = menu.getBlockEntity();
        // TODO: Show energy transfer rate
        this.font.draw(poseStack, "TODO: Transfer rate", 8, 35, 0x808080);
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
                currentTab = tab;
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
