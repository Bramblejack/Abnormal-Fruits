package com.bramblejack.abnormalfruits.init;

import com.bramblejack.abnormalfruits.AbnormalFruits;
import com.bramblejack.abnormalfruits.worldgen.treedecorators.HangingFruitDecorator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class AbnormalFruitsTreeDecorators {

    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATORS =
            DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, AbnormalFruits.MODID);

    public static final RegistryObject<TreeDecoratorType<HangingFruitDecorator>> HANGING_FRUIT =
            TREE_DECORATORS.register("hanging_fruit", () ->
                    new TreeDecoratorType<>(HangingFruitDecorator.CODEC));
}