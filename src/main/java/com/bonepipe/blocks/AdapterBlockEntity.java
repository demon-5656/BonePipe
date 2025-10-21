package com.bonepipe.blocks;

import com.bonepipe.BonePipe;
import com.bonepipe.gui.AdapterMenu;
import com.bonepipe.items.UpgradeCardItem;
import com.bonepipe.network.FrequencyKey;
import com.bonepipe.network.NetworkManager;
import com.bonepipe.network.NetworkNode;
import com.bonepipe.network.WirelessNetwork;
import com.bonepipe.util.MachineDetector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
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
    
    // Activity tracking
    private int lastTransferTick = 0;
    private long totalTransferred = 0;
    
    // Upgrade card inventory (4 slots)
    private final ItemStackHandler upgradeInventory = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            recalculateUpgrades();
        }
        
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return UpgradeCardItem.isUpgradeCard(stack);
        }
    };
    
    // Calculated upgrade bonuses
    private double speedMultiplier = 1.0;
    private double rangeMultiplier = 1.0;
    private int stackBonus = 0;
    private boolean hasFilter = false;
    
    // Tick counter
    private int tickCounter = 0;

    public AdapterBlockEntity(BlockPos pos, BlockState state) {
        super(com.bonepipe.core.Registration.ADAPTER_BE.get(), pos, state);
        
        // Initialize side configs
        for (Direction dir : Direction.values()) {
            sideConfigs.put(dir, new SideConfig());
        }
    }
    
    /**
     * Recalculate upgrade bonuses from installed cards
     */
    private void recalculateUpgrades() {
        speedMultiplier = 1.0;
        rangeMultiplier = 1.0;
        stackBonus = 0;
        hasFilter = false;
        
        for (int i = 0; i < upgradeInventory.getSlots(); i++) {
            ItemStack stack = upgradeInventory.getStackInSlot(i);
            UpgradeCardItem.UpgradeType type = UpgradeCardItem.getUpgradeType(stack);
            
            if (type != null) {
                speedMultiplier *= type.speedMultiplier;
                rangeMultiplier += type.rangeMultiplier;
                stackBonus += type.stackBonus;
                if (type == UpgradeCardItem.UpgradeType.FILTER) {
                    hasFilter = true;
                }
            }
        }
        
        BonePipe.LOGGER.debug("Adapter at {} upgrades: speed={}x, range={}x, stack=+{}, filter={}", 
            worldPosition, speedMultiplier, rangeMultiplier, stackBonus, hasFilter);
    }

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
        WirelessNetwork network = NetworkManager.getInstance().getOrCreateNetwork(key);
        NetworkNode node = new NetworkNode(level, worldPosition, key);
        network.addNode(node);
        
        registeredInNetwork = true;
        BonePipe.LOGGER.debug("Adapter at {} registered in network {}", worldPosition, key);
    }
    
    /**
     * Unregister from network
     */
    private void unregisterFromNetwork() {
        if (!registeredInNetwork || owner == null || frequency.isEmpty()) {
            return;
        }
        
        FrequencyKey key = new FrequencyKey(owner, frequency);
        WirelessNetwork network = NetworkManager.getInstance().getNetwork(key);
        if (network != null) {
            NetworkNode node = new NetworkNode(level, worldPosition, key);
            network.removeNode(node);
            BonePipe.LOGGER.debug("Adapter at {} unregistered from network {}", worldPosition, key);
        }
        
        registeredInNetwork = false;
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
        tag.put("upgradeInventory", upgradeInventory.serializeNBT());
        tag.putLong("totalTransferred", totalTransferred);
        // TODO: Save side configs
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
        if (tag.contains("upgradeInventory")) {
            upgradeInventory.deserializeNBT(tag.getCompound("upgradeInventory"));
            recalculateUpgrades();
        }
        if (tag.contains("totalTransferred")) {
            totalTransferred = tag.getLong("totalTransferred");
        }
        // TODO: Load side configs
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
            unregisterFromNetwork();
            this.frequency = frequency;
            registeredInNetwork = false;
            setChanged();
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
    
    /**
     * Get upgrade inventory handler
     */
    public IItemHandler getUpgradeInventory() {
        return upgradeInventory;
    }
    
    /**
     * Get speed multiplier from upgrades
     */
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }
    
    /**
     * Get range multiplier from upgrades
     */
    public double getRangeMultiplier() {
        return rangeMultiplier;
    }
    
    /**
     * Get stack size bonus from upgrades
     */
    public int getStackBonus() {
        return stackBonus;
    }
    
    /**
     * Check if filter upgrade is installed
     */
    public boolean hasFilterUpgrade() {
        return hasFilter;
    }

    // Inner classes
    public enum AccessMode {
        PUBLIC,
        PRIVATE,
        TRUSTED
    }

    public static class SideConfig {
        private boolean enabled = false;
        private TransferMode mode = TransferMode.DISABLED;
        // TODO: Add filter, slot/tank selection, etc.

        public enum TransferMode {
            DISABLED,
            INPUT,
            OUTPUT,
            BOTH
        }
    }
}
