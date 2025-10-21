package com.bonepipe.network.packets;

import com.bonepipe.blocks.AdapterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet to update adapter access mode (Client -> Server)
 */
public class UpdateAccessModePacket {
    
    private final BlockPos pos;
    private final AdapterBlockEntity.AccessMode accessMode;
    
    public UpdateAccessModePacket(BlockPos pos, AdapterBlockEntity.AccessMode accessMode) {
        this.pos = pos;
        this.accessMode = accessMode;
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeEnum(accessMode);
    }
    
    public static UpdateAccessModePacket decode(FriendlyByteBuf buf) {
        return new UpdateAccessModePacket(
            buf.readBlockPos(),
            buf.readEnum(AdapterBlockEntity.AccessMode.class)
        );
    }
    
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.level.isLoaded(pos)) {
                BlockEntity be = player.level.getBlockEntity(pos);
                if (be instanceof AdapterBlockEntity adapter) {
                    // Verify player has permission (only owner can change access mode)
                    if (adapter.getOwner() != null && adapter.getOwner().equals(player.getUUID())) {
                        adapter.setAccessMode(accessMode);
                        adapter.setChanged();
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
