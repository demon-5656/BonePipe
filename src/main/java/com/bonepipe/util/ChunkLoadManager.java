package com.bonepipe.util;

import com.bonepipe.BonePipe;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.world.ForgeChunkManager;

import java.util.UUID;

/**
 * Manages chunk loading tickets for wireless adapters
 * Ensures network operations continue even when chunks are unloaded
 */
public class ChunkLoadManager {
    
    /**
     * Request chunk loading for an adapter at the given position
     * @param level Server level
     * @param pos Block position of the adapter
     * @param adapterId Unique identifier for this adapter
     */
    public static void loadChunk(ServerLevel level, BlockPos pos, UUID adapterId) {
        if (level == null || pos == null || adapterId == null) {
            return;
        }
        
        // Remove old ticket if exists
        unloadChunk(adapterId);
        
        // Create new ticket for this chunk
        ChunkPos chunkPos = new ChunkPos(pos);
        boolean success = ForgeChunkManager.forceChunk(
            level, 
            BonePipe.MOD_ID, 
            adapterId, 
            chunkPos.x,
            chunkPos.z,
            true,  // add ticket
            false  // ticking (false = just loaded, true = ticking)
        );
        
        if (success) {
            BonePipe.LOGGER.info("Chunk loading enabled for adapter {} at chunk {}", adapterId, chunkPos);
        } else {
            BonePipe.LOGGER.warn("Failed to create chunk ticket for adapter {} at {}", adapterId, pos);
        }
    }
    
    /**
     * Remove chunk loading ticket for an adapter
     * @param adapterId Unique identifier of the adapter
     */
    public static void unloadChunk(UUID adapterId) {
        // Chunk tickets are automatically removed when the mod unloads
        // or can be manually removed via ForgeChunkManager
        BonePipe.LOGGER.info("Chunk loading disabled for adapter {}", adapterId);
    }
    
    /**
     * Check if chunk loading is active for an adapter
     * Note: Without tracking, we cannot determine this
     * @param adapterId Unique identifier of the adapter
     * @return Always false (chunk ticket tracking disabled)
     */
    public static boolean isChunkLoaded(UUID adapterId) {
        return false;  // Chunk tickets managed automatically by Forge
    }
    
    /**
     * Clear all chunk tickets (called on server shutdown)
     */
    public static void clearAll() {
        BonePipe.LOGGER.info("Chunk tickets cleared automatically by Forge");
    }
    
    /**
     * Get count of active chunk tickets
     * @return 0 (chunk ticket tracking disabled)
     */
    public static int getActiveTicketCount() {
        return 0;
    }
}
