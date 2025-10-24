package com.bonepipe.network.packets;

import com.bonepipe.blocks.AdapterBlockEntity;
import com.bonepipe.transfer.TransferChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet to update channel configuration (Client -> Server)
 * Controls INPUT/OUTPUT/BOTH/DISABLED modes for each transfer channel
 */
public class UpdateChannelConfigPacket {
    
    private final BlockPos pos;
    private final TransferChannel channel;
    private final AdapterBlockEntity.ChannelConfig.TransferMode mode;
    
    public UpdateChannelConfigPacket(BlockPos pos, TransferChannel channel, 
                                     AdapterBlockEntity.ChannelConfig.TransferMode mode) {
        this.pos = pos;
        this.channel = channel;
        this.mode = mode;
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeEnum(channel);
        buf.writeEnum(mode);
    }
    
    public static UpdateChannelConfigPacket decode(FriendlyByteBuf buf) {
        return new UpdateChannelConfigPacket(
            buf.readBlockPos(),
            buf.readEnum(TransferChannel.class),
            buf.readEnum(AdapterBlockEntity.ChannelConfig.TransferMode.class)
        );
    }
    
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.level.isLoaded(pos)) {
                BlockEntity be = player.level.getBlockEntity(pos);
                if (be instanceof AdapterBlockEntity adapter) {
                    // Verify player has permission (only owner can change config)
                    if (adapter.getOwner() == null || adapter.getOwner().equals(player.getUUID())) {
                        AdapterBlockEntity.ChannelConfig config = adapter.getChannelConfig(channel);
                        if (config != null) {
                            config.mode = mode;
                            adapter.setChanged();
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
