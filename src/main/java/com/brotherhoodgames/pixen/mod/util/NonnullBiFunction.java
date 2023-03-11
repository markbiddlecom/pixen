package com.brotherhoodgames.pixen.mod.util;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface NonnullBiFunction<PARAM1, PARAM2, RESULT> {
  @Nonnull
  RESULT apply(@Nonnull PARAM1 param1, @Nonnull PARAM2 param2);
}
