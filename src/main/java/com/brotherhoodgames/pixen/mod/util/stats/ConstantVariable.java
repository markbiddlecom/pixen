package com.brotherhoodgames.pixen.mod.util.stats;

import lombok.Data;

@Data
public class ConstantVariable implements RandomVariable {
  final double c;

  @Override
  public double sample(double x) {
    return c;
  }
}
