package com.bonepipe.transfer;

import com.bonepipe.network.NetworkNode;

/**
 * Interface for transfer handlers
 * Each channel type implements this interface
 */
public interface ITransferHandler {
    
    /**
     * Get the channel this handler supports
     */
    TransferChannel getChannel();
    
    /**
     * Perform a transfer operation between two nodes
     * 
     * @param source Source node (INPUT)
     * @param destination Destination node (OUTPUT)
     * @param maxAmount Maximum amount to transfer
     * @return Result of the transfer
     */
    TransferResult transfer(NetworkNode source, NetworkNode destination, long maxAmount);
    
    /**
     * Check if a node can act as a source (has extractable resources)
     */
    boolean canExtract(NetworkNode node);
    
    /**
     * Check if a node can act as a destination (can receive resources)
     */
    boolean canInsert(NetworkNode node);
    
    /**
     * Get the maximum amount that can be extracted from a source
     */
    long getExtractableAmount(NetworkNode source);
    
    /**
     * Get the maximum amount that can be inserted into a destination
     */
    long getInsertableAmount(NetworkNode destination);
}
