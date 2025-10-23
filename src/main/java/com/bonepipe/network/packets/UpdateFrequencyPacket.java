package com.bonepipe.network.packets;

import com.bonepipe.blocks.AdapterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet to update adapter frequency (Client -> Server)
 */
public class UpdateFrequencyPacket {
    
    private final BlockPos pos;
    private final String frequency;
    
    public UpdateFrequencyPacket(BlockPos pos, String frequency) {
        this.pos = pos;
        this.frequency = frequency;
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(frequency);
    }
    
    public static UpdateFrequencyPacket decode(FriendlyByteBuf buf) {
        return new UpdateFrequencyPacket(
            buf.readBlockPos(),
            buf.readUtf()
        );
    }
    
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.level.isLoaded(pos)) {
                BlockEntity be = player.level.getBlockEntity(pos);
                if (be instanceof AdapterBlockEntity adapter) {
                    com.bonepipe.BonePipe.LOGGER.info("Received frequency update packet: pos={}, freq={}, owner={}", 
                        pos, frequency, adapter.getOwner());
                    
                    // Verify player has permission
                    if (adapter.getOwner() == null || adapter.getOwner().equals(player.getUUID())) {
                        adapter.setFrequency(frequency);
                        adapter.setChanged();
                        player.level.sendBlockUpdated(pos, adapter.getBlockState(), adapter.getBlockState(), 3);
                        
                        // Send sync packet back to client
                        NetworkHandler.CHANNEL.sendTo(
                            new SyncAdapterDataPacket(
                                pos,
                                adapter.getFrequency(),
                                adapter.getOwner(),
                                adapter.getAccessMode(),
                                adapter.isEnabled()
                            ),
                            player.connection.connection,
                            net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                        );
                        
                        com.bonepipe.BonePipe.LOGGER.info("Frequency set successfully: {}", frequency);
                    } else {
                        com.bonepipe.BonePipe.LOGGER.warn("Player {} has no permission to change adapter at {}", 
                            player.getDisplayName().getString(), pos);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
