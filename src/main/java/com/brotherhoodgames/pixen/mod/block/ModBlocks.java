package com.brotherhoodgames.pixen.mod.block;

import com.brotherhoodgames.pixen.mod.PixenMod;
import com.brotherhoodgames.pixen.mod.item.ModItems;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

  public static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(ForgeRegistries.BLOCKS, PixenMod.MOD_ID);

  public static final RegistryObject<Block> REDWOOD_WOOD_BLOCK =
      registerBlock("redwood_wood_block", () -> new Block(BlockBehaviour.Properties.of(Material.WOOD)));

  private static <T extends Block> RegistryObject<T> registerBlock(
      @Nonnull String name, @Nonnull Supplier<T> block) {
    RegistryObject<T> toReturn = BLOCKS.register(name, block);
    registerBlockItem(name, toReturn);
    return toReturn;
  }

  private static <T extends Block> RegistryObject<Item> registerBlockItem(
      @Nonnull String name, @Nonnull RegistryObject<T> block) {
    return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
  }

  public static void register(@Nonnull IEventBus eventBus) {
    BLOCKS.register(eventBus);
  }

  private ModBlocks() {}
}
