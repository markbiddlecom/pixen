package com.brotherhoodgames.pixen.mod.tree;

import static com.brotherhoodgames.pixen.mod.util.stats.RandomVariables.confidenceInterval;
import static com.brotherhoodgames.pixen.mod.util.stats.RandomVariables.constant;
import static com.brotherhoodgames.pixen.mod.util.stats.RandomVariables.range;

import com.brotherhoodgames.pixen.mod.util.stats.Pdf;
import com.brotherhoodgames.pixen.mod.util.stats.Pdfs;
import com.brotherhoodgames.pixen.mod.util.stats.RandomVariable;
import com.brotherhoodgames.pixen.mod.util.stats.RandomVariables;
import java.io.Serializable;
import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderClassName = "Builder")
public final class GiantRedwoodGenerationParameters implements Serializable {
  public static final GiantRedwoodGenerationParameters DEFAULT_PARAMETERS =
      builder()
          .trunkDiameter(RandomVariables.confidenceInterval(0.99, 6, 14))
          .trunkChords(RandomVariables.confidenceInterval(0.8, 2, 4))
          .trunkChordScale(RandomVariables.range(0.2, 0.6))
          .trunkChordRadii(RandomVariables.range(0, 0.8))
          .trunkChordEccentricity(RandomVariables.range(0.0, 0.2))
          .trunkChordGravity(RandomVariables.confidenceInterval(0.8, 0.1, 0.2).clamp(0.05, 0.6))
          .trunkChordInitialVelocityMagnitude(RandomVariables.confidenceInterval(0.9, 0, 0.1))
          .trunkChordInitialVelocityDeflection(RandomVariables.range(-Math.PI / 8, Math.PI / 8))
          .trunkChordAngularVelocity(
              RandomVariables.confidenceInterval(0.8, Math.toRadians(0), Math.toRadians(20)))
          .trunkChordAngularVelocityDampening(RandomVariables.range(0.9, 1.0))
          .heartwoodDiameter(RandomVariables.confidenceInterval(0.999999, 2, 4))
          .trunkSetback(constant(0.01))
          .trunkSetbackAcceleration(RandomVariables.confidenceInterval(0.98, 0.0005, 0.003))
          .branchCount(RandomVariables.range(6, 12))
          .branchLength(RandomVariables.confidenceInterval(0.8, 15, 20))
          .branchStraightnessBias(constant(0.1))
          .branchSeparationBias(constant(3.0))
          .branchRadialBias(constant(1.0))
          .branchUpwardBias(constant(0.8))
          .branchSegmentLength(RandomVariables.confidenceInterval(0.9, 1.5, 2.5))
          .branchSplitProbabilityScalar(RandomVariables.range(0.3, 0.5))
          .branchSplitMinimumFirstSegmentLength(constant(4))
          .branchYDeflectionRadians(
              RandomVariables.confidenceInterval(0.99, Math.toRadians(-5.0), Math.toRadians(15)))
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
          .leafClusterDropOffProbability(constant(0.1))
          .leafClusterNodeCount(range(1, 4))
          .leafClusterRadius(confidenceInterval(0.8, 4, 6))
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
