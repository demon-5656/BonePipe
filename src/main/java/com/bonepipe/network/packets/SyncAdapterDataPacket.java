package com.bonepipe.network.packets;

import com.bonepipe.blocks.AdapterBlockEntity;
import com.bonepipe.transfer.TransferChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet to sync adapter data to client (Server -> Client)
 */
public class SyncAdapterDataPacket {
    
    private final BlockPos pos;
    private final String frequency;
    private final UUID owner;
    private final AdapterBlockEntity.AccessMode accessMode;
    private final boolean connected;
    private final String machineName;
    private final Map<TransferChannel, AdapterBlockEntity.ChannelConfig.TransferMode> channelModes;
    
    public SyncAdapterDataPacket(BlockPos pos, String frequency, UUID owner, 
                                AdapterBlockEntity.AccessMode accessMode, boolean connected,
                                String machineName, 
                                Map<TransferChannel, AdapterBlockEntity.ChannelConfig.TransferMode> channelModes) {
        this.pos = pos;
        this.frequency = frequency;
        this.owner = owner;
        this.accessMode = accessMode;
        this.connected = connected;
        this.machineName = machineName;
        this.channelModes = channelModes;
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(frequency);
        buf.writeBoolean(owner != null);
        if (owner != null) {
            buf.writeUUID(owner);
        }
        buf.writeEnum(accessMode);
        buf.writeBoolean(connected);
        buf.writeUtf(machineName != null ? machineName : "None");
        
        // Encode channel configurations
        buf.writeInt(channelModes.size());
        for (Map.Entry<TransferChannel, AdapterBlockEntity.ChannelConfig.TransferMode> entry : channelModes.entrySet()) {
            buf.writeEnum(entry.getKey());
            buf.writeEnum(entry.getValue());
        }
    }
    
    public static SyncAdapterDataPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String frequency = buf.readUtf();
        UUID owner = buf.readBoolean() ? buf.readUUID() : null;
        AdapterBlockEntity.AccessMode accessMode = buf.readEnum(AdapterBlockEntity.AccessMode.class);
        boolean connected = buf.readBoolean();
        String machineName = buf.readUtf();
        
        // Decode channel configurations
        Map<TransferChannel, AdapterBlockEntity.ChannelConfig.TransferMode> channelModes = new HashMap<>();
        int channelCount = buf.readInt();
        for (int i = 0; i < channelCount; i++) {
            TransferChannel channel = buf.readEnum(TransferChannel.class);
            AdapterBlockEntity.ChannelConfig.TransferMode mode = buf.readEnum(AdapterBlockEntity.ChannelConfig.TransferMode.class);
            channelModes.put(channel, mode);
        }
        
        return new SyncAdapterDataPacket(pos, frequency, owner, accessMode, connected, machineName, channelModes);
    }
    
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.level.isLoaded(pos)) {
                BlockEntity be = mc.level.getBlockEntity(pos);
                if (be instanceof AdapterBlockEntity adapter) {
                    adapter.setFrequency(frequency);
                    adapter.setOwner(owner);
                    adapter.setAccessMode(accessMode);
                    adapter.setConnectedMachineName(machineName);
                    
                    // Sync channel configurations
                    for (Map.Entry<TransferChannel, AdapterBlockEntity.ChannelConfig.TransferMode> entry : channelModes.entrySet()) {
                        AdapterBlockEntity.ChannelConfig config = adapter.getChannelConfig(entry.getKey());
                        if (config != null) {
                            config.mode = entry.getValue();
                        }
                    }
                    
                    // Update connection status and blockstate
                    if (connected != adapter.isEnabled()) {
                        // Force client to update visuals
                        mc.level.sendBlockUpdated(pos, 
                            adapter.getBlockState(), 
                            adapter.getBlockState(), 3);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
