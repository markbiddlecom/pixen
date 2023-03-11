package com.brotherhoodgames.pixen.mod.util;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface NonnullFunction<INPUT, RETURN> {
  @Nonnull
  RETURN apply(@Nonnull INPUT input);
}
