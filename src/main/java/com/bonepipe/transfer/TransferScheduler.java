package com.bonepipe.transfer;

import com.bonepipe.BonePipe;
import com.bonepipe.network.WirelessNetwork;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Scheduler for managing transfer operations across a network
 * Implements round-robin and other balancing algorithms
 */
public class TransferScheduler {
    
    // Registry of transfer handlers
    private final Map<TransferChannel, ITransferHandler> handlers = new HashMap<>();
    
    // Performance limits (loaded from config)
    private int maxTransfersPerTick;
    private int maxPairsPerChannel;
    
    public TransferScheduler() {
        // Load config values
        updateConfigValues();
        registerDefaultHandlers();
    }
    
    /**
     * Update performance limits from config
     */
    private void updateConfigValues() {
        maxTransfersPerTick = com.bonepipe.core.Config.SERVER.maxTransfersPerTick.get();
        maxPairsPerChannel = com.bonepipe.core.Config.SERVER.maxPairsPerChannel.get();
    }
    
    /**
     * Register all default transfer handlers
     */
    private void registerDefaultHandlers() {
        // Register Forge handlers
        registerHandler(new ItemTransferHandler());
        registerHandler(new FluidTransferHandler());
        registerHandler(new EnergyTransferHandler());
        
        // Register Mekanism Gas handler (if Mekanism is loaded at runtime)
        if (isMekanismLoaded()) {
            try {
                registerHandler(new GasTransferHandler());
                BonePipe.LOGGER.info("Mekanism Gas handler registered successfully");
            } catch (Exception e) {
                BonePipe.LOGGER.error("Failed to register Mekanism Gas handler: {}", e.getMessage());
            }
        }
        
        BonePipe.LOGGER.info("TransferScheduler initialized with {} handlers", handlers.size());
    }
    
    /**
     * Check if Mekanism mod is loaded at runtime
     */
    private boolean isMekanismLoaded() {
        try {
            Class.forName("mekanism.api.chemical.gas.IGasHandler");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Register a transfer handler
     */
    public void registerHandler(ITransferHandler handler) {
        handlers.put(handler.getChannel(), handler);
        BonePipe.LOGGER.debug("Registered handler for channel: {}", handler.getChannel());
    }
    
    /**
     * Main scheduling method - called every tick
     * 
     * @param network The network to process
     * @param level The level/dimension
     * @return Number of successful transfers
     */
    public long schedule(WirelessNetwork network, Level level) {
        long totalTransfers = 0;
        
        // Process each registered channel
        for (Map.Entry<TransferChannel, ITransferHandler> entry : handlers.entrySet()) {
            TransferChannel channel = entry.getKey();
            ITransferHandler handler = entry.getValue();
            
            try {
                long channelTransfers = processChannel(network, handler);
                totalTransfers += channelTransfers;
                
                if (channelTransfers > 0) {
                    BonePipe.LOGGER.trace("Channel {} transferred {} units", 
                        channel, channelTransfers);
                }
            } catch (Exception e) {
                BonePipe.LOGGER.error("Error processing channel {}: {}", 
                    channel, e.getMessage(), e);
            }
        }
        
        return totalTransfers;
    }
    
    /**
     * Process a single channel's transfers
     */
    private long processChannel(WirelessNetwork network, ITransferHandler handler) {
        long totalTransferred = 0;
        int operationsCount = 0;
        
        var validNodes = network.getValidNodes();
        
        if (validNodes.isEmpty()) {
            return 0;
        }
        
        BonePipe.LOGGER.debug("📊 Processing channel {} with {} nodes", 
            handler.getChannel(), validNodes.size());
        
        // 1. Separate INPUT and OUTPUT nodes
        var inputNodes = new java.util.ArrayList<com.bonepipe.network.NetworkNode>();
        var outputNodes = new java.util.ArrayList<com.bonepipe.network.NetworkNode>();
        
        for (var node : validNodes) {
            var adapter = node.getAdapter();
            if (adapter == null || !adapter.isEnabled()) {
                BonePipe.LOGGER.debug("  Node {} skipped: adapter={}, enabled={}", 
                    node.getPosition(), adapter != null, adapter != null && adapter.isEnabled());
                continue;
            }
            
            // Check channel configuration for this transfer type
            var channelConfig = adapter.getChannelConfig(handler.getChannel());
            if (channelConfig == null || !channelConfig.isEnabled()) {
                BonePipe.LOGGER.debug("  Node {} skipped: channel {} config={}, enabled={}", 
                    node.getPosition(), handler.getChannel().getDisplayName(), 
                    channelConfig != null, channelConfig != null && channelConfig.isEnabled());
                continue;
            }
            
            BonePipe.LOGGER.debug("  Node {} for channel {}: mode={}", 
                node.getPosition(), handler.getChannel().getDisplayName(), channelConfig.mode);
            
            // Add to appropriate list based on transfer mode
            switch (channelConfig.mode) {
                case INPUT -> inputNodes.add(node);
                case OUTPUT -> outputNodes.add(node);
                case BOTH -> {
                    inputNodes.add(node);
                    outputNodes.add(node);
                }
            }
        }
        
        BonePipe.LOGGER.debug("  Result: {} inputs, {} outputs", inputNodes.size(), outputNodes.size());
        
        if (inputNodes.isEmpty() || outputNodes.isEmpty()) {
            return 0;
        }        // 2. Round-robin pairing with limits
        int pairsProcessed = 0;
        int outputIndex = 0;
        
        for (var inputNode : inputNodes) {
            if (pairsProcessed >= maxPairsPerChannel) {
                break;
            }
            
            var inputAdapter = inputNode.getAdapter();
            if (inputAdapter == null) {
                continue;
            }
            
            // 3. Try transfer to outputs (round-robin)
            int attemptsThisInput = 0;
            while (attemptsThisInput < outputNodes.size() && operationsCount < maxTransfersPerTick) {
                var outputNode = outputNodes.get(outputIndex % outputNodes.size());
                outputIndex++;
                attemptsThisInput++;
                
                var outputAdapter = outputNode.getAdapter();
                if (outputAdapter == null || outputAdapter.equals(inputAdapter)) {
                    continue;
                }
                
                // Filters are checked in the handler's transfer logic
                
                // 5. Execute transfer with upgrade bonuses
                try {
                    // Calculate transfer amount with speed bonus (use config values)
                    long baseAmount = 64; // Default for items
                    if (handler.getChannel() == TransferChannel.FLUIDS) {
                        baseAmount = com.bonepipe.core.Config.SERVER.baseFluidTransferRate.get();
                    } else if (handler.getChannel() == TransferChannel.ENERGY) {
                        baseAmount = com.bonepipe.core.Config.SERVER.baseEnergyTransferRate.get();
                    } else if (handler.getChannel() == TransferChannel.ITEMS) {
                        baseAmount = com.bonepipe.core.Config.SERVER.baseItemTransferRate.get();
                    }
                    
                    long transferAmount = (long) (baseAmount * inputAdapter.getSpeedMultiplier());
                    transferAmount += inputAdapter.getStackBonus();
                    
                    // Execute the transfer using NetworkNode-based API
                    var result = handler.transfer(inputNode, outputNode, transferAmount);
                    
                    if (result.isSuccess() && result.getAmountTransferred() > 0) {
                        long transferred = result.getAmountTransferred();
                        totalTransferred += transferred;
                        operationsCount++;
                        pairsProcessed++;
                        
                        // Update activity
                        inputAdapter.recordTransfer(transferred);
                        outputAdapter.recordTransfer(transferred);
                        
                        BonePipe.LOGGER.debug("Transferred {} units via {} from {} to {}", 
                            transferred, handler.getChannel(), 
                            inputNode.getPosition().toShortString(),
                            outputNode.getPosition().toShortString());
                        
                        // Break to next input (successful transfer)
                        break;
                    }
                } catch (Exception e) {
                    BonePipe.LOGGER.warn("Transfer failed between {} and {}: {}", 
                        inputNode.getPosition(), outputNode.getPosition(), e.getMessage());
                }
            }
        }
        
        if (totalTransferred > 0) {
            BonePipe.LOGGER.info("Channel {} completed {} transfers ({} units total)", 
                handler.getChannel(), pairsProcessed, totalTransferred);
        }
        
        return totalTransferred;
    }
    
    /**
     * Get a registered handler
     */
    public ITransferHandler getHandler(TransferChannel channel) {
        return handlers.get(channel);
    }
    
    /**
     * Check if a channel is supported
     */
    public boolean isChannelSupported(TransferChannel channel) {
        return handlers.containsKey(channel);
    }
}
