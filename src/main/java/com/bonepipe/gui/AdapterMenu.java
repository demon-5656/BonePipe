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
        
        // NOTE: Upgrade slots removed - upgrades now installed by right-clicking adapter with upgrade card
        
        // Add player inventory (standard 3x9 grid)
        // Using Mekanism-style positioning: BASE_Y_OFFSET = 84
        int yOffset = 84;
        int xOffset = 8;
        
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 
                    xOffset + col * 18, yOffset + row * 18));
            }
        }
        
        // Add player hotbar (bottom row)
        // Position: yOffset + 58 (standard Minecraft inventory spacing)
        yOffset += 58;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, xOffset + col * 18, yOffset));
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
     * Note: No upgrade slots in GUI anymore - upgrades installed directly on adapter
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();
            
            // All slots are player inventory (no custom slots)
            int playerInvSize = 36;
            
            // Move between inventory sections
            if (index < playerInvSize) {
                // Already in player inventory, no other slots to move to
                return ItemStack.EMPTY;
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
