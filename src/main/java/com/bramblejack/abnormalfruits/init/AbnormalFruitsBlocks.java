package com.bramblejack.abnormalfruits.init;

import com.bramblejack.abnormalfruits.AbnormalFruits;
import com.bramblejack.abnormalfruits.block.CherriesPlantBlock;
import com.bramblejack.abnormalfruits.block.PlumPlantBlock;
import com.teamabnormals.blueprint.core.util.registry.BlockSubRegistryHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.RegistryObject;

public class AbnormalFruitsBlocks {

    public static final BlockSubRegistryHelper ABNORMALFRUITS_HELPER =
            AbnormalFruits.REGISTRY_HELPER.getBlockSubHelper();

    public static final RegistryObject<Block> PLUM_PLANT = ABNORMALFRUITS_HELPER.createBlockNoItem(
            "plum_plant",
            () -> new PlumPlantBlock(BlockBehaviour.Properties.of()
                    .strength(0.3F)
                    .noCollission()
                    .randomTicks()
                    .sound(SoundType.CROP)
                    .pushReaction(PushReaction.DESTROY)));

    public static final RegistryObject<Block> CHERRIES_PLANT = ABNORMALFRUITS_HELPER.createBlockNoItem(
            "cherries_plant",
            () -> new CherriesPlantBlock(BlockBehaviour.Properties.of()
                    .strength(0.3F)
                    .noCollission()
                    .randomTicks()
                    .sound(SoundType.CROP)
                    .pushReaction(PushReaction.DESTROY)));

    // Called from AbnormalFruits constructor to force class initialization before RegisterEvent fires
    public static void init() {}
}