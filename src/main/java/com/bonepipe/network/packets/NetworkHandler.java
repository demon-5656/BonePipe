package com.bonepipe.network.packets;

import com.bonepipe.BonePipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network handler for client-server communication
 */
public class NetworkHandler {
    
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(BonePipe.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    
    // Alias for backward compatibility
    public static final SimpleChannel CHANNEL = INSTANCE;
    
    private static int packetId = 0;
    
    /**
     * Register all network packets
     */
    public static void register() {
        // Client -> Server packets
        INSTANCE.registerMessage(
            nextId(),
            UpdateFrequencyPacket.class,
            UpdateFrequencyPacket::encode,
            UpdateFrequencyPacket::decode,
            UpdateFrequencyPacket::handle
        );
        
        INSTANCE.registerMessage(
            nextId(),
            UpdateAccessModePacket.class,
            UpdateAccessModePacket::encode,
            UpdateAccessModePacket::decode,
            UpdateAccessModePacket::handle
        );
        
        INSTANCE.registerMessage(
            nextId(),
            UpdateSideConfigPacket.class,
            UpdateSideConfigPacket::encode,
            UpdateSideConfigPacket::decode,
            UpdateSideConfigPacket::handle
        );
        
        // Server -> Client packets
        INSTANCE.registerMessage(
            nextId(),
            SyncAdapterDataPacket.class,
            SyncAdapterDataPacket::encode,
            SyncAdapterDataPacket::decode,
            SyncAdapterDataPacket::handle
        );
        
        BonePipe.LOGGER.info("Registered {} network packets", packetId);
    }
    
    private static int nextId() {
        return packetId++;
    }
}
