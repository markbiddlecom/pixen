package com.brotherhoodgames.pixen.mod.util;

import javax.annotation.Nonnull;
import net.minecraft.util.RandomSource;

public class Randomness {
  public static <T> T oneOf(@Nonnull RandomSource random, T... options) {
    if (options.length == 0) return null;
    else return options[random.nextInt(options.length)];
  }

  private Randomness() {}
}
