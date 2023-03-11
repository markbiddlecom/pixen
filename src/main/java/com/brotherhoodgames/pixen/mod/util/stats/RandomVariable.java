package com.brotherhoodgames.pixen.mod.util.stats;

import java.io.Serializable;
import java.util.Random;
import javax.annotation.Nonnull;
import net.minecraft.util.RandomSource;

public interface RandomVariable extends Serializable {
  double sample(double x);

  default double sample(@Nonnull Random r) {
    return sample(r.nextDouble());
  }

  default double sample(@Nonnull RandomSource r) {
    return sample(r.nextDouble());
  }

  default float sampleFloat(float x) {
    return (float) sample(x);
  }

  default @Nonnull RandomVariable clamp(double min, double max) {
    return new ClampedRandomVariable(this, min, max);
  }

  default @Nonnull RandomVariable compositeWith(@Nonnull RandomVariable rhs) {
    return new CompositeRandomVariable(this, rhs);
  }
}
