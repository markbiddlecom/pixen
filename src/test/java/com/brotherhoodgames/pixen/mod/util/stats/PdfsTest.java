package com.brotherhoodgames.pixen.mod.util.stats;

import static com.brotherhoodgames.pixen.mod.util.stats.ManualPdf.EPSILON;
import static com.brotherhoodgames.pixen.mod.util.stats.Pdfs.fromHistogram;
import static com.brotherhoodgames.pixen.mod.util.stats.Pdfs.fromHistogramLines;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PdfsTest {
  @Test
  void testFromHistogramEmptyText() {
    assertThrows(IllegalArgumentException.class, () -> fromHistogram(""));
  }

  @Test
  void testFromHistogramNullText() {
    assertThrows(NullPointerException.class, () -> fromHistogram(null));
  }

  @Test
  void testFromHistogramConstant() {
    ManualPdf subject = fromHistogram("****");
    assertArrayEquals(new double[] {0.25, 0.25, 0.25, 0.25}, subject.pdfToArray(), EPSILON);
  }

  @Test
  void testFromHistogramLinear() {
    ManualPdf subject =
        fromHistogramLines(
            "   *", // ↩
            "  * ", // ↩
            " *  ", // ↩
            "*   ");
    assertArrayEquals(new double[] {0.1, 0.2, 0.3, 0.4}, subject.pdfToArray(), EPSILON);
  }

  @Test
  void testFromHistogramExponential() {
    ManualPdf subject =
        fromHistogramLines(
            "    *", // ↩
            "     ", // ↩
            "     ", // ↩
            "     ", // ↩
            "   * ", // ↩
            "     ", // ↩
            "  *  ", // ↩
            " *   ");
    assertArrayEquals(
        new double[] {0.0, 1d / 15, 2d / 15, 4d / 15, 8d / 15}, subject.pdfToArray(), EPSILON);
  }

  @Test
  void testFromHistogramNormalizesBarHeight() {
    ManualPdf subject =
        fromHistogramLines(
            "****", // ↩
            "    ", // ↩
            "    ", // ↩
            "    ");
    assertArrayEquals(new double[] {0.25, 0.25, 0.25, 0.25}, subject.pdfToArray(), EPSILON);
  }

  @Test
  void testFromHistogramTreatsEmptyColumnsAsZero() {
    ManualPdf subject = fromHistogram("  **");
    assertArrayEquals(new double[] {0.0, 0.0, 0.5, 0.5}, subject.pdfToArray(), EPSILON);
  }

  @Test
  void testFromHistogramIgnoresEmptyLines() {
    ManualPdf subject =
        fromHistogramLines(
            "", // ↩
            "   *", // ↩
            "  * ", // ↩
            "", // ↩
            "", // ↩
            " *  ", // ↩
            "*   ");
    assertArrayEquals(new double[] {0.1, 0.2, 0.3, 0.4}, subject.pdfToArray(), EPSILON);
  }

  @Test
  void testFromHistogramTreatsShortLinesAsEmptyCells() {
    ManualPdf subject =
        fromHistogramLines(
            "    *", // ↩
            "     ", // ↩
            "     ", // ↩
            "     ", // ↩
            "   *", // ↩
            " ", // ↩ this line counts because it has at least one character
            "", // ↩ this line is ignored because it is totally empty
            "  *", // ↩
            " *");
    assertArrayEquals(
        new double[] {0.0, 1d / 15, 2d / 15, 4d / 15, 8d / 15}, subject.pdfToArray(), EPSILON);
  }

  @Test
  void testFromHistogramRecordsOnlyTheHighestCharacter() {
    ManualPdf subject =
        fromHistogramLines(
            "   *", // ↩
            "  **", // ↩
            " ** ", // ↩
            "** *");
    assertArrayEquals(new double[] {0.1, 0.2, 0.3, 0.4}, subject.pdfToArray(), EPSILON);
  }
}
