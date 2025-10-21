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
                    // Verify player has permission
                    if (adapter.getOwner() == null || adapter.getOwner().equals(player.getUUID())) {
                        adapter.setFrequency(frequency);
                        adapter.setChanged();
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
