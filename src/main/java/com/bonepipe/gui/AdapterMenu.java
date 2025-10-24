package com.bonepipe.gui;

import com.bonepipe.blocks.AdapterBlockEntity;
import com.bonepipe.core.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Container menu for Wireless Adapter GUI
 * Handles server-side inventory logic and syncing
 */
public class AdapterMenu extends AbstractContainerMenu {
    
    private final AdapterBlockEntity blockEntity;
    private final ContainerLevelAccess levelAccess;
    
    /**
     * Constructor for server side
     */
    public AdapterMenu(int containerId, Inventory playerInventory, AdapterBlockEntity blockEntity) {
        super(Registration.ADAPTER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.levelAccess = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        
        // No inventory slots - just configuration GUI
    }
    
    /**
     * Constructor for client side (receives data from server)
     */
    public AdapterMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }
    
    /**
     * Extract BlockEntity from network data
     */
    private static AdapterBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf data) {
        BlockEntity be = playerInventory.player.level.getBlockEntity(data.readBlockPos());
        if (be instanceof AdapterBlockEntity adapter) {
            return adapter;
        }
        throw new IllegalStateException("Invalid BlockEntity at position");
    }
    
    /**
     * Check if menu is still valid
     */
    @Override
    public boolean stillValid(Player player) {
        return stillValid(levelAccess, player, blockEntity.getBlockState().getBlock());
    }
    
    /**
     * Handle quick move (shift-click)
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // No slots - nothing to move
        return ItemStack.EMPTY;
    }
    
    /**
     * Get the BlockEntity
     */
    public AdapterBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
