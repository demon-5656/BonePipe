package com.bonepipe.core;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Configuration settings for BonePipe mod
 */
public class Config {
    
    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;
    
    public static final ClientConfig CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;
    
    static {
        Pair<ServerConfig, ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER = serverPair.getLeft();
        SERVER_SPEC = serverPair.getRight();
        
        Pair<ClientConfig, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }
    
    /**
     * Register configuration files
     */
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }
    
    /**
     * Server-side configuration
     */
    public static class ServerConfig {
        
        // Performance settings
        public final ForgeConfigSpec.IntValue maxTransfersPerTick;
        public final ForgeConfigSpec.IntValue maxPairsPerChannel;
        public final ForgeConfigSpec.IntValue ticksBetweenTransfers;
        
        // Transfer limits
        public final ForgeConfigSpec.IntValue baseItemTransferRate;
        public final ForgeConfigSpec.IntValue baseFluidTransferRate;
        public final ForgeConfigSpec.IntValue baseEnergyTransferRate;
        
        // Network settings
        public final ForgeConfigSpec.IntValue baseWirelessRange;
        public final ForgeConfigSpec.BooleanValue allowCrossDimensional;
        public final ForgeConfigSpec.BooleanValue requireLineOfSight;
        
        public ServerConfig(ForgeConfigSpec.Builder builder) {
            builder.push("performance");
            maxTransfersPerTick = builder
                .comment("Maximum number of transfers per tick across all networks")
                .defineInRange("maxTransfersPerTick", 100, 1, 10000);
            maxPairsPerChannel = builder
                .comment("Maximum number of transfer pairs per channel per tick")
                .defineInRange("maxPairsPerChannel", 20, 1, 1000);
            ticksBetweenTransfers = builder
                .comment("Minimum ticks between transfers for the same node")
                .defineInRange("ticksBetweenTransfers", 1, 1, 20);
            builder.pop();
            
            builder.push("transfer_rates");
            baseItemTransferRate = builder
                .comment("Base item transfer rate (items per operation) - Mekanism Ultimate tier: 64")
                .defineInRange("baseItemTransferRate", 64, 1, 64 * 9);
            baseFluidTransferRate = builder
                .comment("Base fluid transfer rate (mB per operation) - Mekanism Ultimate tier: 64000")
                .defineInRange("baseFluidTransferRate", 64000, 1, 256000);
            baseEnergyTransferRate = builder
                .comment("Base energy transfer rate (FE per operation) - Mekanism Ultimate tier: 204800")
                .defineInRange("baseEnergyTransferRate", 204800, 1, 1000000);
            builder.pop();
            
            builder.push("network");
            baseWirelessRange = builder
                .comment("Base wireless range in blocks (0 = unlimited)")
                .defineInRange("baseWirelessRange", 0, 0, 1024);
            allowCrossDimensional = builder
                .comment("Allow adapters to connect across dimensions")
                .define("allowCrossDimensional", true);
            requireLineOfSight = builder
                .comment("Require line of sight for wireless connection")
                .define("requireLineOfSight", false);
            builder.pop();
        }
    }
    
    /**
     * Client-side configuration
     */
    public static class ClientConfig {
        
        public final ForgeConfigSpec.BooleanValue renderParticles;
        public final ForgeConfigSpec.DoubleValue soundVolume;
        public final ForgeConfigSpec.BooleanValue showDebugInfo;
        public final ForgeConfigSpec.BooleanValue compactTooltips;
        
        public ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.push("visuals");
            renderParticles = builder
                .comment("Render transfer particles")
                .define("renderParticles", true);
            soundVolume = builder
                .comment("Volume multiplier for BonePipe sounds")
                .defineInRange("soundVolume", 1.0, 0.0, 2.0);
            builder.pop();
            
            builder.push("ui");
            showDebugInfo = builder
                .comment("Show debug information in GUI")
                .define("showDebugInfo", false);
            compactTooltips = builder
                .comment("Use compact tooltips")
                .define("compactTooltips", false);
            builder.pop();
        }
    }
}
