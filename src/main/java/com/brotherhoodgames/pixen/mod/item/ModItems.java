package com.brotherhoodgames.pixen.mod.item;

import com.brotherhoodgames.pixen.mod.PixenMod;
import javax.annotation.Nonnull;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
  public static final DeferredRegister<Item> ITEMS =
      DeferredRegister.create(ForgeRegistries.ITEMS, PixenMod.MOD_ID);

  public static void register(@Nonnull IEventBus eventBus) {
    ITEMS.register(eventBus);
  }

  private ModItems() {}
}
