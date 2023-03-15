package com.brotherhoodgames.pixen.mod.util.stats;

import com.brotherhoodgames.pixen.mod.util.NonnullFunction;
import java.io.Serializable;
import javax.annotation.Nonnull;
import net.minecraft.util.RandomSource;

public interface Pdf extends Serializable {
  double sample(double x);

  double sample(@Nonnull RandomSource r);

  default @Nonnull NonnullFunction<Double, Double> toFunction(
      double domainStart, double domain, double rangeStart, double range) {
    return x -> rangeStart + this.sample((x - domainStart) / domain) * range;
  }
}
