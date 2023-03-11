package com.brotherhoodgames.pixen.mod;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_O;

import com.brotherhoodgames.pixen.mod.block.ModBlocks;
import com.brotherhoodgames.pixen.mod.item.ModItems;
import com.brotherhoodgames.pixen.mod.tree.GiantRedwoodGenerator;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(PixenMod.MOD_ID)
public class PixenMod {
  public static final String MOD_ID = "pixen";
  private static final Logger LOGGER = LogUtils.getLogger();
  private static Minecraft mc = Minecraft.getInstance();

  private static final KeyMapping TEST_TREE_MAPPING =
      new KeyMapping(
          "key.categories.misc",
          KeyConflictContext.IN_GAME,
          InputConstants.Type.KEYSYM,
          GLFW_KEY_O,
          "key.pixen.create_test_tree");

  public PixenMod() {
    IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

    ModItems.register(modEventBus);
    ModBlocks.register(modEventBus);

    modEventBus.addListener(this::commonSetup);
    MinecraftForge.EVENT_BUS.register(this);
    modEventBus.addListener(this::addCreative);
  }

  private void commonSetup(final FMLCommonSetupEvent event) {}

  private void addCreative(@Nonnull CreativeModeTabEvent.BuildContents event) {
    if (event.getTab() == CreativeModeTabs.BUILDING_BLOCKS) {
      event.accept(ModBlocks.REDWOOD_WOOD_BLOCK);
    }
  }

  // You can use EventBusSubscriber to automatically register all static methods in the class
  // annotated with @SubscribeEvent
  @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
  public static class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
      // Some client setup code
      LOGGER.info("HELLO FROM CLIENT SETUP");
      LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    public static void onKeyRegister(@Nonnull RegisterKeyMappingsEvent e) {
      e.register(TEST_TREE_MAPPING);
    }
  }

  @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
  public static class ClientInputEvents {
    @SubscribeEvent
    public static void onKeyInput(@Nonnull InputEvent.Key e) {
      if (TEST_TREE_MAPPING.consumeClick()) {
        Camera c = mc.gameRenderer.getMainCamera();
        Optional.ofNullable(mc.level)
            .map(
                l ->
                    l.clip(
                        new ClipContext(
                            c.getPosition(),
                            c.getPosition().add(new Vec3(c.getLookVector().normalize().mul(500f))),
                            ClipContext.Block.OUTLINE,
                            ClipContext.Fluid.NONE,
                            null)))
            .filter(r -> r.getType() != HitResult.Type.MISS)
            .map(BlockHitResult::getBlockPos)
            .ifPresent(
                where ->
                    new GiantRedwoodGenerator()
                        .generate(
                            where.getX(),
                            where.getY(),
                            where.getZ(),
                            RandomSource.create(),
                            (b, x, y, z) -> {
                              mc.level.setBlock(
                                  new BlockPos(x, y, z),
                                  b == GiantRedwoodGenerator.TreeBlock.BARK
                                      ? Blocks.SPRUCE_WOOD.defaultBlockState()
                                      : Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState(),
                                  3);
                            }));
      }
    }
  }
}
