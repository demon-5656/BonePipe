package com.bonepipe.compat.top;

import com.bonepipe.blocks.AdapterBlockEntity;
import com.bonepipe.blocks.ControllerBlockEntity;
import com.bonepipe.network.NetworkManager;
import com.bonepipe.network.NetworkStatistics;
import mcjty.theoneprobe.api.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The One Probe (TOP) Integration
 * Shows information when hovering over blocks with probe
 */
public class TOPProvider implements IProbeInfoProvider {
    
    @Override
    public ResourceLocation getID() {
        return new ResourceLocation("bonepipe", "top_provider");
    }
    
    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, 
                            BlockState blockState, IProbeHitData data) {
        BlockEntity be = level.getBlockEntity(data.getPos());
        
        if (be instanceof AdapterBlockEntity adapter) {
            addAdapterInfo(probeInfo, adapter, mode);
        } else if (be instanceof ControllerBlockEntity controller) {
            addControllerInfo(probeInfo, controller, mode);
        }
    }
    
    /**
     * Add information for Wireless Adapter
     */
    private void addAdapterInfo(IProbeInfo probeInfo, AdapterBlockEntity adapter, ProbeMode mode) {
        // Basic info (always shown)
        probeInfo.text(Component.literal("§eFrequency: §f" + adapter.getFrequency()));
        
        // Connection status
        String connectedMachine = adapter.getConnectedMachineName();
        if (connectedMachine != null && !connectedMachine.isEmpty()) {
            probeInfo.text(Component.literal("§aConnected: §f" + connectedMachine));
        } else {
            probeInfo.text(Component.literal("§7Not connected to machine"));
        }
        
        // Transfer rate (if transferring)
        AdapterBlockEntity.TransferMetrics metrics = adapter.getMetrics();
        if (metrics != null) {
            long itemRate = metrics.getItemsPerSecond();
            long fluidRate = metrics.getFluidsPerSecond();
            long energyRate = metrics.getEnergyPerSecond();
            
            if (itemRate > 0) {
                probeInfo.text(Component.literal("§bItems: §f" + formatRate(itemRate) + "/s"));
            }
            if (fluidRate > 0) {
                probeInfo.text(Component.literal("§9Fluids: §f" + formatRate(fluidRate) + " mB/s"));
            }
            if (energyRate > 0) {
                probeInfo.text(Component.literal("§cEnergy: §f" + formatRate(energyRate) + " FE/s"));
            }
        }
        
        // Extended info (when sneaking)
        if (mode == ProbeMode.EXTENDED) {
            // Upgrades
            int speedUpgrades = adapter.countUpgrade(com.bonepipe.items.UpgradeType.SPEED);
            int stackUpgrades = adapter.countUpgrade(com.bonepipe.items.UpgradeType.STACK);
            int filterUpgrades = adapter.countUpgrade(com.bonepipe.items.UpgradeType.FILTER);
            int capacityUpgrades = adapter.countUpgrade(com.bonepipe.items.UpgradeType.CAPACITY);
            
            if (speedUpgrades > 0 || stackUpgrades > 0 || filterUpgrades > 0 || capacityUpgrades > 0) {
                probeInfo.text(Component.literal("§6Upgrades:"));
                if (speedUpgrades > 0) {
                    double multiplier = Math.pow(2.0, speedUpgrades);
                    probeInfo.text(Component.literal("  §7Speed: x" + String.format("%.1f", multiplier)));
                }
                if (stackUpgrades > 0) {
                    int bonus = stackUpgrades * 8;
                    probeInfo.text(Component.literal("  §7Stack: +" + bonus));
                }
                if (filterUpgrades > 0) {
                    int slots = 1 + (capacityUpgrades * 9);
                    probeInfo.text(Component.literal("  §7Filter: " + slots + " slots"));
                }
            }
            
            // Total transfers
            if (metrics != null) {
                probeInfo.text(Component.literal("§7Total Transferred:"));
                if (metrics.getTotalItems() > 0) {
                    probeInfo.text(Component.literal("  §bItems: " + formatLarge(metrics.getTotalItems())));
                }
                if (metrics.getTotalFluids() > 0) {
                    probeInfo.text(Component.literal("  §9Fluids: " + formatLarge(metrics.getTotalFluids()) + " mB"));
                }
                if (metrics.getTotalEnergy() > 0) {
                    probeInfo.text(Component.literal("  §cEnergy: " + formatLarge(metrics.getTotalEnergy()) + " FE"));
                }
            }
        }
    }
    
    /**
     * Add information for Network Controller
     */
    private void addControllerInfo(IProbeInfo probeInfo, ControllerBlockEntity controller, ProbeMode mode) {
        // Basic info
        probeInfo.text(Component.literal("§eFrequency: §f" + controller.getFrequency()));
        
        // Network stats
        var network = NetworkManager.getNetwork(controller.getFrequency(), controller.getOwner());
        if (network != null) {
            int activeNodes = network.getActiveAdapters().size();
            int totalNodes = network.getAllAdapters().size();
            probeInfo.text(Component.literal("§aNodes: §f" + activeNodes + "/" + totalNodes));
        } else {
            probeInfo.text(Component.literal("§7No network found"));
        }
        
        // Statistics
        var stats = NetworkStatistics.getStats(controller.getFrequency(), controller.getOwner());
        if (stats != null && mode == ProbeMode.EXTENDED) {
            probeInfo.text(Component.literal("§6Network Statistics:"));
            
            long currentRate = stats.getCurrentRate();
            if (currentRate > 0) {
                probeInfo.text(Component.literal("  §7Current: " + formatRate(currentRate) + "/s"));
            }
            
            long peakRate = stats.getPeakRate();
            if (peakRate > 0) {
                probeInfo.text(Component.literal("  §7Peak: " + formatRate(peakRate) + "/s"));
            }
            
            long avgRate = stats.getAverageRate();
            if (avgRate > 0) {
                probeInfo.text(Component.literal("  §7Average: " + formatRate(avgRate) + "/s"));
            }
            
            long uptime = stats.getUptime();
            if (uptime > 0) {
                probeInfo.text(Component.literal("  §7Uptime: " + formatTime(uptime)));
            }
        }
    }
    
    /**
     * Format transfer rate with k/M suffixes
     */
    private String formatRate(long rate) {
        if (rate >= 1_000_000) {
            return String.format("%.2fM", rate / 1_000_000.0);
        } else if (rate >= 1_000) {
            return String.format("%.1fk", rate / 1_000.0);
        }
        return String.valueOf(rate);
    }
    
    /**
     * Format large numbers with k/M/B suffixes
     */
    private String formatLarge(long value) {
        if (value >= 1_000_000_000) {
            return String.format("%.2fB", value / 1_000_000_000.0);
        } else if (value >= 1_000_000) {
            return String.format("%.2fM", value / 1_000_000.0);
        } else if (value >= 1_000) {
            return String.format("%.1fk", value / 1_000.0);
        }
        return String.valueOf(value);
    }
    
    /**
     * Format uptime as h/m/s
     */
    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        }
        return secs + "s";
    }
}
