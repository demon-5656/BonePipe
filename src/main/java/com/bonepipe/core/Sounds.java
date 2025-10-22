package com.bonepipe.core;

import com.bonepipe.BonePipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Sound event registration for BonePipe
 */
public class Sounds {
    
    public static final DeferredRegister<SoundEvent> SOUNDS = 
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BonePipe.MODID);
    
    // Transfer sound (whoosh)
    public static final RegistryObject<SoundEvent> TRANSFER = register("transfer");
    
    // Connect to network sound
    public static final RegistryObject<SoundEvent> CONNECT = register("connect");
    
    // Disconnect from network sound
    public static final RegistryObject<SoundEvent> DISCONNECT = register("disconnect");
    
    private static RegistryObject<SoundEvent> register(String name) {
        ResourceLocation id = new ResourceLocation(BonePipe.MODID, name);
        return SOUNDS.register(name, () -> new SoundEvent(id));
    }
}
