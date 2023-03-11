package com.brotherhoodgames.pixen.mod.util.stats;

import java.util.Random;
import lombok.Data;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

@Data
public class CompositeRandomVariable implements RandomVariable {
  private final RandomVariable lhs;
  private final RandomVariable rhs;

  @Override
  public double sample(double x) {
    return lhs.sample(x) + rhs.sample(x);
  }

  @Override
  public double sample(@NotNull Random r) {
    return lhs.sample(r) + rhs.sample(r);
  }

  @Override
  public double sample(@NotNull RandomSource r) {
    return lhs.sample(r) + rhs.sample(r);
  }

  @Override
  public float sampleFloat(float x) {
    return lhs.sampleFloat(x) + rhs.sampleFloat(x);
  }
}
