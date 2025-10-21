package com.bonepipe.transfer;

/**
 * Enum of all supported transfer channels
 * Each channel handles a specific type of resource
 */
public enum TransferChannel {
    ITEMS("items", "Item Transfer"),
    FLUIDS("fluids", "Fluid Transfer"),
    ENERGY("energy", "Energy Transfer"),
    MEK_GAS("mek_gas", "Gas Transfer"),
    MEK_INFUSE("mek_infuse", "Infusion Transfer"),
    MEK_PIGMENT("mek_pigment", "Pigment Transfer"),
    MEK_SLURRY("mek_slurry", "Slurry Transfer"),
    REDSTONE("redstone", "Redstone Control");
    
    private final String id;
    private final String displayName;
    
    TransferChannel(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }
    
    public String getId() {
        return id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Check if this channel requires Mekanism
     */
    public boolean requiresMekanism() {
        return this == MEK_GAS || this == MEK_INFUSE || 
               this == MEK_PIGMENT || this == MEK_SLURRY;
    }
    
    /**
     * Get channel by ID
     */
    public static TransferChannel fromId(String id) {
        for (TransferChannel channel : values()) {
            if (channel.id.equals(id)) {
                return channel;
            }
        }
        return null;
    }
}
