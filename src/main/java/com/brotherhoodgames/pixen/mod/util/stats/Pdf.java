package com.brotherhoodgames.pixen.mod.util.stats;

import static com.brotherhoodgames.pixen.mod.util.DoubleRange.UNIT_RANGE;

import com.brotherhoodgames.pixen.mod.util.DoubleRange;
import com.brotherhoodgames.pixen.mod.util.NonnullFunction;
import java.io.Serializable;
import javax.annotation.Nonnull;
import net.minecraft.util.RandomSource;

public interface Pdf extends Serializable {
  double samplePdf(double x);

  double sampleCdf(double x);

  default double sampleCdf(@Nonnull RandomSource r) {
    return sampleCdf(r.nextDouble());
  }

  default @Nonnull NonnullFunction<Double, Double> pdfToFunction(
      @Nonnull DoubleRange domain, @Nonnull DoubleRange range) {
    return x -> UNIT_RANGE.project(this.samplePdf(domain.normalize(x)), range);
  }

  static @Nonnull DoubleRange.RangeMaxBuilder domainFrom(double min) {
    return DoubleRange.from(min);
  }

  static @Nonnull DoubleRange.RangeMaxBuilder rangeFrom(double min) {
    return DoubleRange.from(min);
  }
}
