package com.brotherhoodgames.pixen.mod.util;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;

public class Randomness {
  public static @Nullable <T> T oneOf(@Nonnull RandomSource random, T... options) {
    if (options.length == 0) return null;
    else return options[random.nextInt(options.length)];
  }

  public static @Nullable <T> T oneOf(@Nonnull RandomSource random, @Nullable List<T> options) {
    if (options == null || options.isEmpty()) return null;
    else return options.get(random.nextInt(options.size()));
  }

  public static @Nonnull <T> T oneOf(
      @Nonnull RandomSource random, @Nonnull T defaultValue, @Nullable List<T> options) {
    if (options == null || options.isEmpty()) return defaultValue;
    else
      return Optional.ofNullable(options.get(random.nextInt(options.size()))).orElse(defaultValue);
  }

  private Randomness() {}
}
