package com.bonepipe.transfer;

import com.bonepipe.BonePipe;
import com.bonepipe.blocks.AdapterBlockEntity;
import com.bonepipe.network.NetworkNode;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

/**
 * Transfer handler for items using Forge's IItemHandler capability
 */
public class ItemTransferHandler implements ITransferHandler {
    
    @Override
    public TransferChannel getChannel() {
        return TransferChannel.ITEMS;
    }
    
    @Override
    public TransferResult transfer(NetworkNode source, NetworkNode destination, long maxAmount) {
        AdapterBlockEntity srcAdapter = source.getAdapter();
        AdapterBlockEntity dstAdapter = destination.getAdapter();
        
        if (srcAdapter == null || dstAdapter == null) {
            return TransferResult.failure("Invalid adapters");
        }
        
        // Get machine connections
        BlockEntity srcMachine = getMachineConnection(srcAdapter);
        BlockEntity dstMachine = getMachineConnection(dstAdapter);
        
        if (srcMachine == null || dstMachine == null) {
            return TransferResult.empty();
        }
        
        // Get item handlers
        IItemHandler srcHandler = getItemHandler(srcMachine, getSideToMachine(srcAdapter));
        IItemHandler dstHandler = getItemHandler(dstMachine, getSideToMachine(dstAdapter));
        
        if (srcHandler == null || dstHandler == null) {
            return TransferResult.empty();
        }
        
        // Perform transfer
        int transferred = 0;
        int remaining = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        
        // Try to extract and insert items
        for (int srcSlot = 0; srcSlot < srcHandler.getSlots() && remaining > 0; srcSlot++) {
            // Simulate extract
            ItemStack extractSimulated = srcHandler.extractItem(srcSlot, remaining, true);
            if (extractSimulated.isEmpty()) {
                continue;
            }
            
            // Try to insert into destination
            ItemStack insertResult = insertToHandler(dstHandler, extractSimulated, true);
            int canTransfer = extractSimulated.getCount() - insertResult.getCount();
            
            if (canTransfer > 0) {
                // Real extract
                ItemStack extracted = srcHandler.extractItem(srcSlot, canTransfer, false);
                
                // Real insert
                ItemStack leftover = insertToHandler(dstHandler, extracted, false);
                
                int actualTransfer = extracted.getCount() - leftover.getCount();
                transferred += actualTransfer;
                remaining -= actualTransfer;
                
                // Put back leftovers if any (shouldn't happen in normal cases)
                if (!leftover.isEmpty()) {
                    BonePipe.LOGGER.warn("Leftover items after transfer: {}", leftover);
                }
            }
        }
        
        return transferred > 0 ? 
            TransferResult.success(transferred) : 
            TransferResult.empty();
    }
    
    /**
     * Try to insert an ItemStack into any slot of the handler
     */
    private ItemStack insertToHandler(IItemHandler handler, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack.copy();
        
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, simulate);
        }
        
        return remaining;
    }
    
    @Override
    public boolean canExtract(NetworkNode node) {
        AdapterBlockEntity adapter = node.getAdapter();
        if (adapter == null) return false;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return false;
        
        IItemHandler handler = getItemHandler(machine, getSideToMachine(adapter));
        if (handler == null) return false;
        
        // Check if any slot has extractable items
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.extractItem(i, 1, true).isEmpty()) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean canInsert(NetworkNode node) {
        AdapterBlockEntity adapter = node.getAdapter();
        if (adapter == null) return false;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return false;
        
        IItemHandler handler = getItemHandler(machine, getSideToMachine(adapter));
        return handler != null && handler.getSlots() > 0;
    }
    
    @Override
    public long getExtractableAmount(NetworkNode source) {
        AdapterBlockEntity adapter = source.getAdapter();
        if (adapter == null) return 0;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return 0;
        
        IItemHandler handler = getItemHandler(machine, getSideToMachine(adapter));
        if (handler == null) return 0;
        
        long total = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            total += stack.getCount();
        }
        
        return total;
    }
    
    @Override
    public long getInsertableAmount(NetworkNode destination) {
        AdapterBlockEntity adapter = destination.getAdapter();
        if (adapter == null) return 0;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return 0;
        
        IItemHandler handler = getItemHandler(machine, getSideToMachine(adapter));
        if (handler == null) return 0;
        
        // Rough estimate: count empty slots
        long capacity = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.isEmpty()) {
                capacity += 64; // Assume max stack size
            } else {
                capacity += (stack.getMaxStackSize() - stack.getCount());
            }
        }
        
        return capacity;
    }
    
    /**
     * Get the machine BlockEntity connected to this adapter
     */
    private BlockEntity getMachineConnection(AdapterBlockEntity adapter) {
        // TODO: Implement proper machine detection
        // For now, check all adjacent blocks
        for (Direction dir : Direction.values()) {
            BlockEntity be = adapter.getLevel().getBlockEntity(adapter.getBlockPos().relative(dir));
            if (be != null && be != adapter) {
                return be;
            }
        }
        return null;
    }
    
    /**
     * Get the direction to the connected machine
     */
    private Direction getSideToMachine(AdapterBlockEntity adapter) {
        // TODO: Implement proper side detection
        // For now, return UP as default
        return Direction.UP;
    }
    
    /**
     * Get item handler capability from a BlockEntity
     */
    private IItemHandler getItemHandler(BlockEntity be, Direction side) {
        return be.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElse(null);
    }
}
