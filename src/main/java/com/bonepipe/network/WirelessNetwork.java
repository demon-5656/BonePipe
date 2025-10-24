package com.bonepipe.network;

import com.bonepipe.BonePipe;
import com.bonepipe.transfer.TransferScheduler;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a single wireless network of connected adapters
 * All adapters on the same frequency communicate through this network
 */
public class WirelessNetwork {
    
    private final FrequencyKey key;
    private final Set<NetworkNode> nodes = ConcurrentHashMap.newKeySet();
    private final TransferScheduler scheduler = new TransferScheduler();
    
    // Statistics
    private long totalTransfers = 0;
    private long lastTickTransfers = 0;
    
    // Optimization: cache valid nodes
    private List<NetworkNode> cachedValidNodes = null;
    private int cacheAge = 0;
    private static final int CACHE_LIFETIME = 20; // 1 second
    
    // Transfer rate limiter
    private int tickCounter = 0;
    private static final int TRANSFER_INTERVAL = 20; // Transfer every 20 ticks (1 per second)
    
    public WirelessNetwork(FrequencyKey key) {
        this.key = Objects.requireNonNull(key);
        BonePipe.LOGGER.debug("Created new network: {}", key);
    }
    
    /**
     * Add a node to this network
     */
    public void addNode(NetworkNode node) {
        if (nodes.add(node)) {
            invalidateCache();
            BonePipe.LOGGER.debug("Node {} joined network {}", node, key);
        }
    }
    
    /**
     * Remove a node from this network
     */
    public void removeNode(NetworkNode node) {
        if (nodes.remove(node)) {
            invalidateCache();
            BonePipe.LOGGER.debug("Node {} left network {}", node, key);
        }
    }
    
    /**
     * Get all valid nodes (with chunk loaded and adapter exists)
     */
    public List<NetworkNode> getValidNodes() {
        if (cachedValidNodes == null || cacheAge++ > CACHE_LIFETIME) {
            cachedValidNodes = nodes.stream()
                .filter(NetworkNode::isValid)
                .toList();
            cacheAge = 0;
        }
        return cachedValidNodes;
    }
    
    /**
     * Main network tick - called every game tick
     */
    public void tick(Level level) {
        // Clean up invalid nodes
        if (cacheAge == 0) { // Only check periodically
            nodes.removeIf(node -> !node.isValid());
        }
        
        // Skip if network is empty
        if (nodes.isEmpty()) {
            return;
        }
        
        // Rate limit transfers
        tickCounter++;
        if (tickCounter < TRANSFER_INTERVAL) {
            return;
        }
        tickCounter = 0;
        
        // Reset tick counters
        lastTickTransfers = 0;
        
        // Run transfer scheduler
        try {
            lastTickTransfers = scheduler.schedule(this, level);
            totalTransfers += lastTickTransfers;
        } catch (Exception e) {
            BonePipe.LOGGER.error("Error during network tick for {}: {}", key, e.getMessage(), e);
        }
    }
    
    /**
     * Check if this network is empty and should be removed
     */
    public boolean isEmpty() {
        return nodes.isEmpty();
    }
    
    /**
     * Get the network frequency key
     */
    public FrequencyKey getKey() {
        return key;
    }
    
    /**
     * Get total number of nodes
     */
    public int getNodeCount() {
        return nodes.size();
    }
    
    /**
     * Get statistics
     */
    public long getTotalTransfers() {
        return totalTransfers;
    }
    
    public long getLastTickTransfers() {
        return lastTickTransfers;
    }
    
    /**
     * Invalidate cached data
     */
    private void invalidateCache() {
        cachedValidNodes = null;
        cacheAge = 0;
    }
    
    @Override
    public String toString() {
        return String.format("WirelessNetwork[%s, %d nodes, %d transfers]", 
            key, nodes.size(), totalTransfers);
    }
}
