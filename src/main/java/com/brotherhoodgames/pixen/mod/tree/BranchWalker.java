package com.brotherhoodgames.pixen.mod.tree;

import com.brotherhoodgames.pixen.mod.util.NonnullFunction;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import net.minecraft.core.BlockPos;

@AllArgsConstructor
/*package*/ class BranchWalker {
  private final @Nonnull NonnullFunction<BlockPos, Integer> dimensionAccessor;
  private final @Nonnull Consumer<Integer> stepApplicator;

  static @Nonnull BranchWalker xWalk(
      final @Nonnull List<GiantRedwoodGenerator.TreeBlock[][]> tree,
      final int maxTreeRadius,
      final @Nonnull BlockPos anchorLocation) {
    return createWalk(
        tree,
        maxTreeRadius,
        BlockPos::getX,
        d -> d,
        _ignored -> anchorLocation.getY(),
        _ignored -> anchorLocation.getZ());
  }

  static @Nonnull BranchWalker yWalk(
      final @Nonnull List<GiantRedwoodGenerator.TreeBlock[][]> tree,
      final int maxTreeRadius,
      final @Nonnull BlockPos anchorLocation) {
    return createWalk(
        tree,
        maxTreeRadius,
        BlockPos::getY,
        _ignored -> anchorLocation.getX(),
        d -> d,
        _ignored -> anchorLocation.getZ());
  }

  static @Nonnull BranchWalker zWalk(
      final @Nonnull List<GiantRedwoodGenerator.TreeBlock[][]> tree,
      final int maxTreeRadius,
      final @Nonnull BlockPos anchorLocation) {
    return createWalk(
        tree,
        maxTreeRadius,
        BlockPos::getZ,
        _ignored -> anchorLocation.getX(),
        _ignored -> anchorLocation.getY(),
        d -> d);
  }

  static @Nonnull BranchWalker createWalk(
      final @Nonnull List<GiantRedwoodGenerator.TreeBlock[][]> tree,
      final int maxTreeRadius,
      final @Nonnull NonnullFunction<BlockPos, Integer> dimensionAccessor,
      final @Nonnull NonnullFunction<Integer, Integer> xIndexer,
      final @Nonnull NonnullFunction<Integer, Integer> yIndexer,
      final @Nonnull NonnullFunction<Integer, Integer> zIndexer) {
    return new BranchWalker(
        dimensionAccessor,
        d -> {
          int y = yIndexer.apply(d);
          int x = maxTreeRadius + xIndexer.apply(d);
          int z = maxTreeRadius + zIndexer.apply(d);

          if (y >= 0
              && y < tree.size()
              && x >= 0
              && x < maxTreeRadius * 2
              && z >= 0
              && z < maxTreeRadius * 2)
            tree.get(yIndexer.apply(d))[maxTreeRadius + xIndexer.apply(d)][
                    maxTreeRadius + zIndexer.apply(d)] =
                GiantRedwoodGenerator.TreeBlock.LOG;
        });
  }

  /*package*/ void walk(@Nonnull BlockPos start, @Nonnull BlockPos end) {
    final int startDim = dimensionAccessor.apply(start);
    final int endDim = dimensionAccessor.apply(end);
    if (startDim != endDim) {
      int step = endDim > startDim ? 1 : -1;
      IntPredicate endCondition = step > 0 ? (newD -> newD <= endDim) : (newD -> newD >= endDim);
      int d = startDim;
      do {
        d += step;
        stepApplicator.accept(d);
      } while (endCondition.test(d));
    }
  }
}
