package com.bonepipe.blocks;

import com.bonepipe.BonePipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
    private boolean connectedToMachine = false;
    private Direction machineDirection = null;
    
    // Tick counter
    private int tickCounter = 0;

    public AdapterBlockEntity(BlockPos pos, BlockState state) {
        super(null, pos, state); // TODO: Set proper BlockEntityType
        
        // Initialize side configs
        for (Direction dir : Direction.values()) {
            sideConfigs.put(dir, new SideConfig());
        }
    }

    /**
     * Main tick logic - called every game tick (20 TPS)
     */
    public void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }

        tickCounter++;

        // Every 20 ticks (once per second)
        if (tickCounter % 20 == 0) {
            checkMachineConnection();
        }

        // Network operations
        if (connectedToMachine && !frequency.isEmpty()) {
            performNetworkOperations();
        }
    }

    /**
     * Check if we're still connected to a machine
     */
    private void checkMachineConnection() {
        // TODO: Implement capability checking for adjacent machines
        // For now, just log
        if (tickCounter % 100 == 0) {
            BonePipe.LOGGER.debug("Adapter at {} checking connection", worldPosition);
        }
    }

    /**
     * Perform network transfer operations
     */
    private void performNetworkOperations() {
        // TODO: Implement network logic
        // 1. Register with NetworkManager
        // 2. Process transfer requests
        // 3. Execute actual transfers
    }

    /**
     * Called when block is removed
     */
    public void onRemoved() {
        // TODO: Unregister from network
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
        // TODO: Return AdapterMenu
        return null;
    }

    // Getters/Setters
    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
        setChanged();
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public AccessMode getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(AccessMode mode) {
        this.accessMode = mode;
        setChanged();
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
