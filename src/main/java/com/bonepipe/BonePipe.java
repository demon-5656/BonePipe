package com.bonepipe;

import com.bonepipe.core.Registration;
import com.bonepipe.network.packets.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BonePipe.MOD_ID)
public class BonePipe {
    public static final String MOD_ID = "bonepipe";
    public static final Logger LOGGER = LogManager.getLogger();

    public BonePipe() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Регистрация всех элементов мода
        Registration.init(modEventBus);
        
        // События жизненного цикла
        modEventBus.addListener(this::commonSetup);
        
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
}
