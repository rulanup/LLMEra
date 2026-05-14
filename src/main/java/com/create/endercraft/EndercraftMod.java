package com.create.endercraft;

import com.create.endercraft.network.ModNetworking;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(EndercraftMod.MODID)
public class EndercraftMod {
    public static final String MODID = "endercraft";

    public EndercraftMod() {
        IEventBus modEventBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModNetworking.register(modEventBus);
    }
}
