package com.bonepipe.transfer;

import com.bonepipe.blocks.AdapterBlockEntity;
import com.bonepipe.network.NetworkNode;
import com.bonepipe.util.MachineDetector;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * Transfer handler for fluids using Forge's IFluidHandler capability
 * Simplified to match EnergyTransferHandler pattern
 */
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
            com.bonepipe.BonePipe.LOGGER.info("💧 FLUID: Missing machines - src={}, dst={}", 
                srcMachine != null, dstMachine != null);
            return TransferResult.empty();
        }
        
        // Get fluid handlers (same pattern as energy)
        Direction srcSide = getSideToMachine(srcAdapter);
        Direction dstSide = getSideToMachine(dstAdapter);
        
        com.bonepipe.BonePipe.LOGGER.info("💧 FLUID: Machines {} (side {}) → {} (side {})",
            srcMachine.getClass().getSimpleName(), srcSide,
            dstMachine.getClass().getSimpleName(), dstSide);
        
        IFluidHandler srcHandler = getFluidHandler(srcMachine, srcSide);
        IFluidHandler dstHandler = getFluidHandler(dstMachine, dstSide);
        
        if (srcHandler == null || dstHandler == null) {
            String machineType = srcMachine.getClass().getSimpleName();
            if (machineType.contains("Chemical")) {
                com.bonepipe.BonePipe.LOGGER.info("💧 FLUID: ⚠️ {} doesn't support FLUIDS! Use Mekanism GAS channel or Fluid Tanks instead", machineType);
            } else {
                com.bonepipe.BonePipe.LOGGER.info("💧 FLUID: No IFluidHandler capability - src={}, dst={}", 
                    srcHandler != null, dstHandler != null);
            }
            return TransferResult.empty();
        }
        
        com.bonepipe.BonePipe.LOGGER.info("💧 FLUID: Handlers OK - src tanks={}, dst tanks={}", 
            srcHandler.getTanks(), dstHandler.getTanks());
        
        // Calculate transfer amount
        int amount = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        
        // Simulate drain from source
        FluidStack drainable = srcHandler.drain(amount, IFluidHandler.FluidAction.SIMULATE);
        if (drainable.isEmpty()) {
            com.bonepipe.BonePipe.LOGGER.info("💧 FLUID: Cannot drain (empty or locked)");
            return TransferResult.empty();
        }
        
        com.bonepipe.BonePipe.LOGGER.info("💧 FLUID: Can drain {} mb of {}", 
            drainable.getAmount(), 
            net.minecraftforge.registries.ForgeRegistries.FLUIDS.getKey(drainable.getFluid()));
        
        // Simulate fill to destination
        int fillable = dstHandler.fill(drainable, IFluidHandler.FluidAction.SIMULATE);
        if (fillable == 0) {
            com.bonepipe.BonePipe.LOGGER.info("💧 FLUID: Destination full or incompatible");
            return TransferResult.empty();
        }
        
        com.bonepipe.BonePipe.LOGGER.info("💧 FLUID: Can fill {} mb", fillable);
        
        // Real drain
        FluidStack drained = srcHandler.drain(fillable, IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) {
            com.bonepipe.BonePipe.LOGGER.warn("💧 FLUID: Real drain FAILED after successful simulate!");
            return TransferResult.empty();
        }
        
        // Real fill
        int filled = dstHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        
        com.bonepipe.BonePipe.LOGGER.info("💧 FLUID: ✅ Transferred {} mb", filled);
        
        // Return result
        return filled > 0 ? 
            TransferResult.success(filled) : 
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
        
        // Try to simulate drain - if we can drain anything, we can extract
        FluidStack drainable = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        return !drainable.isEmpty();
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
        
        // Simulate drain to get actual drainable amount
        FluidStack drainable = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        return drainable.isEmpty() ? 0 : drainable.getAmount();
    }
    
    @Override
    public long getInsertableAmount(NetworkNode destination) {
        AdapterBlockEntity adapter = destination.getAdapter();
        if (adapter == null) return 0;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return 0;
        
        IFluidHandler handler = getFluidHandler(machine, getSideToMachine(adapter));
        if (handler == null) return 0;
        
        // Sum up capacity across all tanks
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
    private BlockEntity getMachineConnection(AdapterBlockEntity adapter) {
        return MachineDetector.findConnectedMachine(adapter.getLevel(), adapter.getBlockPos());
    }

    /**
     * Get side to access machine
     */
    private Direction getSideToMachine(AdapterBlockEntity adapter) {
        return MachineDetector.findMachineDirection(adapter.getLevel(), adapter.getBlockPos());
    }    /**
     * Get fluid handler capability from a BlockEntity
     */
    private IFluidHandler getFluidHandler(BlockEntity be, Direction side) {
        return be.getCapability(ForgeCapabilities.FLUID_HANDLER, side)
            .orElse(null);
    }
}
