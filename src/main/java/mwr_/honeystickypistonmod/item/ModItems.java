package mwr_.honeystickypistonmod;

import mwr_.honeystickypistonmod.block.ModBlocks;
import mwr_.honeystickypistonmod.item.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeTabs {
    @SubscribeEvent
    public static void onCreativeTabBuild(CreativeModeTabEvent.BuildContents event) {
        if (event.getTab() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.getEntries().putAfter(
                new ItemStack(net.minecraft.world.level.block.Blocks.STICKY_PISTON), // Insert after sticky piston
                new ItemStack(ModItems.HONEY_STICKY_PISTON.get()),
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }
    }
}
