package com.brotherhoodgames.pixen.mod.tree;

import static com.brotherhoodgames.pixen.mod.util.stats.Pdf.domainFrom;
import static com.brotherhoodgames.pixen.mod.util.stats.Pdf.rangeFrom;

import com.brotherhoodgames.pixen.mod.util.Randomness;
import com.brotherhoodgames.pixen.mod.util.stats.Pdf;
import com.brotherhoodgames.pixen.mod.util.stats.RandomVariable;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Data;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Data
@Builder(builderClassName = "Builder")
/*package*/ class Branch implements IterativeGenerator {
  final @Nullable Branch parent;
  double currentLength;
  double currentSegmentLength;
  double targetSegmentLength;
  double currentThickness;
  @Nonnull Vec3 growthDirection;
  @Nonnull BlockPos currentLocation;

  final double targetLength;
  final double outwardBias;
  final double avoidBias;
  final double upwardBias;
  final double continueBias;
  final double splitProbabilityScalar;
  final double branchSplitMinimumFirstSegmentLength;
  final @Nonnull Vec3 baseDirection;
  final @Nonnull Vec3 basePosition;
  final @Nonnull Pdf splitProbabilityFunction;
  final @Nonnull RandomVariable segmentLengthFunction;
  final @Nonnull TurnSelectionFunction turnSelectionFunction;

  @Override
  public @Nonnull Stream<IterativeGenerator> iterate(
      @Nonnull RandomSource random,
      @Nonnull GiantRedwoodGenerationParameters parameters,
      @Nonnull TreeSpace tree) {
    tree.setIfEmpty(currentLocation, GiantRedwoodGenerator.TreeBlock.LOG);

    if (!advance(tree)) {
      // This branch is finished
      return LeafNode.initializeAndStreamLeafNodes(random, currentLocation, parameters);
    }

    Stream.Builder<IterativeGenerator> remainingGenerators =
        Stream.<IterativeGenerator>builder().add(this);
    double splitP =
        splitProbabilityScalar
            * splitProbabilityFunction
                .pdfToFunction(domainFrom(0).to(targetLength), rangeFrom(0).to(1))
                .apply(currentLength);

    if (random.nextDouble() <= splitP) {
      tree.set(currentLocation, GiantRedwoodGenerator.TreeBlock.DEBUG_LOG_SPLIT);
      remainingGenerators.add(initializeSplit(random));
      if (random.nextDouble() < parameters.leafClusterAtSplitProbability.sample(random))
        LeafNode.initializeLeafNodes(random, currentLocation, parameters, remainingGenerators);
    } else if (currentSegmentLength >= targetSegmentLength) {
      tree.set(currentLocation, GiantRedwoodGenerator.TreeBlock.DEBUG_LOG_TURN);

      Vec3 turnBias = calculateTurnBias(random, tree);
      growthDirection =
          turnSelectionFunction.turn(random, currentLocation, growthDirection, turnBias);

      currentSegmentLength = 0;
      targetSegmentLength = sampleTargetLength(random, growthDirection);
    }

    return remainingGenerators.build();
  }

  private double sampleTargetLength(
      @Nonnull RandomSource random, @Nonnull Vec3 forGrowthDirection) {
    double newTargetLength = segmentLengthFunction.sample(random);
    if (newTargetLength > 0.9 && GrowthDirections.isVertical(forGrowthDirection)) return 0.9;
    else return newTargetLength;
  }

  private @Nonnull Branch initializeSplit(@NotNull RandomSource random) {
    // The new growth direction is picked at random from the cardinal directions, excluding the
    // branch's current growth direction and its opposite, the vector in the direction of the tree's
    // core, and the vector pointing back to branch's base position.
    Vec3 newGrowthDirection =
        Randomness.oneOf(
                random,
                growthDirection, // default to continuing along the same branch--should never happen
                GrowthDirections.streamDirectionsExcludingNearest(
                        currentLocation.getCenter().vectorTo(basePosition).normalize(),
                        growthDirection,
                        growthDirection.reverse(),
                        currentLocation.getCenter().vectorTo(basePosition))
                    .toList())
            .normalize();

    double newTargetSegmentLength =
        Math.max(
            branchSplitMinimumFirstSegmentLength, sampleTargetLength(random, newGrowthDirection));

    // The new base direction is calculated as a normalized vector from the parent branch's center
    // pointing to the first turn position for the new branch. (I.e., the turn position is projected
    // outward to the target segment length and added to the current position's vector.)
    Vec3 newBaseDirection =
        basePosition
            .vectorTo(currentLocation.getCenter())
            .add(newGrowthDirection.scale(newTargetSegmentLength))
            .normalize();

    return Branch.builder()
        .splitFrom(this)
        .baseDirection(newBaseDirection)
        .currentLocation(new BlockPos(currentLocation.getCenter().add(newGrowthDirection)))
        .currentSegmentLength(0)
        .targetSegmentLength(newTargetSegmentLength)
        .currentThickness(1)
        .growthDirection(newGrowthDirection)
        .build();
  }

  @NotNull
  private Vec3 calculateTurnBias(@Nonnull RandomSource random, @Nonnull TreeSpace tree) {
    // The outward bias is a vector that will return the branch to the position it *should* be if
    // it had travelled straight outwards along the base direction vector. The XZ direction is
    // weighted independently of the Y direction.
    Vec3 outwardBias =
        currentLocation
            .getCenter()
            .vectorTo(baseDirection.normalize().scale(currentLength).add(basePosition))
            .normalize()
            .multiply(this.outwardBias, this.upwardBias, this.outwardBias);

    // The continuation bias is the propensity of the branch to continue growing in its current
    // direction even though it's being asked to turn. Put differently, this is the propensity of
    // the branch to override its turn directive.
    Vec3 continueBias = growthDirection.normalize().scale(this.continueBias);

    // The avoidance bias is a vector that will move the branch's growth the farthest away from all
    // neighboring blocks. It's calculated by producing a "push" vector for each block nearby the
    // current location whose length is inversely related to the square of its distance, and then
    // summing all push vectors.
    final int MAX_AVOID_DISTANCE = 8;
    final double MAX_DISTANCE_SQR = MAX_AVOID_DISTANCE * MAX_AVOID_DISTANCE;
    final Vec3 center = currentLocation.getCenter();
    Vec3 avoidBias =
        tree.cell(currentLocation)
            .streamCellEnvelope(MAX_AVOID_DISTANCE * 2)
            .filter(
                // Only consider cells with data, are within the distance radius, and that are
                // farther away from the trunk than the current location. We're ignoring blocks that
                // are closer to the trunk because the outward bias already effectively considers
                // that and including them in this bias would skew more strongly towards straight
                // outward growth.
                cell ->
                    cell.isFilled()
                        && cell.distanceToSqr(basePosition)
                            > currentLocation.getCenter().distanceToSqr(basePosition)
                        && cell.distanceToSqr(currentLocation) < MAX_DISTANCE_SQR)
            .map(
                cell -> {
                  // We're using a random contained point to increase the chances of an upward or
                  // downward push when all entities are on the same plane as the current location.
                  Vec3 point = cell.randomContainedTreeCoordinate(random);
                  Vec3 force = point.vectorTo(center);
                  double distanceSqr = force.lengthSqr();
                  return force.normalize().scale(MAX_DISTANCE_SQR - distanceSqr);
                })
            .reduce(Vec3.ZERO, Vec3::add)
            .normalize()
            .scale(this.avoidBias);

    // We leave the overall turn bias de-normalized to accommodate turn algorithms that take the
    // strength of the bias into account.
    return outwardBias.add(continueBias).add(avoidBias);
  }

  private boolean advance(@NotNull TreeSpace tree) {
    Vec3 position =
        new Vec3(currentLocation.getX(), currentLocation.getY(), currentLocation.getZ());
    BlockPos newPos = new BlockPos(currentLocation);
    int i = 0;
    for (Vec3 growth = growthDirection;
        newPos.equals(currentLocation) && i < 10;
        growth = growth.multiply(0.5f, 0.5f, 0.5f), i++) {
      position = position.add(growthDirection);
      newPos = new BlockPos(position);
    }

    // Make sure we're still within the bounds of the tree area
    if (!tree.areValidTreeCoordinates(newPos)) return false;

    // Add missing tree slices
    TreeSpace.Slice slice = tree.slice(newPos.getY()).allocate().orElse(null);
    if (slice == null) return false;

    int l = newPos.distManhattan(currentLocation);
    currentLength += l;
    currentSegmentLength += l;

    if (l > 1) {
      // we made a diagonal move; we need to connect it with an elbow
      // Always walk y first, but Mix up whether we walk x first or z first
      //      BranchWalker xWalk = BranchWalker.xWalk(tree, maxTreeRadius, currentLocation);
      //      BranchWalker zWalk = BranchWalker.zWalk(tree, maxTreeRadius, currentLocation);
      //      List<BranchWalker> walkers =
      //          Lists.newArrayList(
      //              BranchWalker.yWalk(tree, maxTreeRadius, currentLocation),
      //              currentLocation.getX() % 2 == 0 ? xWalk : zWalk,
      //              currentLocation.getX() % 2 == 0 ? zWalk : xWalk);
      //      for (BranchWalker walker : walkers) {
      //        walker.walk(currentLocation, newPos);
      //      }
    }

    currentLocation = newPos;
    return currentLength < targetLength;
  }

  static class Builder {
    @Nonnull
    Builder fromParameters(
        @Nonnull RandomSource r, @Nonnull GiantRedwoodGenerationParameters parameters) {
      return this.targetLength(parameters.branchLength.sample(r))
          .targetSegmentLength(parameters.branchSegmentLength.sample(r))
          .outwardBias(parameters.branchRadialBias.sample(r))
          .continueBias(parameters.branchStraightnessBias.sample(r))
          .avoidBias(parameters.branchSeparationBias.sample(r))
          .upwardBias(parameters.branchUpwardBias.sample(r))
          .splitProbabilityScalar(parameters.branchSplitProbabilityScalar.sample(r))
          .branchSplitMinimumFirstSegmentLength(
              parameters.branchSplitMinimumFirstSegmentLength.sample(r))
          .splitProbabilityFunction(parameters.branchSplitDistribution)
          .segmentLengthFunction(parameters.branchSegmentLength);
    }

    @Nonnull
    Builder splitFrom(@Nonnull Branch parent) {
      return parent(parent)
          .baseDirection(parent.baseDirection)
          .basePosition(parent.basePosition)
          .currentLength(parent.currentLength)
          .growthDirection(parent.growthDirection)
          .currentLocation(parent.currentLocation)
          .targetLength(parent.targetLength)
          .outwardBias(parent.outwardBias)
          .avoidBias(parent.avoidBias)
          .upwardBias(parent.upwardBias)
          .continueBias(parent.continueBias)
          .splitProbabilityFunction(parent.splitProbabilityFunction)
          .branchSplitMinimumFirstSegmentLength(branchSplitMinimumFirstSegmentLength)
          .segmentLengthFunction(parent.segmentLengthFunction)
          .turnSelectionFunction(parent.turnSelectionFunction);
    }
  }

  @FunctionalInterface
  interface TurnSelectionFunction {
    @Nonnull
    Vec3 turn(
        @Nonnull RandomSource randomSource,
        @Nonnull BlockPos currentLocation,
        @Nonnull Vec3 currentDirection,
        @Nonnull Vec3 turnBias);
  }
}
