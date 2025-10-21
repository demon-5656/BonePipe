package com.bonepipe.gui;

import com.bonepipe.blocks.ControllerBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;

/**
 * Container Menu for Network Controller
 */
public class ControllerMenu extends AbstractContainerMenu {
    
    private final ControllerBlockEntity blockEntity;
    
    public ControllerMenu(int containerId, Inventory playerInventory, ControllerBlockEntity blockEntity) {
        super(com.bonepipe.core.Registration.CONTROLLER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
    }
    
    // Client-side constructor (from packet)
    public ControllerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, buf));
    }
    
    private static ControllerBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        if (playerInventory.player.level.getBlockEntity(pos) instanceof ControllerBlockEntity be) {
            return be;
        }
        throw new IllegalStateException("Invalid ControllerBlockEntity at " + pos);
    }
    
    public ControllerBlockEntity getBlockEntity() {
        return blockEntity;
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && 
               player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5,
                                   blockEntity.getBlockPos().getY() + 0.5,
                                   blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }
}
