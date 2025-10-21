package com.bonepipe.client;

import com.bonepipe.core.Registration;
import com.bonepipe.gui.AdapterScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-side initialization
 * Registers screens and other client-only content
 */
@Mod.EventBusSubscriber(modid = com.bonepipe.BonePipe.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register screens
            MenuScreens.register(Registration.ADAPTER_MENU.get(), AdapterScreen::new);
        });
    }
}
