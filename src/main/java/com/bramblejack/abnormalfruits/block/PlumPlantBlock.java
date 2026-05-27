package com.bramblejack.abnormalfruits.block;

import com.teamabnormals.environmental.core.registry.EnvironmentalBlocks;
import com.teamabnormals.environmental.core.registry.EnvironmentalItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;

public class PlumPlantBlock extends Block implements BonemealableBlock {

    public static final IntegerProperty AGE    = IntegerProperty.create("age", 0, 2);
    public static final IntegerProperty FRUITS = IntegerProperty.create("fruits", 1, 3);

    protected static final VoxelShape SHAPE = Block.box(1.0, 9.0, 2.0, 15.0, 16.0, 14.0);

    public PlumPlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(FRUITS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, FRUITS);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public static boolean isPlumLeaves(BlockState state) {
        return state.is(EnvironmentalBlocks.PLUM_LEAVES.get())
                || state.is(EnvironmentalBlocks.CHEERFUL_PLUM_LEAVES.get())
                || state.is(EnvironmentalBlocks.MOODY_PLUM_LEAVES.get());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isPlumLeaves(level.getBlockState(pos.above()));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
                                  LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.UP && !state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < 2;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age < 2) {
            int skyLight = level.getRawBrightness(pos.above(), 0);
            if (skyLight >= 7 && ForgeHooks.onCropsGrowPre(level, pos, state, random.nextInt(10) == 0)) {
                level.setBlock(pos, state.setValue(AGE, age + 1), 2);
                ForgeHooks.onCropsGrowPost(level, pos, state);
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(AGE) == 2) {
            if (!level.isClientSide()) {
                int fruits = state.getValue(FRUITS);
                Block.popResource(level, pos, new ItemStack(EnvironmentalItems.PLUM.get(), fruits));
                level.setBlock(pos, state.setValue(AGE, 0), 2);
            }
            level.playSound(player, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS,
                    1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level,
                                       BlockPos pos, Player player) {
        return new ItemStack(EnvironmentalItems.PLUM.get());
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return state.getValue(AGE) < 2;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < 2 && ForgeHooks.onCropsGrowPre(level, pos, state, true)) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
            ForgeHooks.onCropsGrowPost(level, pos, state);
        }
    }
}