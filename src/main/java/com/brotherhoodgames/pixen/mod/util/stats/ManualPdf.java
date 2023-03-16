package com.brotherhoodgames.pixen.mod.util.stats;

import com.brotherhoodgames.pixen.mod.util.NonnullFunction;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

public class ManualPdf implements Pdf {
  private final ImmutableList<Double> pdf;
  private final ImmutableList<Double> cdf;

  public <N extends Number> ManualPdf(@Nonnull Collection<N> distribution) {
    this(distribution.stream());
  }

  public ManualPdf(@Nonnull DoubleStream distribution) {
    this(distribution.boxed());
  }

  public <N extends Number> ManualPdf(@Nonnull Stream<N> distribution) {
    double[] total = {0};
    boolean[] nullElement = {false};
    Number[] invalid = {null};

    ImmutableList.Builder<Double> cdf = ImmutableList.builder();
    this.pdf =
        distribution
            .map(
                n -> {
                  if (n == null) {
                    nullElement[0] = true;
                    return 0d;
                  }
                  double d = n.doubleValue();
                  if (d < 0 && invalid[0] == null) {
                    invalid[0] = n;
                  }
                  total[0] += d;
                  cdf.add(total[0]);
                  return d;
                })
            .collect(ImmutableList.toImmutableList());
    this.cdf = cdf.build();

    if (Math.abs(1.0 - total[0]) > 1e-9) {
      throw new IllegalArgumentException("Distribution stream did not sum to 1.0");
    } else if (nullElement[0]) {
      throw new NullPointerException("Distribution contained at least one null element");
    } else if (invalid[0] != null) {
      throw new IllegalArgumentException("Distribution contained an invalid element " + invalid[0]);
    }
  }

  @Override
  public double sample(double x) {
    int i = (int) Math.round(x * pdf.size());
    return i < 0 ? 0 : (i >= pdf.size() ? 0 : pdf.get(i));
  }

  @Override
  public double sample(@Nonnull RandomSource r) {
    int i = Collections.binarySearch(cdf, r.nextDouble());
    if (i < 0) {
      i = -i - 1;
    }
    return (double) i / cdf.size();
  }

  @NotNull
  @Override
  public NonnullFunction<Double, Double> toFunction(
      double domainStart, double domain, double rangeStart, double range) {
    final double[] min = {pdf.get(0)}, max = {pdf.get(0)};
    pdf.forEach(
        d -> {
          if (d < min[0]) min[0] = d;
          if (d > max[0]) max[0] = d;
        });
    final double existingRangeStart = min[0];
    final double existingRange = max[0] - min[0];
    return x -> {
      double normalizedX = (x - domainStart) / domain;
      double normalizedSample = (sample(normalizedX) - existingRangeStart) / existingRange;
      return rangeStart + range * normalizedSample;
    };
  }
}
