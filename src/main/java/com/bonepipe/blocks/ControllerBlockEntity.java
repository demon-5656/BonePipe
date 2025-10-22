package com.bonepipe.blocks;

import com.bonepipe.BonePipe;
import com.bonepipe.gui.ControllerMenu;
import com.bonepipe.network.FrequencyKey;
import com.bonepipe.network.NetworkManager;
import com.bonepipe.network.NetworkNode;
import com.bonepipe.network.NetworkStatistics;
import com.bonepipe.network.WirelessNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Block Entity for Network Controller
 * Manages and monitors entire wireless network on a frequency
 */
public class ControllerBlockEntity extends BlockEntity implements MenuProvider {
    
    // Network configuration
    private String frequency = "";
    private UUID owner = null;
    private AdapterBlockEntity.AccessMode accessMode = AdapterBlockEntity.AccessMode.PRIVATE;
    
    // Network state
    private boolean registered = false;
    private int tickCounter = 0;
    
    // Cached network data
    private List<NetworkNodeInfo> cachedNodes = new ArrayList<>();
    private NetworkStatistics.FrequencyStats cachedStats = null;
    private long lastUpdateTime = 0;
    
    public ControllerBlockEntity(BlockPos pos, BlockState state) {
        super(com.bonepipe.core.Registration.CONTROLLER_BE.get(), pos, state);
    }
    
    /**
     * Main tick logic
     */
    public void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }
        
        tickCounter++;
        
        // Update every 20 ticks (once per second)
        if (tickCounter % 20 == 0) {
            updateNetworkData();
            updateBlockState();
        }
    }
    
    /**
     * Update cached network data
     */
    private void updateNetworkData() {
        if (frequency.isEmpty() || owner == null) {
            cachedNodes.clear();
            cachedStats = null;
            return;
        }
        
        // Get network from manager
        FrequencyKey key = new FrequencyKey(owner, frequency);
        WirelessNetwork network = NetworkManager.getInstance().getNetwork(key);
        if (network != null) {
            // Update node list
            cachedNodes.clear();
            for (NetworkNode node : network.getValidNodes()) {
                if (node.isValid()) {
                    AdapterBlockEntity adapter = node.getAdapter();
                    if (adapter != null) {
                        cachedNodes.add(new NetworkNodeInfo(
                            adapter.getBlockPos(),
                            adapter.getMachineDirection() != null,
                            adapter.isActive(),
                            adapter.getMetrics()
                        ));
                    }
                }
            }
            
            // Update statistics (simplified - stats tracking can be added later)
            // cachedStats = null; // TODO: integrate with NetworkStatistics properly
            
            lastUpdateTime = System.currentTimeMillis();
            setChanged();
        }
    }
    
    /**
     * Update block state based on network activity
     */
    private void updateBlockState() {
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            boolean shouldBeActive = !frequency.isEmpty() && !cachedNodes.isEmpty();
            
            if (state.getValue(ControllerBlock.ACTIVE) != shouldBeActive) {
                level.setBlock(worldPosition, 
                    state.setValue(ControllerBlock.ACTIVE, shouldBeActive),
                    Block.UPDATE_CLIENTS);
            }
        }
    }
    
    /**
     * Called when block is removed
     */
    public void onRemoved() {
        // Controller doesn't affect network operation, just monitoring
        BonePipe.LOGGER.info("Controller at {} removed", worldPosition);
    }
    
    // ========== GETTERS & SETTERS ==========
    
    public String getFrequency() {
        return frequency;
    }
    
    public void setFrequency(String frequency) {
        if (!this.frequency.equals(frequency)) {
            this.frequency = frequency;
            cachedNodes.clear();
            setChanged();
        }
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }
    
    public AdapterBlockEntity.AccessMode getAccessMode() {
        return accessMode;
    }
    
    public void setAccessMode(AdapterBlockEntity.AccessMode mode) {
        this.accessMode = mode;
        setChanged();
    }
    
    public List<NetworkNodeInfo> getNodes() {
        return new ArrayList<>(cachedNodes);
    }
    
    public NetworkStatistics.FrequencyStats getStats() {
        return cachedStats;
    }
    
    public int getNodeCount() {
        return cachedNodes.size();
    }
    
    public int getActiveNodeCount() {
        return (int) cachedNodes.stream().filter(n -> n.active).count();
    }
    
    // ========== NBT SAVE/LOAD ==========
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Frequency", frequency);
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
        tag.putString("AccessMode", accessMode.name());
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        frequency = tag.getString("Frequency");
        if (tag.hasUUID("Owner")) {
            owner = tag.getUUID("Owner");
        }
        if (tag.contains("AccessMode")) {
            try {
                accessMode = AdapterBlockEntity.AccessMode.valueOf(tag.getString("AccessMode"));
            } catch (IllegalArgumentException e) {
                accessMode = AdapterBlockEntity.AccessMode.PRIVATE;
            }
        }
    }
    
    // ========== MENU PROVIDER ==========
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.bonepipe.controller");
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ControllerMenu(containerId, playerInventory, this);
    }
    
    // ========== NETWORK NODE INFO ==========
    
    /**
     * Information about a network node (adapter)
     */
    public static class NetworkNodeInfo {
        public final BlockPos position;
        public final boolean hasConnection;
        public final boolean active;
        public final AdapterBlockEntity.TransferMetrics metrics;
        
        public NetworkNodeInfo(BlockPos position, boolean hasConnection, 
                             boolean active, AdapterBlockEntity.TransferMetrics metrics) {
            this.position = position;
            this.hasConnection = hasConnection;
            this.active = active;
            this.metrics = metrics;
        }
    }
}
