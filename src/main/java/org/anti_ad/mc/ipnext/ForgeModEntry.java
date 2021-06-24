package org.anti_ad.mc.ipnext;

import kotlin.Unit;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.anti_ad.mc.common.forge.CommonForgeEventHandler;
import org.anti_ad.mc.ipnext.event.ClientInitHandler;
import org.anti_ad.mc.ipnext.forge.ForgeEventHandler;
import org.anti_ad.mc.ipnext.gui.ConfigScreen;

/**
 * InventoryProfilesNext
 */
@Mod(ModInfo.MOD_ID)
public class ForgeModEntry {

    public ForgeModEntry() {

        MinecraftForge.EVENT_BUS.register(new CommonForgeEventHandler());

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandler());

        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> {
            return (x, y) -> new ConfigScreen();
        });

//    GlobalInputHandler.getInstance().registerInputHandler(new InputHandler());
//
//    Configs.saveLoadManager.load();

        InventoryProfilesKt.init();

        ClientInitHandler.INSTANCE.register(() -> {
            ModInfo.MOD_VERSION = ModInfo.getModVersion();
            return Unit.INSTANCE;
        });

//    GlobalInitHandler.INSTANCE.onInit();
        // ^^ let do it on first tick event
    }

}
