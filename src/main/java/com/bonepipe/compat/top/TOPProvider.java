package com.bonepipe.compat.top;

import com.bonepipe.blocks.AdapterBlockEntity;
import mcjty.theoneprobe.api.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The One Probe (TOP) Integration
 * Shows information when hovering over blocks with probe
 */
public class TOPProvider implements IProbeInfoProvider {
    
    @Override
    public ResourceLocation getID() {
        return new ResourceLocation("bonepipe", "top_provider");
    }
    
    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, 
                            BlockState blockState, IProbeHitData data) {
        BlockEntity be = level.getBlockEntity(data.getPos());
        
        if (be instanceof AdapterBlockEntity adapter) {
            addAdapterInfo(probeInfo, adapter, mode);
        }
    }
    
    /**
     * Add information for Wireless Adapter
     */
    private void addAdapterInfo(IProbeInfo probeInfo, AdapterBlockEntity adapter, ProbeMode mode) {
        // Basic info (always shown)
        String freq = adapter.getFrequency();
        if (freq != null && !freq.isEmpty()) {
            probeInfo.text(Component.literal("§eFrequency: §f" + freq));
        } else {
            probeInfo.text(Component.literal("§7No frequency set"));
        }
        
        // Owner
        if (adapter.getOwner() != null) {
            probeInfo.text(Component.literal("§7Owner: " + adapter.getOwner().toString().substring(0, 8)));
        }
        
        // Extended info (when sneaking)
        if (mode == ProbeMode.EXTENDED) {
            // Side configuration info
            probeInfo.text(Component.literal("§6Configure sides in GUI"));
        }
    }
}
