package com.bonepipe.blocks;

import com.bonepipe.BonePipe;
import com.bonepipe.network.FrequencyKey;
import com.bonepipe.network.NetworkManager;
import com.bonepipe.network.NetworkNode;
import com.bonepipe.network.WirelessNetwork;
import com.bonepipe.util.MachineDetector;
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
    private BlockEntity connectedMachine = null;
    private Direction machineDirection = null;
    private int connectionCheckCooldown = 0;
    
    // Network registration
    private boolean registeredInNetwork = false;
    
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
        
        // Log connection changes
        if (connectedMachine != oldMachine) {
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
