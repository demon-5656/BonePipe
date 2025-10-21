package com.bonepipe.network.packets;

import com.bonepipe.blocks.AdapterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet to update side configuration (Client -> Server)
 * Controls enable/disable and INPUT/OUTPUT modes for each side
 */
public class UpdateSideConfigPacket {
    
    private final BlockPos pos;
    private final Direction side;
    private final boolean enabled;
    private final AdapterBlockEntity.SideConfig.TransferMode mode;
    
    public UpdateSideConfigPacket(BlockPos pos, Direction side, boolean enabled, 
                                 AdapterBlockEntity.SideConfig.TransferMode mode) {
        this.pos = pos;
        this.side = side;
        this.enabled = enabled;
        this.mode = mode;
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeEnum(side);
        buf.writeBoolean(enabled);
        buf.writeEnum(mode);
    }
    
    public static UpdateSideConfigPacket decode(FriendlyByteBuf buf) {
        return new UpdateSideConfigPacket(
            buf.readBlockPos(),
            buf.readEnum(Direction.class),
            buf.readBoolean(),
            buf.readEnum(AdapterBlockEntity.SideConfig.TransferMode.class)
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
                        AdapterBlockEntity.SideConfig config = adapter.getSideConfig(side);
                        if (config != null) {
                            config.enabled = enabled;
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
