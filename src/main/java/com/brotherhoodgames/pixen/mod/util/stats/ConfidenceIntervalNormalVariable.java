package com.brotherhoodgames.pixen.mod.util.stats;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.math3.special.Erf;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class ConfidenceIntervalNormalVariable extends NormalVariable {
  private final double confidence;
  private final double min;
  private final double max;

  public ConfidenceIntervalNormalVariable(double confidence, double min, double max) {
    super(calculateStdDev(confidence, min, max), calculateMean(min, max));
    this.confidence = confidence;
    this.min = min;
    this.max = max;
  }

  private static double calculateStdDev(double confidence, double min, double max) {
    return (min - calculateMean(min, max)) / -calculateZValue(confidence);
  }

  private static double calculateMean(double min, double max) {
    return (min + max) / 2.0;
  }

  private static double calculateZValue(double confidence) {
    return -1.0 * Erf.erfcInv(1.0 - confidence) * Math.sqrt(2);
  }
}
