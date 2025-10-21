package com.bonepipe.util;

import com.bonepipe.BonePipe;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.world.ForgeChunkManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages chunk loading tickets for wireless adapters
 * Ensures network operations continue even when chunks are unloaded
 */
public class ChunkLoadManager {
    
    private static final Map<UUID, ForgeChunkManager.TicketHelper> activeTickets = new HashMap<>();
    
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
        ForgeChunkManager.TicketHelper ticket = ForgeChunkManager.forceChunk(
            level, 
            BonePipe.MOD_ID, 
            adapterId, 
            chunkPos, 
            true,  // add ticket
            false  // ticking (false = just loaded, true = ticking)
        );
        
        if (ticket != null) {
            activeTickets.put(adapterId, ticket);
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
        ForgeChunkManager.TicketHelper ticket = activeTickets.remove(adapterId);
        if (ticket != null) {
            ticket.getChunkHelper().releaseTicket();
            BonePipe.LOGGER.info("Chunk loading disabled for adapter {}", adapterId);
        }
    }
    
    /**
     * Check if chunk loading is active for an adapter
     * @param adapterId Unique identifier of the adapter
     * @return true if chunk is being force-loaded
     */
    public static boolean isChunkLoaded(UUID adapterId) {
        return activeTickets.containsKey(adapterId);
    }
    
    /**
     * Clear all chunk tickets (called on server shutdown)
     */
    public static void clearAll() {
        for (UUID adapterId : activeTickets.keySet()) {
            unloadChunk(adapterId);
        }
        activeTickets.clear();
        BonePipe.LOGGER.info("Cleared all chunk loading tickets");
    }
    
    /**
     * Get count of active chunk tickets
     * @return Number of chunks being force-loaded
     */
    public static int getActiveTicketCount() {
        return activeTickets.size();
    }
}
