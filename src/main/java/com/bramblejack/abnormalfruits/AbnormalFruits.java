package com.bramblejack.abnormalfruits;

import com.bramblejack.abnormalfruits.init.AbnormalFruitsBlocks;
import com.bramblejack.abnormalfruits.init.AbnormalFruitsTreeDecorators;
import com.teamabnormals.blueprint.core.util.registry.RegistryHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AbnormalFruits.MODID)
public class AbnormalFruits {

    public static final String MODID = "abnormalfruits";
    public static final RegistryHelper REGISTRY_HELPER = new RegistryHelper(MODID);

    public AbnormalFruits() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        AbnormalFruitsBlocks.init();

        REGISTRY_HELPER.register(modBus);
        AbnormalFruitsTreeDecorators.TREE_DECORATORS.register(modBus);
    }
}