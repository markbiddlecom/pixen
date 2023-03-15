package com.brotherhoodgames.pixen.mod.tree;

import com.brotherhoodgames.pixen.mod.util.stats.Pdf;
import com.brotherhoodgames.pixen.mod.util.stats.RandomVariable;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
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
/*package*/ class Branch {
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
  final @Nonnull Vec3 baseDirection;
  final @Nonnull Pdf splitProbabilityFunction;
  final @Nonnull RandomVariable segmentLengthFunction;
  final @Nonnull TurnSelectionFunction turnSelectionFunction;

  @Nonnull
  List<Branch> crawl(@Nonnull RandomSource random, @Nonnull TreeSpace tree) {
    tree.setIfEmpty(currentLocation, GiantRedwoodGenerator.TreeBlock.LOG);

    if (!advance(tree)) {
      // TODO: Apply leaves
      // This branch is finished
      return Collections.emptyList();
    }

    List<Branch> remainingBranches = Lists.newArrayList(this);
    double splitP =
        //        splitProbabilityScalar * splitProbabilityFunction.sample(currentLength /
        // targetLength);
        0;
    if (random.nextDouble() <= splitP) {
      tree.set(currentLocation, GiantRedwoodGenerator.TreeBlock.DEBUG_LOG_SPLIT);
      remainingBranches.add(
          Branch.builder()
              .splitFrom(this)
              .currentSegmentLength(0)
              .targetSegmentLength(-1)
              .currentThickness(1)
              .growthDirection(
                  growthDirection
                      .cross(new Vec3(currentLocation.getX(), 0, currentLocation.getZ()))
                      .normalize())
              .build());
    } else if (currentSegmentLength >= targetSegmentLength) {
      tree.set(currentLocation, GiantRedwoodGenerator.TreeBlock.DEBUG_LOG_TURN);
      growthDirection =
          turnSelectionFunction.turn(random, currentLocation, growthDirection, calculateTurnBias());

      currentSegmentLength = 0;
      targetSegmentLength = segmentLengthFunction.sample(random);
      if (targetSegmentLength > 3 && isVertical(growthDirection)) targetSegmentLength = 3;
    }

    return remainingBranches;
  }

  @NotNull
  private Vec3 calculateTurnBias() {
    // The outward bias is a vector that will return the branch to the position it *should* be if
    // it had travelled straight outwards along the base direction vector. The XZ direction is
    // weighted independently of the Y direction.
    Vec3 outwardBias =
        currentLocation
            .getCenter()
            .vectorTo(baseDirection.normalize().scale(currentLength))
            .normalize()
            .multiply(this.outwardBias, this.upwardBias, this.outwardBias);

    // The continuation bias is the propensity of the branch to continue growing in its current
    // direction even though it's being asked to turn. Put differently, this is the propensity of
    // the branch to override its turn directive.
    Vec3 continueBias = growthDirection.normalize().scale(this.continueBias);

    // The avoidance bias is a vector that will move the branch's growth farthest away from all
    // neighboring blocks.
    // TODO: calculate avoid bias
    Vec3 avoidBias = Vec3.ZERO;

    // We leave the overall turn bias unscaled to accommodate turn algorithms that take the
    // strength of the bias into account.
    Vec3 turnBias = outwardBias.add(continueBias).add(avoidBias);
    return turnBias;
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

  /*package*/ static boolean isVertical(@Nonnull Vec3 growthDirection) {
    return growthDirection.multiply(0, 1, 0).length() >= 0.8;
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
          .splitProbabilityFunction(parameters.branchSplitDistribution)
          .segmentLengthFunction(parameters.branchSegmentLength);
    }

    @Nonnull
    Builder splitFrom(@Nonnull Branch parent) {
      return parent(parent)
          .currentLength(parent.currentLength)
          .growthDirection(parent.growthDirection)
          .currentLocation(parent.currentLocation)
          .targetLength(parent.targetLength)
          .outwardBias(parent.outwardBias)
          .avoidBias(parent.avoidBias)
          .upwardBias(parent.upwardBias)
          .continueBias(parent.continueBias)
          .splitProbabilityFunction(parent.splitProbabilityFunction)
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
