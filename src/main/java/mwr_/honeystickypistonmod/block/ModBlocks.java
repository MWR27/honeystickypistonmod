package mwr_.honeystickypistonmod.block;

import mwr_.honeystickypistonmod.HoneyStickyPistonMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
   public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HoneyStickyPistonMod.MOD_ID);

   public static final RegistryObject<Block> HONEY_STICKY_PISTON = BLOCKS.register("honey_sticky_piston", () -> honeyStickyPistonBase(true));
   public static final RegistryObject<Block> HONEY_STICKY_PISTON_HEAD = BLOCKS.register("honey_sticky_piston_head",
           () -> new HoneyStickyPistonHeadBlock(BlockBehaviour.Properties.of().strength(1.5F).noLootTable())
   );
   public static final RegistryObject<Block> MOVING_HONEY_STICKY_PISTON = BLOCKS.register("moving_honey_sticky_piston",
           () -> new MovingHoneyStickyPistonBlock(BlockBehaviour.Properties.of()
                   .strength(-1.0F)
                   .dynamicShape()
                   .noOcclusion()
                   .isRedstoneConductor((state, level, pos) -> false)
                   .isSuffocating((state, level, pos) -> false)
                   .isViewBlocking((state, level, pos) -> false)
           )
   );

   private static HoneyStickyPistonBaseBlock honeyStickyPistonBase(boolean isSticky) {
      BlockBehaviour.StatePredicate statePredicate = (state, level, pos) -> !state.getValue(HoneyStickyPistonBaseBlock.EXTENDED);
      return new HoneyStickyPistonBaseBlock(isSticky, BlockBehaviour.Properties.of()
              .strength(1.5F)
              .isRedstoneConductor((state, level, pos) -> false)
              .isSuffocating(statePredicate)
              .isViewBlocking(statePredicate)
      );
   }
}
