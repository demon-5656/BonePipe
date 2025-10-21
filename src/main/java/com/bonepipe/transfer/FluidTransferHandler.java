package com.bonepipe.transfer;

import com.bonepipe.BonePipe;
import com.bonepipe.blocks.AdapterBlockEntity;
import com.bonepipe.network.NetworkNode;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * Transfer handler for fluids using Forge's IFluidHandler capability
 */
package com.bonepipe.transfer;

import com.bonepipe.BonePipe;
import com.bonepipe.blocks.AdapterBlockEntity;
import com.bonepipe.network.NetworkNode;
import com.bonepipe.util.MachineDetector;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidTransferHandler implements ITransferHandler {
    
    @Override
    public TransferChannel getChannel() {
        return TransferChannel.FLUIDS;
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
        
        // Get fluid handlers
        IFluidHandler srcHandler = getFluidHandler(srcMachine, getSideToMachine(srcAdapter));
        IFluidHandler dstHandler = getFluidHandler(dstMachine, getSideToMachine(dstAdapter));
        
        if (srcHandler == null || dstHandler == null) {
            return TransferResult.empty();
        }
        
        // Perform transfer
        int transferred = 0;
        int remaining = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        
        // Try to drain from source and fill destination
        for (int srcTank = 0; srcTank < srcHandler.getTanks() && remaining > 0; srcTank++) {
            // Simulate drain
            FluidStack drainSimulated = srcHandler.drain(remaining, IFluidHandler.FluidAction.SIMULATE);
            if (drainSimulated.isEmpty()) {
                continue;
            }
            
            // Simulate fill
            int fillSimulated = dstHandler.fill(drainSimulated, IFluidHandler.FluidAction.SIMULATE);
            if (fillSimulated == 0) {
                continue;
            }
            
            // Real drain
            FluidStack drained = srcHandler.drain(fillSimulated, IFluidHandler.FluidAction.EXECUTE);
            if (drained.isEmpty()) {
                continue;
            }
            
            // Real fill
            int filled = dstHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            
            if (filled != drained.getAmount()) {
                BonePipe.LOGGER.warn("Fluid transfer mismatch: drained {} but filled {}", 
                    drained.getAmount(), filled);
            }
            
            transferred += filled;
            remaining -= filled;
        }
        
        return transferred > 0 ? 
            TransferResult.success(transferred) : 
            TransferResult.empty();
    }
    
    @Override
    public boolean canExtract(NetworkNode node) {
        AdapterBlockEntity adapter = node.getAdapter();
        if (adapter == null) return false;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return false;
        
        IFluidHandler handler = getFluidHandler(machine, getSideToMachine(adapter));
        if (handler == null) return false;
        
        // Check if any tank has drainable fluid
        for (int i = 0; i < handler.getTanks(); i++) {
            FluidStack fluid = handler.getFluidInTank(i);
            if (!fluid.isEmpty()) {
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
        
        IFluidHandler handler = getFluidHandler(machine, getSideToMachine(adapter));
        return handler != null && handler.getTanks() > 0;
    }
    
    @Override
    public long getExtractableAmount(NetworkNode source) {
        AdapterBlockEntity adapter = source.getAdapter();
        if (adapter == null) return 0;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return 0;
        
        IFluidHandler handler = getFluidHandler(machine, getSideToMachine(adapter));
        if (handler == null) return 0;
        
        long total = 0;
        for (int i = 0; i < handler.getTanks(); i++) {
            FluidStack fluid = handler.getFluidInTank(i);
            total += fluid.getAmount();
        }
        
        return total;
    }
    
    @Override
    public long getInsertableAmount(NetworkNode destination) {
        AdapterBlockEntity adapter = destination.getAdapter();
        if (adapter == null) return 0;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return 0;
        
        IFluidHandler handler = getFluidHandler(machine, getSideToMachine(adapter));
        if (handler == null) return 0;
        
        long capacity = 0;
        for (int i = 0; i < handler.getTanks(); i++) {
            FluidStack fluid = handler.getFluidInTank(i);
            int tankCapacity = handler.getTankCapacity(i);
            capacity += (tankCapacity - fluid.getAmount());
        }
        
        return capacity;
    }
    
    /**
     * Get connected machine's BlockEntity
     */
    private BlockEntity getMachineConnection(NetworkNode node) {
        return MachineDetector.findConnectedMachine(node.getLevel(), node.getPos());
    }

    /**
     * Get side to access machine
     */
    private Direction getSideToMachine(NetworkNode node) {
        return MachineDetector.findMachineDirection(node.getLevel(), node.getPos());
    }    /**
     * Get fluid handler capability from a BlockEntity
     */
    private IFluidHandler getFluidHandler(BlockEntity be, Direction side) {
        return be.getCapability(ForgeCapabilities.FLUID_HANDLER, side).orElse(null);
    }
}
