package com.brotherhoodgames.pixen.mod.util.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ConfidenceIntervalNormalVariableTest {
  @Test
  public void testConfidenceInterval() {
    final double confidence = 0.8, min = 10, max = 20;
    final long count = 1_000_000;

    RandomVariable var = RandomVariables.confidenceInterval(confidence, min, max);
    Random r = new Random();
    double total = 0;
    long countWithinRange = 0;

    for (int i = 0; i < count; i++) {
      double sample = var.sample(r);
      total += sample;
      if (sample > min && sample < max) {
        countWithinRange++;
      }
    }

    assertEquals(
        0.8,
        (double) countWithinRange / count,
        0.01,
        "Approximately 80% of samples fall within the expected range");
    assertEquals((min + max) / 2.0, total / count, 0.1, "The mean is approximately correct");
  }

  @Test
  @Disabled("This method produces a histogram of sample values")
  void printConfidenceIntervalHistogram() {
    RandomVariable var = RandomVariables.confidenceInterval(.9, -5, 15);
    Map<Integer, Integer> counts = Maps.newHashMap();
    Random r = new Random();
    float max = 0;
    Integer minSample = null;
    Integer maxSample = null;
    for (int i = 0; i < 100_000; i++) {
      int sample = (int) Math.floor(var.sample(r));
      int count = counts.getOrDefault(sample, 0) + 1;
      counts.put(sample, count);
      if (count > max) max = count;
      if (minSample == null || sample < minSample) minSample = sample;
      if (maxSample == null || sample > maxSample) maxSample = sample;
    }
    for (int s = minSample; s <= maxSample; s++) {
      int count = counts.getOrDefault(s, 0);
      int bar = (int) (70.0 * (count / max));
      if (s == -5 || s == 16) {
        System.out.print(" ----|-");
        System.out.println("-".repeat(80));
      }
      System.out.printf("%4d | %-70s (%,d)\n", s, "=".repeat(bar), count);
    }
  }
}
