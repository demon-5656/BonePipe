package com.bonepipe.network;

import java.util.Objects;
import java.util.UUID;

/**
 * Unique key for identifying a wireless network
 * Combination of: owner UUID + frequency name + color (optional)
 */
public class FrequencyKey {
    
    private final UUID owner;
    private final String frequency;
    private final int color; // RGB color for visual distinction
    
    public FrequencyKey(UUID owner, String frequency) {
        this(owner, frequency, 0xFFFFFF); // Default white
    }
    
    public FrequencyKey(UUID owner, String frequency, int color) {
        this.owner = Objects.requireNonNull(owner, "Owner cannot be null");
        this.frequency = Objects.requireNonNull(frequency, "Frequency cannot be null");
        this.color = color;
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    public String getFrequency() {
        return frequency;
    }
    
    public int getColor() {
        return color;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FrequencyKey)) return false;
        FrequencyKey that = (FrequencyKey) o;
        return color == that.color && 
               owner.equals(that.owner) && 
               frequency.equals(that.frequency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(owner, frequency, color);
    }
    
    @Override
    public String toString() {
        return String.format("FrequencyKey[%s@%s#%06X]", 
            frequency, owner.toString().substring(0, 8), color);
    }
}
