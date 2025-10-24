package com.bonepipe.transfer;

import com.bonepipe.blocks.AdapterBlockEntity;
import com.bonepipe.network.NetworkNode;
import com.bonepipe.util.MachineDetector;
import mekanism.api.Action;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Transfer handler for Mekanism Gas using Gas Capability
 * Simplified to match EnergyTransferHandler pattern
 */
public class GasTransferHandler implements ITransferHandler {
    
    @Override
    public TransferChannel getChannel() {
        return TransferChannel.MEK_GAS;
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
        
        // Get gas handlers (same pattern as energy/fluids)
        Direction srcSide = getSideToMachine(srcAdapter);
        Direction dstSide = getSideToMachine(dstAdapter);
        IGasHandler srcHandler = getGasHandler(srcMachine, srcSide);
        IGasHandler dstHandler = getGasHandler(dstMachine, dstSide);
        
        if (srcHandler == null || dstHandler == null) {
            return TransferResult.empty();
        }
        
        // Simulate extract from source
        GasStack extractable = srcHandler.extractChemical(maxAmount, Action.SIMULATE);
        if (extractable.isEmpty()) {
            return TransferResult.empty();
        }
        
        // Simulate insert to destination
        GasStack remainder = dstHandler.insertChemical(extractable, Action.SIMULATE);
        long canTransfer = extractable.getAmount() - remainder.getAmount();
        if (canTransfer == 0) {
            return TransferResult.empty();
        }
        
        // Real extract
        GasStack extracted = srcHandler.extractChemical(canTransfer, Action.EXECUTE);
        if (extracted.isEmpty()) {
            return TransferResult.empty();
        }
        
        // Real insert
        GasStack realRemainder = dstHandler.insertChemical(extracted, Action.EXECUTE);
        long transferred = extracted.getAmount() - realRemainder.getAmount();
        
        // Return result
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
        
        IGasHandler handler = getGasHandler(machine, getSideToMachine(adapter));
        if (handler == null) return false;
        
        // Try to simulate extract - if we can extract anything, we can extract
        GasStack extractable = handler.extractChemical(Long.MAX_VALUE, Action.SIMULATE);
        return !extractable.isEmpty();
    }
    
    @Override
    public boolean canInsert(NetworkNode node) {
        AdapterBlockEntity adapter = node.getAdapter();
        if (adapter == null) return false;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return false;
        
        IGasHandler handler = getGasHandler(machine, getSideToMachine(adapter));
        return handler != null && handler.getTanks() > 0;
    }
    
    @Override
    public long getExtractableAmount(NetworkNode source) {
        AdapterBlockEntity adapter = source.getAdapter();
        if (adapter == null) return 0;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return 0;
        
        IGasHandler handler = getGasHandler(machine, getSideToMachine(adapter));
        if (handler == null) return 0;
        
        // Simulate extract to get actual extractable amount
        GasStack extractable = handler.extractChemical(Long.MAX_VALUE, Action.SIMULATE);
        return extractable.isEmpty() ? 0 : extractable.getAmount();
    }
    
    @Override
    public long getInsertableAmount(NetworkNode destination) {
        AdapterBlockEntity adapter = destination.getAdapter();
        if (adapter == null) return 0;
        
        BlockEntity machine = getMachineConnection(adapter);
        if (machine == null) return 0;
        
        IGasHandler handler = getGasHandler(machine, getSideToMachine(adapter));
        if (handler == null) return 0;
        
        // Sum up capacity across all tanks
        long capacity = 0;
        for (int i = 0; i < handler.getTanks(); i++) {
            GasStack gas = handler.getChemicalInTank(i);
            long tankCapacity = handler.getTankCapacity(i);
            capacity += (tankCapacity - gas.getAmount());
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
    }
    
    /**
     * Get gas handler capability from a BlockEntity
     */
    private IGasHandler getGasHandler(BlockEntity entity, Direction side) {
        if (entity == null) return null;
        
        try {
            return entity.getCapability(Capabilities.GAS_HANDLER, side)
                .orElse(null);
        } catch (Exception e) {
            // Silently return null if capability not found
            return null;
        }
    }
}
