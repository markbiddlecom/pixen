package com.brotherhoodgames.pixen.mod.util.stats;

import com.brotherhoodgames.pixen.mod.util.DoubleRange;
import com.brotherhoodgames.pixen.mod.util.NonnullFunction;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

public class ManualPdf implements Pdf {
  @VisibleForTesting static final double EPSILON = 1e-9;

  private final SampleInterpolator interpolator;
  private final ImmutableList<Double> pdf;
  private final ImmutableList<Double> cdf;

  public ManualPdf(@Nonnull double... distribution) {
    this(DoubleStream.of(distribution).boxed());
  }

  public ManualPdf(
      @Nonnull SampleInterpolator sampleInterpolator, @Nonnull double... distribution) {
    this(sampleInterpolator, DoubleStream.of(distribution).boxed());
  }

  public <N extends Number> ManualPdf(@Nonnull Collection<N> distribution) {
    this(distribution.stream());
  }

  public ManualPdf(@Nonnull DoubleStream distribution) {
    this(distribution.boxed());
  }

  public <N extends Number> ManualPdf(@Nonnull Stream<N> distribution) {
    this(DEFAULT_INTERPOLATOR, distribution);
  }

  public <N extends Number> ManualPdf(
      @Nonnull SampleInterpolator interpolator, @Nonnull Stream<N> distribution) {
    this.interpolator = interpolator;
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
  public double samplePdf(double x) {
    if (x < 0 || x > 1) return 0;

    double pos = x * pdf.size();
    int i = (int) Math.floor(pos) - 1;
    if (i < -1 || i > pdf.size()) {
      return 0;
    } else {
      double min = i >= 0 && i < pdf.size() ? pdf.get(i) : 0;
      double max = i < pdf.size() - 1 ? pdf.get(i + 1) : 0;
      return interpolator.lerp(min, max, pos - i + 1);
    }
  }

  @Override
  public double sampleCdf(double x) {
    if (x <= 0) return 0;
    if (x >= 1) return 1;

    int i = Collections.binarySearch(cdf, x);
    if (i < 0) {
      i = -i - 1;
    } else {
      i++;
    }

    double maxSample = i < cdf.size() ? cdf.get(i) : 1;
    double minSample = i > 0 ? cdf.get(i - 1) : 0;
    double sampleRange = maxSample - minSample;

    double iInt =
        interpolator.lerp(i, i + 1.0, sampleRange < EPSILON ? 0 : (x - minSample) / sampleRange);

    return iInt / cdf.size();
  }

  @NotNull
  @Override
  public NonnullFunction<Double, Double> pdfToFunction(
      @Nonnull DoubleRange domain, @Nonnull DoubleRange range) {
    final double max = pdf.stream().mapToDouble(d -> d).max().orElse(0);
    final double min = 0;
    final double existingRange = max - min;
    return x -> {
      double normalizedX = domain.normalize(x);
      double normalizedSample = (samplePdf(normalizedX) - min) / existingRange;
      return range.lerp(normalizedSample);
    };
  }

  @FunctionalInterface
  public interface SampleInterpolator extends Serializable {
    double lerp(double min, double max, double x);
  }

  public static final SampleInterpolator MIN_STEP_INTERPOLATOR = (min, max, x) -> min;
  public static final SampleInterpolator MAX_STEP_INTERPOLATOR = (min, max, x) -> max;
  public static final SampleInterpolator LINEAR_INTERPOLATOR =
      (min, max, x) -> min + (max - min) * x;

  public static final SampleInterpolator DEFAULT_INTERPOLATOR = MIN_STEP_INTERPOLATOR;
}
