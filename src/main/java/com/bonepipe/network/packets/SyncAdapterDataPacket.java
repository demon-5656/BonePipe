package com.bonepipe.network.packets;

import com.bonepipe.blocks.AdapterBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

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
    
    public SyncAdapterDataPacket(BlockPos pos, String frequency, UUID owner, 
                                AdapterBlockEntity.AccessMode accessMode, boolean connected) {
        this.pos = pos;
        this.frequency = frequency;
        this.owner = owner;
        this.accessMode = accessMode;
        this.connected = connected;
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
    }
    
    public static SyncAdapterDataPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String frequency = buf.readUtf();
        UUID owner = buf.readBoolean() ? buf.readUUID() : null;
        AdapterBlockEntity.AccessMode accessMode = buf.readEnum(AdapterBlockEntity.AccessMode.class);
        boolean connected = buf.readBoolean();
        
        return new SyncAdapterDataPacket(pos, frequency, owner, accessMode, connected);
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
