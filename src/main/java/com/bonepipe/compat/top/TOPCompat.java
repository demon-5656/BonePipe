package com.bonepipe.compat.top;

import com.bonepipe.BonePipe;
import mcjty.theoneprobe.api.ITheOneProbe;

import java.util.function.Function;

/**
 * The One Probe compatibility initialization
 */
public class TOPCompat implements Function<ITheOneProbe, Void> {
    
    @Override
    public Void apply(ITheOneProbe theOneProbe) {
        BonePipe.LOGGER.info("Registering The One Probe integration");
        theOneProbe.registerProvider(new TOPProvider());
        return null;
    }
}
