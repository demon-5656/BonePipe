package com.bonepipe.network;

import com.bonepipe.BonePipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global manager for all wireless networks
 * Singleton pattern - one instance per server
 */
@Mod.EventBusSubscriber(modid = BonePipe.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NetworkManager {
    
    private static NetworkManager INSTANCE;
    
    // All networks by frequency key
    private final Map<FrequencyKey, WirelessNetwork> networks = new ConcurrentHashMap<>();
    
    // Performance monitoring
    private int totalTicks = 0;
    private long totalTimeNs = 0;
    
    private NetworkManager() {
        BonePipe.LOGGER.info("NetworkManager initialized");
    }
    
    /**
     * Get the singleton instance
     */
    public static NetworkManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NetworkManager();
        }
        return INSTANCE;
    }
    
    /**
     * Get or create a network for the given frequency
     */
    public WirelessNetwork getOrCreateNetwork(FrequencyKey key) {
        return networks.computeIfAbsent(key, WirelessNetwork::new);
    }
    
    /**
     * Get an existing network (returns null if doesn't exist)
     */
    public WirelessNetwork getNetwork(FrequencyKey key) {
        return networks.get(key);
    }
    
    /**
     * Remove a network (usually when empty)
     */
    public void removeNetwork(FrequencyKey key) {
        WirelessNetwork removed = networks.remove(key);
        if (removed != null) {
            BonePipe.LOGGER.debug("Removed empty network: {}", key);
        }
    }
    
    /**
     * Main tick event - processes all networks
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        
        NetworkManager manager = getInstance();
        
        // Debug: log tick every 100 ticks
        if (manager.totalTicks % 100 == 0) {
            BonePipe.LOGGER.info("⏰ NetworkManager tick #{}, {} networks active", 
                manager.totalTicks, manager.networks.size());
        }
        
        long startTime = System.nanoTime();
        
        // Tick all networks
        for (ServerLevel level : event.getServer().getAllLevels()) {
            manager.tickNetworks(level);
        }
        
        // Performance tracking
        long elapsed = System.nanoTime() - startTime;
        manager.totalTicks++;
        manager.totalTimeNs += elapsed;
        
        // Log performance every 5 seconds (100 ticks)
        if (manager.totalTicks % 100 == 0 && !manager.networks.isEmpty()) {
            double avgMs = (manager.totalTimeNs / manager.totalTicks) / 1_000_000.0;
            BonePipe.LOGGER.debug("Network tick performance: avg {:.3f}ms, {} networks", 
                avgMs, manager.networks.size());
        }
    }
    
    /**
     * Tick all networks in a level
     */
    private void tickNetworks(Level level) {
        if (networks.isEmpty()) {
            return;
        }
        
        // Process each network
        networks.values().forEach(network -> {
            try {
                network.tick(level);
                
                // Clean up empty networks
                if (network.isEmpty()) {
                    removeNetwork(network.getKey());
                }
            } catch (Exception e) {
                BonePipe.LOGGER.error("Error ticking network {}: {}", 
                    network.getKey(), e.getMessage(), e);
            }
        });
    }
    
    /**
     * Clean up on server stop
     */
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        NetworkManager manager = getInstance();
        BonePipe.LOGGER.info("Shutting down NetworkManager with {} networks", 
            manager.networks.size());
        manager.networks.clear();
    }
    
    /**
     * Get statistics
     */
    public int getNetworkCount() {
        return networks.size();
    }
    
    public int getTotalNodeCount() {
        return networks.values().stream()
            .mapToInt(WirelessNetwork::getNodeCount)
            .sum();
    }
    
    @Override
    public String toString() {
        return String.format("NetworkManager[%d networks, %d nodes]", 
            getNetworkCount(), getTotalNodeCount());
    }
}
