package mwr_.honeystickypistonmod.block;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import mwr_.honeystickypistonmod.tileentity.HoneyStickyPistonMovingBlockEntity;
import mwr_.honeystickypistonmod.tileentity.ModBlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MovingHoneyStickyPistonBlock extends BaseEntityBlock {
   public static final DirectionProperty FACING = HoneyStickyPistonHeadBlock.FACING;
   public static final EnumProperty<PistonType> TYPE = HoneyStickyPistonHeadBlock.TYPE;

   public MovingHoneyStickyPistonBlock(BlockBehaviour.Properties properties) {
      super(properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, PistonType.DEFAULT));
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new HoneyStickyPistonMovingBlockEntity(pos, state);
   }

   public static BlockEntity newMovingBlockEntity(BlockPos pos, BlockState state, BlockState movedState, Direction direction, boolean extending, boolean isSourcePiston) {
      return new HoneyStickyPistonMovingBlockEntity(pos, state, movedState, direction, extending, isSourcePiston);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
      return createTickerHelper(entityType, ModBlockEntityType.HONEY_STICKY_PISTON.get(), HoneyStickyPistonMovingBlockEntity::tick);
   }

   @Override
   public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moving) {
      if (!oldState.is(newState.getBlock())) {
         BlockEntity blockEntity = level.getBlockEntity(pos);
         if (blockEntity instanceof HoneyStickyPistonMovingBlockEntity) {
            ((HoneyStickyPistonMovingBlockEntity) blockEntity).finalTick();
         }
         super.onRemove(oldState, level, pos, newState, moving);
      }
   }

   @Override
   public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
      BlockPos pistonBasePos = pos.relative(state.getValue(FACING).getOpposite());
      BlockState pistonBaseState = level.getBlockState(pistonBasePos);
      if (pistonBaseState.getBlock() instanceof HoneyStickyPistonBaseBlock && pistonBaseState.getValue(HoneyStickyPistonBaseBlock.EXTENDED)) {
         level.removeBlock(pistonBasePos, false);
      }
   }

   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
      if (!level.isClientSide) {
         BlockEntity blockEntity = level.getBlockEntity(pos);
         if (blockEntity instanceof HoneyStickyPistonMovingBlockEntity) {
            return InteractionResult.PASS;
         } else {
            level.removeBlock(pos, false);
            return InteractionResult.CONSUME;
         }
      }
      return InteractionResult.PASS;
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
      BlockEntity blockEntity = builder.getLevel().getBlockEntity(new BlockPos(builder.getParameter(LootContextParams.ORIGIN)));
      if (blockEntity instanceof HoneyStickyPistonMovingBlockEntity pistonEntity) {
         return pistonEntity.getMovedState().getDrops(builder);
      }
      return Collections.emptyList();
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return Shapes.empty();
   }

   @Override
   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      HoneyStickyPistonMovingBlockEntity pistonEntity = getBlockEntity(level, pos);
      return pistonEntity != null ? pistonEntity.getCollisionShape(level, pos) : Shapes.empty();
   }

   @Nullable
   private HoneyStickyPistonMovingBlockEntity getBlockEntity(BlockGetter level, BlockPos pos) {
      BlockEntity blockEntity = level.getBlockEntity(pos);
      return blockEntity instanceof HoneyStickyPistonMovingBlockEntity ? (HoneyStickyPistonMovingBlockEntity) blockEntity : null;
   }

   @Override
   public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
      return ItemStack.EMPTY;
   }

   @Override
   public BlockState rotate(BlockState state, Rotation rotation) {
      return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
   }

   @Override
   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation(state.getValue(FACING)));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(FACING, TYPE);
   }

   @Override
   public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
      return false;
   }
}
