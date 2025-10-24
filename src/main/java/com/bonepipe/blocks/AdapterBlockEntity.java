package com.bonepipe.blocks;

import com.bonepipe.BonePipe;
import com.bonepipe.gui.AdapterMenu;
import com.bonepipe.network.FrequencyKey;
import com.bonepipe.network.NetworkManager;
import com.bonepipe.network.NetworkNode;
import com.bonepipe.network.NetworkStatistics;
import com.bonepipe.network.WirelessNetwork;
import com.bonepipe.util.ChunkLoadManager;
import com.bonepipe.util.MachineDetector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Block Entity for Adapter - contains all logic and configuration
 */
public class AdapterBlockEntity extends BlockEntity implements MenuProvider {
    
    // Network configuration
    private String frequency = "";
    private UUID owner = null;
    private AccessMode accessMode = AccessMode.PRIVATE;
    
    // Channel configurations (per transfer type)
    private Map<com.bonepipe.transfer.TransferChannel, ChannelConfig> channelConfigs = new HashMap<>();
    
    // Connection state
    private BlockEntity connectedMachine = null;
    private Direction machineDirection = null;
    private int connectionCheckCooldown = 0;
    private String connectedMachineName = "None"; // Cached for client display
    
    // Capability caching for performance
    private LazyOptional<?> cachedItemHandler = LazyOptional.empty();
    private LazyOptional<?> cachedFluidHandler = LazyOptional.empty();
    private LazyOptional<?> cachedEnergyHandler = LazyOptional.empty();
    
    // Network registration
    private boolean registeredInNetwork = false;
    
    // Chunk loading
    private UUID chunkLoadTicketId = null;
    private boolean chunkLoadingEnabled = false;
    
    // Activity tracking
    private int lastTransferTick = 0;
    private long totalTransferred = 0;
    
    // UPGRADES REMOVED IN v3.0.0 - Simplified version
    // No upgrade inventory, no upgrade bonuses, no multipliers
    
    // Transfer metrics
    private long totalItemsTransferred = 0;
    private long totalFluidsTransferred = 0;
    private long totalEnergyTransferred = 0;
    private long lastTransferTime = 0;
    private long peakTransferRate = 0;
    
    // Tick counter
    private int tickCounter = 0;

    public AdapterBlockEntity(BlockPos pos, BlockState state) {
        super(com.bonepipe.core.Registration.ADAPTER_BE.get(), pos, state);
        
        // Initialize channel configs
        for (com.bonepipe.transfer.TransferChannel channel : com.bonepipe.transfer.TransferChannel.values()) {
            channelConfigs.put(channel, new ChannelConfig());
        }
        
    }
    
    // UPGRADES REMOVED - no recalculation needed

    /**
     * Main tick logic - called every game tick (20 TPS)
     */
    public void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }

        tickCounter++;

        // Check machine connection every 20 ticks (once per second)
        if (connectionCheckCooldown <= 0) {
            checkMachineConnection();
            connectionCheckCooldown = 20;
        } else {
            connectionCheckCooldown--;
        }

        // Register in network if we have frequency and owner
        if (connectedMachine != null && !frequency.isEmpty() && owner != null) {
            if (!registeredInNetwork) {
                registerInNetwork();
            }
        } else {
            if (registeredInNetwork) {
                unregisterFromNetwork();
            }
        }
        
        // Update block state for visual feedback
        updateBlockState();
    }
    
    /**
     * Update block state based on current status
     */
    private void updateBlockState() {
        if (level != null && !level.isClientSide()) {
            BlockState currentState = level.getBlockState(worldPosition);
            boolean shouldBeActive = isActive();
            
            if (currentState.getValue(AdapterBlock.ACTIVE) != shouldBeActive) {
                level.setBlock(worldPosition, 
                    currentState.setValue(AdapterBlock.ACTIVE, shouldBeActive), 
                    Block.UPDATE_CLIENTS);
            }
        }
    }
    
    /**
     * Check if we're connected to a machine
     */
    private void checkMachineConnection() {
        BlockEntity oldMachine = connectedMachine;
        Direction oldDirection = machineDirection;
        
        // Find connected machine
        connectedMachine = MachineDetector.findConnectedMachine(level, worldPosition);
        machineDirection = MachineDetector.findMachineDirection(level, worldPosition);
        
        // Update cached machine name on server
        if (level != null && !level.isClientSide()) {
            connectedMachineName = MachineDetector.getMachineName(connectedMachine);
        }
        
        // If machine changed, invalidate capability cache
        if (connectedMachine != oldMachine) {
            invalidateCapabilityCache();
            
            if (connectedMachine != null) {
                BonePipe.LOGGER.debug("Adapter at {} connected to machine: {}", 
                    worldPosition, connectedMachineName);
            } else if (oldMachine != null) {
                BonePipe.LOGGER.debug("Adapter at {} disconnected from machine", worldPosition);
                unregisterFromNetwork();
            }
            setChanged();
        }
    }
    
    /**
     * Invalidate cached capabilities
     */
    private void invalidateCapabilityCache() {
        cachedItemHandler.invalidate();
        cachedFluidHandler.invalidate();
        cachedEnergyHandler.invalidate();
        
        cachedItemHandler = LazyOptional.empty();
        cachedFluidHandler = LazyOptional.empty();
        cachedEnergyHandler = LazyOptional.empty();
    }
    
    /**
     * Get cached item handler capability
     */
    public LazyOptional<?> getCachedItemHandler() {
        if (!cachedItemHandler.isPresent() && connectedMachine != null) {
            cachedItemHandler = connectedMachine.getCapability(
                ForgeCapabilities.ITEM_HANDLER, machineDirection);
        }
        return cachedItemHandler;
    }
    
    /**
     * Get cached fluid handler capability
     */
    public LazyOptional<?> getCachedFluidHandler() {
        if (!cachedFluidHandler.isPresent() && connectedMachine != null) {
            cachedFluidHandler = connectedMachine.getCapability(
                ForgeCapabilities.FLUID_HANDLER, machineDirection);
        }
        return cachedFluidHandler;
    }
    
    /**
     * Get cached energy handler capability
     */
    public LazyOptional<?> getCachedEnergyHandler() {
        if (!cachedEnergyHandler.isPresent() && connectedMachine != null) {
            cachedEnergyHandler = connectedMachine.getCapability(
                ForgeCapabilities.ENERGY, machineDirection);
        }
        return cachedEnergyHandler;
    }
    
    /**
     * Register this adapter in the wireless network
     */
    private void registerInNetwork() {
        if (level == null || owner == null || frequency.isEmpty()) {
            return;
        }

        FrequencyKey key = new FrequencyKey(owner, frequency);
        NetworkNode node = new NetworkNode(level, worldPosition, key);
        
        NetworkManager manager = NetworkManager.getInstance();
        WirelessNetwork network = manager.getOrCreateNetwork(key);
        network.addNode(node);
        
        registeredInNetwork = true;
        
        // Enable chunk loading for this adapter
        enableChunkLoading();
        
        // Play connect sound
        if (!level.isClientSide()) {
            level.playSound(null, worldPosition, 
                com.bonepipe.core.Sounds.CONNECT.get(), 
                net.minecraft.sounds.SoundSource.BLOCKS, 
                0.5f, 1.0f);
        }
        
        BonePipe.LOGGER.info("Adapter registered: {} → Network {} ({} nodes)", 
            worldPosition, key.getFrequency(), network.getNodeCount());
    }

    // Network sync for chunk loading - Mekanism pattern
    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public CompoundTag getUpdateTag() {
        // Send minimal sync data for chunk loading - NOT full NBT!
        // This prevents overwriting saved data during world load
        CompoundTag tag = super.getUpdateTag();
        
        // Only sync client-visible data, not configuration
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
        tag.putString("frequency", frequency);
        tag.putString("connectedMachineName", connectedMachineName);
        
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        // Handle chunk sync data - DON'T call full load()!
        super.handleUpdateTag(tag);
        
        // Only update visual fields from chunk sync
        if (tag.hasUUID("owner")) {
            owner = tag.getUUID("owner");
        }
        if (tag.contains("frequency")) {
            frequency = tag.getString("frequency");
        }
        if (tag.contains("connectedMachineName")) {
            connectedMachineName = tag.getString("connectedMachineName");
        }
    }
    /**
     * Unregister from network
     */
    private void unregisterFromNetwork() {
        if (level == null || owner == null || frequency.isEmpty()) {
            registeredInNetwork = false;
            disableChunkLoading();
            return;
        }

        FrequencyKey key = new FrequencyKey(owner, frequency);
        NetworkNode node = new NetworkNode(level, worldPosition, key);
        
        NetworkManager manager = NetworkManager.getInstance();
        WirelessNetwork network = manager.getNetwork(key);
        if (network != null) {
            network.removeNode(node);
        }
        
        registeredInNetwork = false;
        
        // Disable chunk loading
        disableChunkLoading();
        
        // Play disconnect sound
        if (!level.isClientSide()) {
            level.playSound(null, worldPosition, 
                com.bonepipe.core.Sounds.DISCONNECT.get(), 
                net.minecraft.sounds.SoundSource.BLOCKS, 
                0.5f, 0.8f);
        }
        
        BonePipe.LOGGER.debug("Adapter at {} unregistered from network", worldPosition);
    }
    
    /**
     * Enable chunk loading for this adapter
     */
    private void enableChunkLoading() {
        if (level == null || level.isClientSide() || chunkLoadingEnabled) {
            return;
        }
        
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        
        // Generate unique ticket ID if not exists
        if (chunkLoadTicketId == null) {
            chunkLoadTicketId = UUID.randomUUID();
        }
        
        ChunkLoadManager.loadChunk(serverLevel, worldPosition, chunkLoadTicketId);
        chunkLoadingEnabled = true;
        BonePipe.LOGGER.info("Chunk loading enabled for adapter at {}", worldPosition);
    }
    
    /**
     * Disable chunk loading for this adapter
     */
    private void disableChunkLoading() {
        if (!chunkLoadingEnabled || chunkLoadTicketId == null) {
            return;
        }
        
        ChunkLoadManager.unloadChunk(chunkLoadTicketId);
        chunkLoadingEnabled = false;
        BonePipe.LOGGER.info("Chunk loading disabled for adapter at {}", worldPosition);
    }
    
    /**
     * Called when block is removed
     */
    public void onRemoved() {
        unregisterFromNetwork();
        invalidateCapabilityCache();
        BonePipe.LOGGER.info("Adapter at {} removed from world", worldPosition);
    }

    // NBT Serialization
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("frequency", frequency);
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
        tag.putString("accessMode", accessMode.name());
        // upgradeInventory removed in v3.0.0
        tag.putLong("totalTransferred", totalTransferred);
        
        // Save chunk loading state
        if (chunkLoadTicketId != null) {
            tag.putUUID("chunkLoadTicketId", chunkLoadTicketId);
        }
        tag.putBoolean("chunkLoadingEnabled", chunkLoadingEnabled);
        
        // Save channel configs
        CompoundTag channelConfigsTag = new CompoundTag();
        StringBuilder debugModes = new StringBuilder();
        for (com.bonepipe.transfer.TransferChannel channel : com.bonepipe.transfer.TransferChannel.values()) {
            ChannelConfig config = channelConfigs.get(channel);
            if (config != null) {
                CompoundTag channelTag = new CompoundTag();
                channelTag.putString("mode", config.mode.name());
                channelConfigsTag.put(channel.getId(), channelTag);
                debugModes.append(channel.getId()).append("=").append(config.mode.name()).append(" ");
            }
        }
        tag.put("channelConfigs", channelConfigsTag);
        

    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        frequency = tag.getString("frequency");
        if (tag.hasUUID("owner")) {
            owner = tag.getUUID("owner");
        }
        if (tag.contains("accessMode")) {
            accessMode = AccessMode.valueOf(tag.getString("accessMode"));
        }
        // upgradeInventory removed in v3.0.0
        if (tag.contains("totalTransferred")) {
            totalTransferred = tag.getLong("totalTransferred");
        }
        
        // Load chunk loading state
        if (tag.hasUUID("chunkLoadTicketId")) {
            chunkLoadTicketId = tag.getUUID("chunkLoadTicketId");
        }
        if (tag.contains("chunkLoadingEnabled")) {
            boolean wasEnabled = tag.getBoolean("chunkLoadingEnabled");
            // Re-enable chunk loading if it was enabled before
            if (wasEnabled && level != null && !level.isClientSide()) {
                enableChunkLoading();
            }
        }
        
        // Load channel configs
        StringBuilder debugModes = new StringBuilder();
        if (tag.contains("channelConfigs")) {
            CompoundTag channelConfigsTag = tag.getCompound("channelConfigs");
            for (com.bonepipe.transfer.TransferChannel channel : com.bonepipe.transfer.TransferChannel.values()) {
                if (channelConfigsTag.contains(channel.getId())) {
                    CompoundTag channelTag = channelConfigsTag.getCompound(channel.getId());
                    ChannelConfig config = channelConfigs.get(channel);
                    if (config == null) {
                        config = new ChannelConfig();
                        channelConfigs.put(channel, config);
                    }
                    if (channelTag.contains("mode")) {
                        config.mode = ChannelConfig.TransferMode.valueOf(channelTag.getString("mode"));
                        debugModes.append(channel.getId()).append("=").append(config.mode.name()).append(" ");
                    }
                }
            }
        }
        
    }

    // MenuProvider implementation
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.bonepipe.adapter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AdapterMenu(id, inventory, this);
    }

    // Getters/Setters
    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        if (!this.frequency.equals(frequency)) {
            BonePipe.LOGGER.info("Changing frequency: old='{}', new='{}' at {}", 
                this.frequency, frequency, getBlockPos());
            unregisterFromNetwork();
            this.frequency = frequency;
            registeredInNetwork = false;
            // Only mark changed on server - client shouldn't trigger saves
            if (level != null && !level.isClientSide()) {
                setChanged();
            }
            BonePipe.LOGGER.info("Frequency changed successfully, will re-register in network");
        }
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        if (!java.util.Objects.equals(this.owner, owner)) {
            unregisterFromNetwork();
            this.owner = owner;
            registeredInNetwork = false;
            // Only mark changed on server - client shouldn't trigger saves
            if (level != null && !level.isClientSide()) {
                setChanged();
            }
        }
    }

    public AccessMode getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(AccessMode mode) {
        this.accessMode = mode;
        // Only mark changed on server - client shouldn't trigger saves
        if (level != null && !level.isClientSide()) {
            setChanged();
        }
    }
    
    /**
     * Check if adapter is currently connected to a machine
     */
    public boolean isConnected() {
        return connectedMachine != null;
    }
    
    /**
     * Get the name of the connected machine for display
     */
    public String getConnectedMachineName() {
        if (level != null && level.isClientSide()) {
            // On client, use cached name from sync packet
            return connectedMachineName;
        }
        // On server, get real name
        return MachineDetector.getMachineName(connectedMachine);
    }
    
    /**
     * Set machine name (called from sync packet on client)
     */
    public void setConnectedMachineName(String name) {
        this.connectedMachineName = name;
    }
    
    /**
     * Check if adapter is currently active (transferring resources)
     */
    public boolean isActive() {
        return tickCounter - lastTransferTick < 20; // Active within last second
    }
    
    /**
     * Mark that a transfer occurred
     */
    public void recordTransfer(long amount) {
        lastTransferTick = tickCounter;
        totalTransferred += amount;
        setChanged();
    }
    
    /**
     * Get total amount transferred through this adapter
     */
    public long getTotalTransferred() {
        return totalTransferred;
    }
    
    /**
     * Get connected machine direction
     */
    public Direction getMachineDirection() {
        return machineDirection;
    }
    
    // UPGRADES REMOVED - No upgrade inventory methods
    
    // UPGRADES REMOVED - Fixed base values only
    public double getSpeedMultiplier() { return 1.0; }
    public double getRangeMultiplier() { return 1.0; }
    public int getStackBonus() { return 0; }
    public boolean hasFilterUpgrade() { return false; }
    public int getTotalFilterSlots() { return 0; }
    
    /**
     * Record transfer for metrics tracking
     */
    public void recordTransfer(NetworkStatistics.TransferType type, long amount) {
        switch (type) {
            case ITEMS -> totalItemsTransferred += amount;
            case FLUIDS -> totalFluidsTransferred += amount;
            case ENERGY -> totalEnergyTransferred += amount;
        }
        
        lastTransferTime = System.currentTimeMillis();
        
        // Update peak rate
        if (amount > peakTransferRate) {
            peakTransferRate = amount;
        }
        
        setChanged();
    }
    
    /**
     * Get transfer metrics for statistics display
     */
    public TransferMetrics getMetrics() {
        long uptime = level != null ? level.getGameTime() * 50 : 0; // ticks to ms
        return new TransferMetrics(
            totalItemsTransferred,
            totalFluidsTransferred,
            totalEnergyTransferred,
            lastTransferTime,
            peakTransferRate,
            uptime
        );
    }
    
    /**
     * Container for transfer metrics
     */
    public static class TransferMetrics {
        public final long totalItems;
        public final long totalFluids;
        public final long totalEnergy;
        public final long lastTransferTime;
        public final long peakRate;
        public final long uptime;
        
        public TransferMetrics(long totalItems, long totalFluids, long totalEnergy,
                             long lastTransferTime, long peakRate, long uptime) {
            this.totalItems = totalItems;
            this.totalFluids = totalFluids;
            this.totalEnergy = totalEnergy;
            this.lastTransferTime = lastTransferTime;
            this.peakRate = peakRate;
            this.uptime = uptime;
        }
        
        public boolean isActive() {
            return (System.currentTimeMillis() - lastTransferTime) < 5000;
        }
        
        public long getTotal() {
            return totalItems + totalFluids + totalEnergy;
        }
    }    /**
     * Check if adapter is enabled (has machine connected and frequency set)
     * On client, use synced machine name instead of server-only connectedMachine field
     */
    public boolean isEnabled() {
        if (level != null && level.isClientSide()) {
            // On client, check cached machine name from sync packet
            return connectedMachineName != null && 
                   !connectedMachineName.isEmpty() && 
                   !connectedMachineName.equals("None") && 
                   !frequency.isEmpty();
        }
        // On server, use actual connected machine
        return connectedMachine != null && !frequency.isEmpty();
    }
    
    /**
     * Get channel configuration for a transfer type
     */
    public ChannelConfig getChannelConfig(com.bonepipe.transfer.TransferChannel channel) {
        return channelConfigs.computeIfAbsent(channel, k -> new ChannelConfig());
    }
    
    /**
     * Set channel configuration
     */
    public void setChannelConfig(com.bonepipe.transfer.TransferChannel channel, ChannelConfig config) {
        channelConfigs.put(channel, config);
        // Only mark changed on server - client shouldn't trigger saves
        if (level != null && !level.isClientSide()) {
            setChanged();
        }
    }
    
    /**
     * Get connected machine BlockEntity
     */
    public BlockEntity getConnectedMachine() {
        return connectedMachine;
    }
    
    /**
     * Spawn visual particles when transfer occurs
     */
    private void spawnTransferParticles() {
        if (level != null && level.isClientSide()) {
            // Spawn portal-like particles
            for (int i = 0; i < 3; i++) {
                double offsetX = (level.random.nextDouble() - 0.5) * 0.5;
                double offsetY = level.random.nextDouble() * 0.5;
                double offsetZ = (level.random.nextDouble() - 0.5) * 0.5;
                
                level.addParticle(
                    net.minecraft.core.particles.ParticleTypes.PORTAL,
                    worldPosition.getX() + 0.5 + offsetX,
                    worldPosition.getY() + 0.5 + offsetY,
                    worldPosition.getZ() + 0.5 + offsetZ,
                    (level.random.nextDouble() - 0.5) * 0.1,
                    level.random.nextDouble() * 0.1,
                    (level.random.nextDouble() - 0.5) * 0.1
                );
            }
        }
    }

    // Inner classes
    public enum AccessMode {
        PUBLIC,
        PRIVATE,
        TRUSTED
    }

    /**
     * Configuration for a single transfer channel
     */
    public static class ChannelConfig {
        public TransferMode mode = TransferMode.DISABLED;
        
        // Item filtering (only for ITEMS channel)
        private java.util.List<net.minecraft.world.item.ItemStack> itemWhitelist = new java.util.ArrayList<>();
        private boolean itemWhitelistEnabled = false;
        
        // Fluid filtering (only for FLUIDS channel)
        private java.util.Set<net.minecraft.resources.ResourceLocation> fluidWhitelist = new java.util.HashSet<>();
        private boolean fluidWhitelistEnabled = false;

        public enum TransferMode {
            DISABLED,
            INPUT,
            OUTPUT,
            BOTH
        }
        
        public boolean isEnabled() {
            return mode != TransferMode.DISABLED;
        }
        
        /**
         * Check if an item passes the filter
         */
        public boolean matchesItemFilter(net.minecraft.world.item.ItemStack stack) {
            if (!itemWhitelistEnabled || itemWhitelist.isEmpty()) {
                return true; // No filter = allow all
            }
            
            for (net.minecraft.world.item.ItemStack filter : itemWhitelist) {
                if (net.minecraft.world.item.ItemStack.isSame(stack, filter)) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Check if a fluid passes the filter
         */
        public boolean matchesFluidFilter(net.minecraft.resources.ResourceLocation fluidId) {
            if (!fluidWhitelistEnabled || fluidWhitelist.isEmpty()) {
                return true; // No filter = allow all
            }
            return fluidWhitelist.contains(fluidId);
        }
    }
    
    /**
     * @deprecated Use ChannelConfig instead. This is kept for backward compatibility.
     */
    @Deprecated
    public static class SideConfig {
        public boolean enabled = false;
        public TransferMode mode = TransferMode.DISABLED;
        
        public enum TransferMode {
            DISABLED,
            INPUT,
            OUTPUT,
            BOTH
        }
    }
    
    /**
     * @deprecated Sides are no longer used. Use getChannelConfig() instead.
     */
    @Deprecated
    public SideConfig getSideConfig(Direction direction) {
        // Return a dummy config for backward compatibility
        SideConfig dummy = new SideConfig();
        dummy.enabled = false;
        dummy.mode = SideConfig.TransferMode.DISABLED;
        return dummy;
    }
}
