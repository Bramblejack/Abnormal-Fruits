package com.bramblejack.abnormalfruits.events;

import com.bramblejack.abnormalfruits.AbnormalFruits;
import com.bramblejack.abnormalfruits.block.CherriesPlantBlock;
import com.bramblejack.abnormalfruits.block.PlumPlantBlock;
import com.bramblejack.abnormalfruits.init.AbnormalFruitsBlocks;
import com.teamabnormals.environmental.core.registry.EnvironmentalItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Predicate;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = AbnormalFruits.MODID)
public class HangingFruitHandler {

    private record FruitPlant(
            Supplier<Item> fruit,
            Supplier<Block> plant,
            IntegerProperty ageProp,
            IntegerProperty fruitsProp,
            int maxFruits,
            Predicate<BlockState> leavesCheck
    ) {
        boolean matchesFruit(ItemStack stack) { return stack.is(fruit.get()); }
        boolean matchesPlant(BlockState state) { return state.is(plant.get()); }
    }

    private static final FruitPlant PLUM = new FruitPlant(
            () -> EnvironmentalItems.PLUM.get(),
            () -> AbnormalFruitsBlocks.PLUM_PLANT.get(),
            PlumPlantBlock.AGE,
            PlumPlantBlock.FRUITS,
            3,
            PlumPlantBlock::isPlumLeaves
    );

    private static final FruitPlant CHERRIES = new FruitPlant(
            () -> EnvironmentalItems.CHERRIES.get(),
            () -> AbnormalFruitsBlocks.CHERRIES_PLANT.get(),
            CherriesPlantBlock.AGE,
            CherriesPlantBlock.FRUITS,
            2,
            CherriesPlantBlock::isCherryLeaves
    );

    private static final FruitPlant[] PLANTS = { PLUM, CHERRIES };

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        FruitPlant fp = findByFruit(held);
        if (fp == null) return;

        if (fp.matchesPlant(state)) {
            int age = state.getValue(fp.ageProp());
            int fruits = state.getValue(fp.fruitsProp());
            if (age != 0 || fruits >= fp.maxFruits()) return;

            deny(event);
            player.stopUsingItem();
            player.swing(InteractionHand.MAIN_HAND, true);
            playSound(level, player, pos);

            if (!level.isClientSide()) {
                level.setBlockAndUpdate(pos, state.setValue(fp.fruitsProp(), fruits + 1));
                if (!player.getAbilities().instabuild) held.shrink(1);
            }
            return;
        }

        if (fp.leavesCheck().test(state) && event.getFace() == Direction.DOWN) {
            BlockPos below = pos.below();
            if (!level.getBlockState(below).isAir()) return;

            BlockState plantState = fp.plant().get().defaultBlockState()
                    .setValue(fp.ageProp(), 0)
                    .setValue(fp.fruitsProp(), 1);

            if (!plantState.canSurvive(level, below)) return;

            deny(event);
            player.stopUsingItem();
            player.swing(InteractionHand.MAIN_HAND, true);
            playSound(level, player, below);

            if (!level.isClientSide()) {
                level.setBlockAndUpdate(below, plantState);
                if (!player.getAbilities().instabuild) held.shrink(1);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        Level level = event.getLevel();

        FruitPlant fp = findByFruit(held);
        if (fp == null) return;

        HitResult hit = player.pick(5.0, 1.0f, false);
        if (hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (fp.matchesPlant(state)) {
            int age = state.getValue(fp.ageProp());
            int fruits = state.getValue(fp.fruitsProp());
            if (age == 0 && fruits < fp.maxFruits()) {
                event.setCanceled(true);
                return;
            }
        }

        if (fp.leavesCheck().test(state) && blockHit.getDirection() == Direction.DOWN) {
            if (level.getBlockState(pos.below()).isAir()) {
                event.setCanceled(true);
            }
        }
    }

    private static FruitPlant findByFruit(ItemStack stack) {
        for (FruitPlant fp : PLANTS) {
            if (fp.matchesFruit(stack)) return fp;
        }
        return null;
    }

    private static void deny(PlayerInteractEvent.RightClickBlock event) {
        event.setUseItem(Event.Result.DENY);
        event.setUseBlock(Event.Result.DENY);
    }

    private static void playSound(Level level, Player player, BlockPos pos) {
        level.playSound(player, pos, SoundEvents.CAVE_VINES_PLACE, SoundSource.BLOCKS,
                1.0F, 0.8F + level.random.nextFloat() * 0.4F);
    }
}