package com.bonepipe.gui;

import com.bonepipe.BonePipe;
import com.bonepipe.blocks.ControllerBlockEntity;
import com.bonepipe.network.NetworkStatistics;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * GUI Screen for Network Controller
 * Shows network overview, statistics, and node management
 */
public class ControllerScreen extends AbstractContainerScreen<ControllerMenu> {
    
    private static final ResourceLocation TEXTURE = 
        new ResourceLocation(BonePipe.MODID, "textures/gui/controller.png");
    
    public ControllerScreen(ControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 220;
    }
    
    @Override
    protected void init() {
        super.init();
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
        
        // Draw background
        this.blit(poseStack, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }
    
    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        ControllerBlockEntity be = menu.getBlockEntity();
        
        // Title
        this.font.draw(poseStack, this.title, 8, 6, 0x404040);
        
        // Frequency info
        String freq = be.getFrequency();
        if (freq.isEmpty()) {
            this.font.draw(poseStack, 
                Component.literal("§cNo Frequency Set"), 
                8, 20, 0xFF0000);
        } else {
            this.font.draw(poseStack, 
                Component.literal("Frequency: §b" + freq), 
                8, 20, 0x404040);
        }
        
        // Network stats
        int nodeCount = be.getNodeCount();
        int activeCount = be.getActiveNodeCount();
        
        this.font.draw(poseStack, 
            Component.literal("Nodes: " + activeCount + "/" + nodeCount), 
            8, 35, nodeCount > 0 ? 0x00AA00 : 0x404040);
        
        // Statistics
        NetworkStatistics.FrequencyStats stats = be.getStats();
        if (stats != null) {
            this.font.draw(poseStack, 
                Component.literal("Current Rate: " + formatRate(stats.getCurrentRate()) + "/s"), 
                8, 50, 0x404040);
            
            this.font.draw(poseStack, 
                Component.literal("Peak Rate: " + formatRate(stats.getPeakRate(NetworkStatistics.TransferType.ITEMS))), 
                8, 60, 0x404040);
            
            this.font.draw(poseStack, 
                Component.literal("Average Rate: " + formatRate(stats.getAverageRate())), 
                8, 70, 0x404040);
            
            this.font.draw(poseStack, 
                Component.literal("Uptime: " + formatTime(stats.getUptime())), 
                8, 80, 0x404040);
            
            this.font.draw(poseStack, 
                Component.literal("Status: " + (stats.isActive() ? "§aActive" : "§7Idle")), 
                8, 90, 0x404040);
        }
        
        // Node list header
        this.font.draw(poseStack, 
            Component.literal("Connected Adapters:"), 
            8, 105, 0x404040);
        
        // Node list
        List<ControllerBlockEntity.NetworkNodeInfo> nodes = be.getNodes();
        int yOffset = 115;
        int maxNodes = 8;
        for (int i = 0; i < Math.min(nodes.size(), maxNodes); i++) {
            ControllerBlockEntity.NetworkNodeInfo node = nodes.get(i);
            
            String status = node.active ? "§a●" : "§7●";
            String pos = String.format("%d, %d, %d", 
                node.position.getX(), 
                node.position.getY(), 
                node.position.getZ());
            
            this.font.draw(poseStack, 
                Component.literal(status + " " + pos), 
                8, yOffset, 0x404040);
            
            if (node.metrics != null) {
                String rate = formatRate(node.metrics.getTotal());
                this.font.draw(poseStack, 
                    Component.literal(rate), 
                    160, yOffset, 0x808080);
            }
            
            yOffset += 10;
        }
        
        if (nodes.size() > maxNodes) {
            this.font.draw(poseStack, 
                Component.literal("...and " + (nodes.size() - maxNodes) + " more"), 
                8, yOffset, 0x808080);
        }
    }
    
    /**
     * Format transfer rate
     */
    private String formatRate(long rate) {
        if (rate >= 1_000_000) {
            return String.format("%.1fM", rate / 1_000_000.0);
        } else if (rate >= 1_000) {
            return String.format("%.1fk", rate / 1_000.0);
        }
        return String.valueOf(rate);
    }
    
    /**
     * Format uptime
     */
    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
}
