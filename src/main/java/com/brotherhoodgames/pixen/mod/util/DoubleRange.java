package com.brotherhoodgames.pixen.mod.util;

import java.io.Serializable;
import java.util.Optional;
import javax.annotation.Nonnull;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class DoubleRange implements Serializable {
  public static final DoubleRange UNIT_RANGE = new DoubleRange(0, 1);

  public final double min;
  public final double max;
  public final double width;

  public DoubleRange(double min, double max) {
    this.min = min;
    this.max = max;
    this.width =
        Double.isInfinite(min) || Double.isInfinite(max) ? Double.POSITIVE_INFINITY : max - min;
  }

  public static @Nonnull RangeMaxBuilder from(double min) {
    return new RangeMaxBuilder() {
      @Nonnull
      @Override
      public DoubleRange to(double max) {
        return new DoubleRange(min, max);
      }

      @Nonnull
      @Override
      public DoubleRange spanning(double width) {
        return new DoubleRange(min, min + width);
      }
    };
  }

  public static @Nonnull RangeMaxBuilder fromOrigin() {
    return from(0);
  }

  public double clamp(double x) {
    return x < min ? min : (x > max ? max : x);
  }

  public double distanceFrom(double x) {
    return x < min ? min - x : (x > max ? x - max : 0);
  }

  public boolean contains(double x) {
    return x >= min && x <= max;
  }

  public boolean doesNotContain(double x) {
    return !contains(x);
  }

  public double lerp(double amount) {
    return min + width * amount;
  }

  public double lerpClamped(double amount) {
    if (amount <= 0) return min;
    else if (amount >= 1) return max;
    else return lerp(amount);
  }

  public double normalize(double x) {
    return (x - min) / width;
  }

  public double normalizeClamped(double x) {
    return normalize(clamp(x));
  }

  public @Nonnull Optional<Double> returnIfContained(double x) {
    if (x < min || x > max) return Optional.empty();
    else return Optional.of(x);
  }

  public double project(double x, @Nonnull DoubleRange toRange) {
    return toRange.lerp(normalize(x));
  }

  public double projectClamped(double x, @Nonnull DoubleRange toRange) {
    return toRange.lerp(normalizeClamped(x));
  }

  @Override
  public String toString() {
    return "<DoubleRange>(" + min + ", " + max + ")";
  }

  public interface RangeMaxBuilder {
    @Nonnull
    DoubleRange to(double max);

    @Nonnull
    DoubleRange spanning(double width);
  }
}
