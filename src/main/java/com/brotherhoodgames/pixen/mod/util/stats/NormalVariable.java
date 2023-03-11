package com.brotherhoodgames.pixen.mod.util.stats;

import java.util.Random;
import javax.annotation.Nonnull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

@Getter
@ToString
@EqualsAndHashCode
public class NormalVariable implements RandomVariable {
  private static final double STD_DEV_TERM = Math.sqrt(2 * Math.PI);

  private final double standardDeviation;
  private final double mean;
  private final double term;

  public NormalVariable(double standardDeviation, double mean) {
    this.standardDeviation = standardDeviation;
    this.mean = mean;
    this.term = 1.0 / (standardDeviation * STD_DEV_TERM);
  }

  @Override
  public double sample(double x) {
    double meanTerm = (x - mean) / standardDeviation;
    return term * Math.pow(Math.E, -0.5 * meanTerm * meanTerm);
  }

  @Override
  public double sample(@Nonnull Random r) {
    return r.nextGaussian() * standardDeviation + mean;
  }

  @Override
  public double sample(@NotNull RandomSource r) {
    return r.nextGaussian() * standardDeviation + mean;
  }
}
