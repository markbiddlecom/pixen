package com.brotherhoodgames.pixen.mod.util.stats;

import static com.brotherhoodgames.pixen.mod.util.stats.ManualPdf.EPSILON;
import static com.brotherhoodgames.pixen.mod.util.stats.Pdf.domainFrom;
import static com.brotherhoodgames.pixen.mod.util.stats.Pdf.rangeFrom;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ManualPdfTest {
  @ParameterizedTest
  @CsvSource({"0.0, 0.0", "0.5, 0.0", "1.0, 1.0", "2.0, 0.0"})
  void testSamplePdfSingleColumn(double x, double expected) {
    ManualPdf subject = new ManualPdf(1);
    assertEquals(expected, subject.samplePdf(x), EPSILON);
  }

  @ParameterizedTest
  @ValueSource(doubles = {-1.0, +1.0001, +2.0})
  void testSamplePdfOutOfBounds(double x) {
    ManualPdf subject = new ManualPdf(.25, .25, .25, .25);
    assertEquals(0, subject.samplePdf(x), EPSILON);
  }

  @Test
  void testSamplePdf() {
    final ManualPdf subject = new ManualPdf(0.1, 0.2, 0.4, 0.2, 0.1);
    assertAll(
        () -> assertEquals(0.0, subject.samplePdf(1.0 / 10), EPSILON, "@ 1/10"),
        () -> assertEquals(0.1, subject.samplePdf(2.0 / 10), EPSILON, "@ 2/10"),
        () -> assertEquals(0.2, subject.samplePdf(5.0 / 10), EPSILON, "@ 5/10"),
        () -> assertEquals(0.4, subject.samplePdf(7.0 / 10), EPSILON, "@ 7/10"),
        () -> assertEquals(0.2, subject.samplePdf(9.0 / 10), EPSILON, "@ 9/10"));
  }

  @ParameterizedTest
  @CsvSource({
    "-0.10, 0.00",
    "+0.00, 0.00",
    "+0.10, 0.00",
    "+0.20, 0.20",
    "+0.70, 0.60",
    "+0.99, 0.80",
    "+1.00, 1.00",
    "+2.00, 1.00"
  })
  void testSampleCdf(double x, double expected) {
    final ManualPdf subject = new ManualPdf(0.2, 0.2, 0.2, 0.2, 0.2);
    assertEquals(expected, subject.sampleCdf(x), EPSILON);
  }

  @ParameterizedTest
  @CsvSource({
    "-0.10, 0.00",
    "+0.00, 0.00",
    "+0.10, 0.10",
    "+0.20, 0.20",
    "+0.70, 0.70",
    "+0.99, 0.99",
    "+1.00, 1.00",
    "+2.00, 1.00"
  })
  void testSampleCdfWithLinearInterpolation(double x, double expected) {
    final ManualPdf subject = new ManualPdf(ManualPdf.LINEAR_INTERPOLATOR, 0.2, 0.2, 0.2, 0.2, 0.2);
    assertEquals(expected, subject.sampleCdf(x), EPSILON);
  }

  @ParameterizedTest
  @CsvSource({
    "-01.0, -10.0",
    "+00.0, -10.0",
    "+09.0, -10.0",
    "+10.0, -10.0",
    "+13.0, -05.0",
    "+19.0, +05.0",
    "+20.0, +10.0",
    "+21.0, -10.0"
  })
  void testPdfToFunction(double x, double expected) {
    final ManualPdf subject = new ManualPdf(0.1, 0.2, 0.3, 0.4);
    assertEquals(
        expected,
        subject.pdfToFunction(domainFrom(10).spanning(10), rangeFrom(-10).to(10)).apply(x),
        EPSILON);
  }
}
