package com.bonepipe.network;

import com.bonepipe.blocks.AdapterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * Represents a single adapter node in the wireless network
 * Wraps the BlockEntity with network-specific data
 */
public class NetworkNode {
    
    private final Level level;
    private final BlockPos position;
    private final FrequencyKey frequencyKey;
    
    // Cached reference to avoid repeated lookups
    private AdapterBlockEntity cachedAdapter;
    private int cacheAge = 0;
    private static final int CACHE_LIFETIME = 20; // 1 second
    
    public NetworkNode(Level level, BlockPos position, FrequencyKey frequencyKey) {
        this.level = Objects.requireNonNull(level);
        this.position = Objects.requireNonNull(position);
        this.frequencyKey = Objects.requireNonNull(frequencyKey);
    }
    
    /**
     * Get the adapter BlockEntity, with caching
     */
    public AdapterBlockEntity getAdapter() {
        if (cachedAdapter == null || cacheAge++ > CACHE_LIFETIME) {
            if (level.getBlockEntity(position) instanceof AdapterBlockEntity adapter) {
                cachedAdapter = adapter;
                cacheAge = 0;
            }
        }
        return cachedAdapter;
    }
    
    /**
     * Check if this node is still valid (chunk loaded, adapter exists)
     */
    public boolean isValid() {
        return level.hasChunkAt(position) && getAdapter() != null;
    }
    
    /**
     * Get the position of this node
     */
    public BlockPos getPosition() {
        return position;
    }
    
    /**
     * Get the level this node is in
     */
    public Level getLevel() {
        return level;
    }
    
    /**
     * Get the frequency key
     */
    public FrequencyKey getFrequencyKey() {
        return frequencyKey;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetworkNode)) return false;
        NetworkNode that = (NetworkNode) o;
        return position.equals(that.position) && 
               level.dimension().equals(that.level.dimension());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(position, level.dimension());
    }
    
    @Override
    public String toString() {
        return String.format("NetworkNode[%s in %s]", 
            position.toShortString(), 
            level.dimension().location());
    }
}
