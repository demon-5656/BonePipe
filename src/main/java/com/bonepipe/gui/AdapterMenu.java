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
import net.minecraftforge.items.SlotItemHandler;

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
        
        // Add upgrade slots (4 slots in a row at the top)
        // Position: x=44, y=100 (below side config widget)
        for (int i = 0; i < 4; i++) {
            this.addSlot(new SlotItemHandler(blockEntity.getUpgradeInventory(), i, 44 + i * 22, 100));
        }
        
        // Add player inventory (standard 3x9 grid)
        // Position: x=8, y=140 (moved down to accommodate content area)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 
                    8 + col * 18, 140 + row * 18));
            }
        }
        
        // Add player hotbar (bottom row)
        // Position: x=8, y=198
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }
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
     * Supports moving upgrade cards and items between inventories
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();
            
            int upgradeSlots = 4;
            int playerInvStart = upgradeSlots;
            int playerInvEnd = playerInvStart + 36;
            
            // From upgrade slots to player inventory
            if (index < upgradeSlots) {
                if (!this.moveItemStackTo(slotStack, playerInvStart, playerInvEnd, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // From player inventory to upgrade slots
            else {
                if (!this.moveItemStackTo(slotStack, 0, upgradeSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }
            
            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            
            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            
            slot.onTake(player, slotStack);
        }
        
        return itemstack;
    }
    
    /**
     * Get the BlockEntity
     */
    public AdapterBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
