package com.brotherhoodgames.pixen.mod.util.stats;

import javax.annotation.Nonnull;

public class RandomVariables {
  public static @Nonnull RandomVariable normalVariable(double standardDeviation, double mean) {
    return new NormalVariable(standardDeviation, mean);
  }

  public static @Nonnull RandomVariable confidenceInterval(
      double confidence, double min, double max) {
    return new ConfidenceIntervalNormalVariable(confidence, min, max);
  }

  public static @Nonnull RandomVariable constant(double c) {
    return new ConstantVariable(c);
  }

  public static @Nonnull RandomVariable linear(double slope, double offset) {
    return new LinearVariable(slope, offset);
  }

  public static @Nonnull RandomVariable range(double min, double max) {
    return LinearVariable.fromRange(min, max);
  }

  public static @Nonnull RandomVariable clampedRange(double min, double max) {
    return LinearVariable.fromRange(min, max).clamp(min, max);
  }

  private RandomVariables() {}
}
