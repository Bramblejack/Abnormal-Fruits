package com.bramblejack.abnormalfruits.worldgen.treedecorators;

import com.bramblejack.abnormalfruits.block.CherriesPlantBlock;
import com.bramblejack.abnormalfruits.block.PlumPlantBlock;
import com.bramblejack.abnormalfruits.init.AbnormalFruitsBlocks;
import com.bramblejack.abnormalfruits.init.AbnormalFruitsTreeDecorators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class HangingFruitDecorator extends TreeDecorator {

    public enum FruitType implements StringRepresentable {
        PLUM("plum", () -> AbnormalFruitsBlocks.PLUM_PLANT.get(), PlumPlantBlock.AGE, PlumPlantBlock.FRUITS, 3),
        CHERRIES("cherries", () -> AbnormalFruitsBlocks.CHERRIES_PLANT.get(), CherriesPlantBlock.AGE, CherriesPlantBlock.FRUITS, 2);

        public static final Codec<FruitType> CODEC = StringRepresentable.fromEnum(FruitType::values);

        private final String name;
        private final Supplier<Block> block;
        private final IntegerProperty ageProp;
        private final IntegerProperty fruitsProp;
        private final int maxFruits;

        FruitType(String name, Supplier<Block> block, IntegerProperty ageProp, IntegerProperty fruitsProp, int maxFruits) {
            this.name = name;
            this.block = block;
            this.ageProp = ageProp;
            this.fruitsProp = fruitsProp;
            this.maxFruits = maxFruits;
        }

        public BlockState defaultState(RandomSource random) {
            return block.get().defaultBlockState()
                    .setValue(ageProp, 0)
                    .setValue(fruitsProp, 1 + random.nextInt(maxFruits));
        }

        public Block getBlock() { return block.get(); }

        @Override
        public String getSerializedName() { return name; }
    }

    public static final Codec<HangingFruitDecorator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    FruitType.CODEC.fieldOf("fruit").forGetter(d -> d.fruitType),
                    Codec.floatRange(0.0f, 1.0f).fieldOf("probability").forGetter(d -> d.probability),
                    Codec.floatRange(0.0f, 1.0f).fieldOf("plant_probability").forGetter(d -> d.plantProbability)
            ).apply(instance, HangingFruitDecorator::new));

    private final FruitType fruitType;
    private final float probability;
    private final float plantProbability;

    public HangingFruitDecorator(FruitType fruitType, float probability, float plantProbability) {
        this.fruitType = fruitType;
        this.probability = probability;
        this.plantProbability = plantProbability;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return AbnormalFruitsTreeDecorators.HANGING_FRUIT.get();
    }

    @Override
    public void place(Context context) {
        RandomSource random = context.random();
        if (random.nextFloat() >= probability) return;

        Set<BlockPos> logPositions = new HashSet<>(context.logs());
        List<BlockPos> candidates = new ArrayList<>(context.leaves());

        for (int i = candidates.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            BlockPos temp = candidates.get(i);
            candidates.set(i, candidates.get(j));
            candidates.set(j, temp);
        }

        for (BlockPos pos : candidates) {
            BlockPos below = pos.below();
            if (!context.isAir(below)) continue;
            if (logPositions.contains(pos)) continue;
            if (random.nextFloat() >= plantProbability) continue;
            context.setBlock(below, fruitType.defaultState(random));
        }
    }
}