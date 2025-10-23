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
    
    // Side configurations (6 directions)
    private Map<Direction, SideConfig> sideConfigs = new HashMap<>();
    
    // Connection state
    private BlockEntity connectedMachine = null;
    private Direction machineDirection = null;
    private int connectionCheckCooldown = 0;
    
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
        
        // Initialize side configs
        for (Direction dir : Direction.values()) {
            sideConfigs.put(dir, new SideConfig());
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
        
        // If machine changed, invalidate capability cache
        if (connectedMachine != oldMachine) {
            invalidateCapabilityCache();
            
            if (connectedMachine != null) {
                BonePipe.LOGGER.debug("Adapter at {} connected to machine at {} (side: {})", 
                    worldPosition, connectedMachine.getBlockPos(), machineDirection);
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
        
        // Save side configs
        CompoundTag sideConfigsTag = new CompoundTag();
        for (Direction dir : Direction.values()) {
            SideConfig config = sideConfigs.get(dir);
            if (config != null) {
                CompoundTag sideTag = new CompoundTag();
                sideTag.putBoolean("enabled", config.enabled);
                sideTag.putString("mode", config.mode.name());
                sideConfigsTag.put(dir.getName(), sideTag);
            }
        }
        tag.put("sideConfigs", sideConfigsTag);
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
            if (wasEnabled && !level.isClientSide()) {
                enableChunkLoading();
            }
        }
        
        // Load side configs
        if (tag.contains("sideConfigs")) {
            CompoundTag sideConfigsTag = tag.getCompound("sideConfigs");
            for (Direction dir : Direction.values()) {
                if (sideConfigsTag.contains(dir.getName())) {
                    CompoundTag sideTag = sideConfigsTag.getCompound(dir.getName());
                    SideConfig config = sideConfigs.get(dir);
                    if (config == null) {
                        config = new SideConfig();
                        sideConfigs.put(dir, config);
                    }
                    config.enabled = sideTag.getBoolean("enabled");
                    if (sideTag.contains("mode")) {
                        config.mode = SideConfig.TransferMode.valueOf(sideTag.getString("mode"));
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
            setChanged();
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
            setChanged();
        }
    }

    public AccessMode getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(AccessMode mode) {
        this.accessMode = mode;
        setChanged();
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
        return MachineDetector.getMachineName(connectedMachine);
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
     * Check if adapter is enabled (has machine connected)
     */
    public boolean isEnabled() {
        return connectedMachine != null && !frequency.isEmpty();
    }
    
    /**
     * Get side configuration for a direction
     */
    public SideConfig getSideConfig(Direction direction) {
        return sideConfigs.get(direction);
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

    public static class SideConfig {
        public boolean enabled = false;
        public TransferMode mode = TransferMode.DISABLED;
        
        // Item filtering
        private java.util.List<net.minecraft.world.item.ItemStack> itemWhitelist = new java.util.ArrayList<>();
        private boolean itemWhitelistEnabled = false;
        
        // Fluid filtering
        private java.util.Set<net.minecraft.resources.ResourceLocation> fluidWhitelist = new java.util.HashSet<>();
        private boolean fluidWhitelistEnabled = false;

        public enum TransferMode {
            DISABLED,
            INPUT,
            OUTPUT,
            BOTH
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
}
