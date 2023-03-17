package com.brotherhoodgames.pixen.mod.util.stats;

import javax.annotation.Nonnull;
import lombok.Data;

@Data
public class LinearVariable implements RandomVariable {
  private final double slope, offset;

  public LinearVariable(double offset, double slope) {
    this.slope = slope;
    this.offset = offset;
  }

  public static @Nonnull LinearVariable fromRange(double min, double max) {
    return new LinearVariable(min, max - min);
  }

  @Override
  public double sample(double x) {
    return offset + slope * x;
  }
}
