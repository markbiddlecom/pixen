package com.brotherhoodgames.pixen.mod.util.stats;

import lombok.Data;

@Data
public class ClampedRandomVariable implements RandomVariable {
  private final RandomVariable basis;
  private final double min;
  private final double max;

  @Override
  public double sample(double r) {
    return Math.min(Math.max(min, basis.sample(r)), max);
  }
}
