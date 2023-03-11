package com.brotherhoodgames.pixen.mod.util.stats;

import java.io.Serializable;
import javax.annotation.Nonnull;
import net.minecraft.util.RandomSource;

public interface Pdf extends Serializable {
  double sample(double x);

  double sample(@Nonnull RandomSource r);
}
