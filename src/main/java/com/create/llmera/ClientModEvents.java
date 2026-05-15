package com.create.llmera;

import com.create.llmera.screen.ConfigScreen;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.minecraft.world.item.Item;

@EventBusSubscriber(modid = LLMEraMod.MODID, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.CONFIG_MENU.get(), ConfigScreen::new);
        registerTooltip(ModItems.INTELLIGENT_TRANSMITTER_ITEM.get(), "block.llmera.intelligent_transmitter");
        registerTooltip(ModItems.TOOL_LINK_STATION_ITEM.get(), "block.llmera.tool_link_station");
        registerTooltip(ModItems.SKILL_BOARD_ITEM.get(), "block.llmera.skill_board");
    }

    private static void registerTooltip(Item item, String key) {
        ItemDescription.useKey(item, key);
        TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE));
    }
}
