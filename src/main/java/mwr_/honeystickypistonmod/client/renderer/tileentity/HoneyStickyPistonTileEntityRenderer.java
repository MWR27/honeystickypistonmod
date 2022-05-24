package mwr_.honeystickypistonmod.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.Random;
import net.minecraft.block.BlockState;
import mwr_.honeystickypistonmod.block.ModBlocks;
import mwr_.honeystickypistonmod.block.HoneyStickyPistonBlock;
import mwr_.honeystickypistonmod.block.HoneyStickyPistonHeadBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.state.properties.PistonType;
import mwr_.honeystickypistonmod.tileentity.HoneyStickyPistonTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HoneyStickyPistonTileEntityRenderer extends TileEntityRenderer<HoneyStickyPistonTileEntity> {
   private BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

   public HoneyStickyPistonTileEntityRenderer(TileEntityRendererDispatcher p_i226012_1_) {
      super(p_i226012_1_);
   }

   public void render(HoneyStickyPistonTileEntity p_225616_1_, float p_225616_2_, MatrixStack p_225616_3_, IRenderTypeBuffer p_225616_4_, int p_225616_5_, int p_225616_6_) {
      World world = p_225616_1_.getLevel();
      if (world != null) {
         BlockPos blockpos = p_225616_1_.getBlockPos().relative(p_225616_1_.getMovementDirection().getOpposite());
         BlockState blockstate = p_225616_1_.getMovedState();
         if (!blockstate.isAir()) {
            BlockModelRenderer.enableCaching();
            p_225616_3_.pushPose();
            p_225616_3_.translate((double)p_225616_1_.getXOff(p_225616_2_), (double)p_225616_1_.getYOff(p_225616_2_), (double)p_225616_1_.getZOff(p_225616_2_));
            if (blockstate.is(ModBlocks.HONEY_STICKY_PISTON_HEAD.get()) && p_225616_1_.getProgress(p_225616_2_) <= 4.0F) {
               blockstate = blockstate.setValue(HoneyStickyPistonHeadBlock.SHORT, Boolean.valueOf(p_225616_1_.getProgress(p_225616_2_) <= 0.5F));
               this.renderBlock(blockpos, blockstate, p_225616_3_, p_225616_4_, world, false, p_225616_6_);
            } else if (p_225616_1_.isSourcePiston() && !p_225616_1_.isExtending()) {
               PistonType pistontype = PistonType.STICKY;
               BlockState blockstate1 = ModBlocks.HONEY_STICKY_PISTON_HEAD.get().defaultBlockState().setValue(HoneyStickyPistonHeadBlock.TYPE, pistontype).setValue(HoneyStickyPistonHeadBlock.FACING, blockstate.getValue(HoneyStickyPistonBlock.FACING));
               blockstate1 = blockstate1.setValue(HoneyStickyPistonHeadBlock.SHORT, Boolean.valueOf(p_225616_1_.getProgress(p_225616_2_) >= 0.5F));
               this.renderBlock(blockpos, blockstate1, p_225616_3_, p_225616_4_, world, false, p_225616_6_);
               BlockPos blockpos1 = blockpos.relative(p_225616_1_.getMovementDirection());
               p_225616_3_.popPose();
               p_225616_3_.pushPose();
               blockstate = blockstate.setValue(HoneyStickyPistonBlock.EXTENDED, Boolean.valueOf(true));
               this.renderBlock(blockpos1, blockstate, p_225616_3_, p_225616_4_, world, true, p_225616_6_);
            } else {
               this.renderBlock(blockpos, blockstate, p_225616_3_, p_225616_4_, world, false, p_225616_6_);
            }

            p_225616_3_.popPose();
            BlockModelRenderer.clearCache();
         }
      }
   }

   private void renderBlock(BlockPos p_228876_1_, BlockState p_228876_2_, MatrixStack p_228876_3_, IRenderTypeBuffer p_228876_4_, World p_228876_5_, boolean p_228876_6_, int p_228876_7_) {
      net.minecraftforge.client.ForgeHooksClient.renderPistonMovedBlocks(p_228876_1_, p_228876_2_, p_228876_3_, p_228876_4_, p_228876_5_, p_228876_6_, p_228876_7_, blockRenderer == null ? blockRenderer = Minecraft.getInstance().getBlockRenderer() : blockRenderer);
      if(false) {
      RenderType rendertype = RenderTypeLookup.getMovingBlockRenderType(p_228876_2_);
      IVertexBuilder ivertexbuilder = p_228876_4_.getBuffer(rendertype);
      this.blockRenderer.getModelRenderer().tesselateBlock(p_228876_5_, this.blockRenderer.getBlockModel(p_228876_2_), p_228876_2_, p_228876_1_, p_228876_3_, ivertexbuilder, p_228876_6_, new Random(), p_228876_2_.getSeed(p_228876_1_), p_228876_7_);
      }
   }
}
