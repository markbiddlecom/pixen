package com.brotherhoodgames.pixen.mod.tree;

import static com.brotherhoodgames.pixen.mod.util.stats.RandomVariables.confidenceInterval;
import static com.brotherhoodgames.pixen.mod.util.stats.RandomVariables.constant;
import static com.brotherhoodgames.pixen.mod.util.stats.RandomVariables.range;

import com.brotherhoodgames.pixen.mod.util.stats.Pdf;
import com.brotherhoodgames.pixen.mod.util.stats.Pdfs;
import com.brotherhoodgames.pixen.mod.util.stats.RandomVariable;
import java.io.Serializable;
import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderClassName = "Builder")
public final class GiantRedwoodGenerationParameters implements Serializable {
  public static final GiantRedwoodGenerationParameters DEFAULT_PARAMETERS =
      builder()
          .trunkDiameter(confidenceInterval(0.99, 6, 12))
          .trunkChords(confidenceInterval(0.8, 2, 4))
          .trunkChordScale(range(0.2, 0.6))
          .trunkChordRadii(range(0, 0.8))
          .trunkChordEccentricity(range(0.0, 0.2))
          .trunkChordGravity(confidenceInterval(0.8, 0.2, 0.3).clamp(0.05, 0.6))
          .trunkChordInitialVelocityMagnitude(confidenceInterval(0.9, 0, 0.1))
          .trunkChordInitialVelocityDeflection(range(-Math.PI / 8, Math.PI / 8))
          .trunkChordAngularVelocity(confidenceInterval(0.8, Math.toRadians(0), Math.toRadians(20)))
          .trunkChordAngularVelocityDampening(range(0.9, 1.0))
          .heartwoodDiameter(confidenceInterval(0.999999, 2, 4))
          .trunkSetback(constant(0.008))
          .trunkSetbackAcceleration(confidenceInterval(0.98, 0.0005, 0.003))
          .branchCount(range(12, 14))
          .branchLength(confidenceInterval(0.9, 13, 15))
          .branchStraightnessBias(constant(0.2))
          .branchSeparationBias(constant(3.0))
          .branchRadialBias(constant(1.0))
          .branchUpwardBias(constant(0.8))
          .branchSegmentLength(range(2, 4))
          .branchSplitProbabilityScalar(range(0.2, 0.4))
          .branchSplitMinimumFirstSegmentLength(constant(3))
          .branchYDeflectionRadians(
              confidenceInterval(0.99, Math.toRadians(-5.0), Math.toRadians(15)))
          .branchHeightDistribution(
              Pdfs.fromHistogram(
                  ""
                      + "                                                          *****      \n"
                      + "                                                        **     **    \n"
                      + "                                                       *         *   \n"
                      + "                                                     **          *   \n"
                      + "                                                  ***             *  \n"
                      + "                              *******************                 *  \n"))
          .branchSplitDistribution(
              Pdfs.fromHistogram(
                  ""
                      + "               ***********                                           \n"
                      + "              *           *                                          \n"
                      + "             *             *                                         \n"
                      + "            *               ************                             \n"
                      + "           *                            *******                      \n"
                      + "          *                                    ****                  \n"))
          .leafClusterAtSplitProbability(constant(0.3))
          .leafClusterDropOffProbability(constant(0.2))
          .leafClusterNodeCount(range(1, 4))
          .leafClusterRadius(confidenceInterval(0.9, 3, 4))
          .build();

  /*package*/ final @Nonnull RandomVariable trunkDiameter;
  /*package*/ final @Nonnull RandomVariable trunkChords;
  /*package*/ final @Nonnull RandomVariable trunkChordScale;
  /*package*/ final @Nonnull RandomVariable trunkChordRadii;
  /*package*/ final @Nonnull RandomVariable trunkChordEccentricity;
  /*package*/ final @Nonnull RandomVariable trunkChordGravity;
  /*package*/ final @Nonnull RandomVariable trunkChordInitialVelocityMagnitude;
  /*package*/ final @Nonnull RandomVariable trunkChordInitialVelocityDeflection;
  /*package*/ final @Nonnull RandomVariable trunkChordAngularVelocity;
  /*package*/ final @Nonnull RandomVariable trunkChordAngularVelocityDampening;
  /*package*/ final @Nonnull RandomVariable heartwoodDiameter;
  /*package*/ final @Nonnull RandomVariable trunkSetback;
  /*package*/ final @Nonnull RandomVariable trunkSetbackAcceleration;

  /*package*/ final @Nonnull RandomVariable branchCount;
  /*package*/ final @Nonnull RandomVariable branchStraightnessBias;
  /*package*/ final @Nonnull RandomVariable branchSeparationBias;
  /*package*/ final @Nonnull RandomVariable branchRadialBias;
  /*package*/ final @Nonnull RandomVariable branchUpwardBias;
  /*package*/ final @Nonnull RandomVariable branchLength;
  /*package*/ final @Nonnull RandomVariable branchSegmentLength;
  /*package*/ final @Nonnull RandomVariable branchSplitProbabilityScalar;
  /*package*/ final @Nonnull RandomVariable branchSplitMinimumFirstSegmentLength;
  /*package*/ final @Nonnull RandomVariable branchYDeflectionRadians;
  /*package*/ final @Nonnull Pdf branchHeightDistribution;
  /*package*/ final @Nonnull Pdf branchSplitDistribution;

  /*package*/ final @Nonnull RandomVariable leafClusterAtSplitProbability;
  /*package*/ final @Nonnull RandomVariable leafClusterDropOffProbability;
  /*package*/ final @Nonnull RandomVariable leafClusterNodeCount;
  /*package*/ final @Nonnull RandomVariable leafClusterRadius;
}
