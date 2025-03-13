package mwr_.honeystickypistonmod.tileentity;

import mwr_.honeystickypistonmod.block.HoneyStickyPistonBaseBlock;
import mwr_.honeystickypistonmod.block.HoneyStickyPistonHeadBlock;
import mwr_.honeystickypistonmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMath;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HoneyStickyPistonMovingBlockEntity extends BlockEntity {
   private BlockState movedState = Blocks.AIR.defaultBlockState();
   private Direction direction;
   private boolean extending;
   private boolean isSourcePiston;
   private float progress;
   private float progressO;
   private long lastTicked;
   private int deathTicks;
   private static final ThreadLocal<Direction> NOCLIP = ThreadLocal.withInitial(() -> null);

   public HoneyStickyPistonMovingBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlockEntityType.HONEY_STICKY_PISTON.get(), pos, state);
   }

   public HoneyStickyPistonMovingBlockEntity(BlockPos pos, BlockState state, BlockState movedState, Direction direction, boolean extending, boolean isSourcePiston) {
      this(pos, state);
      this.movedState = movedState;
      this.direction = direction;
      this.extending = extending;
      this.isSourcePiston = isSourcePiston;
   }

   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   public boolean isExtending() {
      return this.extending;
   }

   public Direction getDirection() {
      return this.direction;
   }

   public boolean isSourcePiston() {
      return this.isSourcePiston;
   }

   public float getProgress(float delta) {
      return Mth.lerp(delta, this.progressO, this.progress);
   }

   public float getXOff(float delta) {
      return (float) this.direction.getStepX() * this.getExtendedProgress(this.getProgress(delta));
   }

   public float getYOff(float delta) {
      return (float) this.direction.getStepY() * this.getExtendedProgress(this.getProgress(delta));
   }

   public float getZOff(float delta) {
      return (float) this.direction.getStepZ() * this.getExtendedProgress(this.getProgress(delta));
   }

   private float getExtendedProgress(float progress) {
      return this.extending ? progress - 1.0F : 1.0F - progress;
   }

   private BlockState getCollisionRelatedBlockState() {
      if (!this.isExtending() && this.isSourcePiston() && this.movedState.getBlock() instanceof HoneyStickyPistonBaseBlock) {
         return ModBlocks.HONEY_STICKY_PISTON_HEAD.get().defaultBlockState()
                 .setValue(HoneyStickyPistonHeadBlock.SHORT, this.progress > 0.25F)
                 .setValue(HoneyStickyPistonHeadBlock.TYPE, this.movedState.is(ModBlocks.HONEY_STICKY_PISTON.get()) ? PistonType.STICKY : PistonType.DEFAULT)
                 .setValue(HoneyStickyPistonHeadBlock.FACING, this.movedState.getValue(HoneyStickyPistonBaseBlock.FACING));
      }
      return this.movedState;
   }

   public void finalTick() {
      if (this.level != null && (this.progressO < 1.0F || this.level.isClientSide)) {
         this.progress = 1.0F;
         this.progressO = this.progress;
         this.level.removeBlockEntity(this.worldPosition);
         this.setRemoved();
         if (this.level.getBlockState(this.worldPosition).is(ModBlocks.MOVING_HONEY_STICKY_PISTON.get())) {
            BlockState blockstate = this.isSourcePiston ? Blocks.AIR.defaultBlockState() :
                    Block.updateFromNeighbourShapes(this.movedState, this.level, this.worldPosition);
            this.level.setBlock(this.worldPosition, blockstate, 3);
            this.level.neighborChanged(this.worldPosition, blockstate.getBlock(), this.worldPosition);
         }
      }
   }

   public static void tick(Level level, BlockPos pos, BlockState state, HoneyStickyPistonMovingBlockEntity blockEntity) {
      blockEntity.lastTicked = level.getGameTime();
      blockEntity.progressO = blockEntity.progress;

      if (blockEntity.progressO >= 1.0F) {
         if (level.isClientSide && blockEntity.deathTicks < 5) {
            blockEntity.deathTicks++;
         } else {
            level.removeBlockEntity(pos);
            blockEntity.setRemoved();
            if (level.getBlockState(pos).is(ModBlocks.MOVING_HONEY_STICKY_PISTON.get())) {
               BlockState newState = Block.updateFromNeighbourShapes(blockEntity.movedState, level, pos);
               if (newState.isAir()) {
                  level.setBlock(pos, blockEntity.movedState, 84);
                  Block.updateOrDestroy(blockEntity.movedState, newState, level, pos, 3);
               } else {
                  if (newState.hasProperty(BlockStateProperties.WATERLOGGED) && newState.getValue(BlockStateProperties.WATERLOGGED)) {
                     newState = newState.setValue(BlockStateProperties.WATERLOGGED, false);
                  }
                  level.setBlock(pos, newState, 67);
                  level.neighborChanged(pos, newState.getBlock(), pos);
               }
            }
         }
      } else {
         blockEntity.progress += 0.5F;
         if (blockEntity.progress >= 1.0F) {
            blockEntity.progress = 1.0F;
         }
      }
   }

   @Override
   public void load(CompoundTag tag) {
      super.load(tag);

      if (this.level != null) {
         this.movedState = NbtUtils.readBlockState(this.level.holderLookup(Registries.BLOCK), tag.getCompound("blockState"));
      } else {
         this.movedState = Blocks.AIR.defaultBlockState(); // Fallback to avoid null errors
      }

      this.direction = Direction.from3DDataValue(tag.getInt("facing"));
      this.progress = tag.getFloat("progress");
      this.progressO = this.progress;
      this.extending = tag.getBoolean("extending");
      this.isSourcePiston = tag.getBoolean("source");
   }


   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.put("blockState", NbtUtils.writeBlockState(this.movedState));
      tag.putInt("facing", this.direction.get3DDataValue());
      tag.putFloat("progress", this.progressO);
      tag.putBoolean("extending", this.extending);
      tag.putBoolean("source", this.isSourcePiston);
   }

   public VoxelShape getCollisionShape(BlockGetter level, BlockPos pos) {
      return Shapes.empty();
   }

   public long getLastTicked() {
      return this.lastTicked;
   }
}
