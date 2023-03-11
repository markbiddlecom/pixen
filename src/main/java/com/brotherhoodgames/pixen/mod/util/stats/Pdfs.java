package com.brotherhoodgames.pixen.mod.util.stats;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;

public class Pdfs {
  public static @Nonnull Pdf fromHistogram(@Nonnull String histogram) {
    return fromHistogram(
        histogram, -1, c -> c == '\r' || c == '\n', c -> !Character.isWhitespace(c));
  }

  public static @Nonnull Pdf fromHistogram(
      @Nonnull String histogram,
      int yAxisDirection,
      @Nonnull IntPredicate lineSeparator,
      @Nonnull IntPredicate value) {
    Map<Integer, Integer> distribution = Maps.newHashMap();
    int[] hy = {0};
    int[] hx = {0};
    int[] maxx = {0};
    histogram
        .chars()
        .forEachOrdered(
            c -> {
              if (lineSeparator.test(c)) {
                if (hx[0] > 0) hy[0]++;
                hx[0] = 0;
              } else {
                if (value.test(c) && (!distribution.containsKey(hx[0]) || yAxisDirection > 0)) {
                  distribution.put(hx[0], hy[0]);
                }
                hx[0]++;
                if (hx[0] > maxx[0]) maxx[0] = hx[0];
              }
            });
    if (hx[0] > 0) hy[0]++;
    double[] total = {0};
    distribution.forEach(
        (x, y) -> {
          if (yAxisDirection < 0) {
            y = hy[0] - y;
            distribution.put(x, y);
          }
          total[0] += y;
        });
    return new ManualPdf(
        IntStream.range(0, maxx[0]).mapToDouble(x -> distribution.getOrDefault(x, 0) / total[0]));
  }

  private Pdfs() {}
}
