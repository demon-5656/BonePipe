package com.bonepipe;

import com.bonepipe.core.Registration;
import com.bonepipe.network.packets.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BonePipe.MOD_ID)
public class BonePipe {
    public static final String MOD_ID = "bonepipe";
    public static final String MODID = MOD_ID; // Alias for compatibility
    public static final Logger LOGGER = LogManager.getLogger();

    public BonePipe() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Регистрация конфигурации
        com.bonepipe.core.Config.register();
        
        // Регистрация всех элементов мода
        Registration.init(modEventBus);
        com.bonepipe.core.Sounds.SOUNDS.register(modEventBus);
        
        // События жизненного цикла
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::enqueueIMC);
        
        // Регистрация событий Forge
        MinecraftForge.EVENT_BUS.register(this);
        
        LOGGER.info("BonePipe mod initialized!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("BonePipe common setup");
        
        // Инициализация сети, интеграций и т.д.
        event.enqueueWork(() -> {
            NetworkHandler.register();
            LOGGER.info("BonePipe packet handler initialized");
        });
    }
    
    /**
     * Send IMC messages to other mods (TOP integration)
     */
    private void enqueueIMC(final InterModEnqueueEvent event) {
        // The One Probe integration
        if (ModList.get().isLoaded("theoneprobe")) {
            LOGGER.info("Registering The One Probe integration");
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", 
                com.bonepipe.compat.top.TOPCompat::new);
        }
    }
}
