package com.brotherhoodgames.pixen.mod.tree;

import static com.brotherhoodgames.pixen.mod.tree.GiantRedwoodGenerator.TreeBlock.DEAD_LEAF_SPACE;
import static com.brotherhoodgames.pixen.mod.tree.GiantRedwoodGenerator.TreeBlock.LEAVES;

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import lombok.Builder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

@Builder(builderClassName = "Builder")
/*package*/ class LeafNode implements IterativeGenerator {
  int currentEnvelopeRadius;
  int remainingSubNodes;

  final BlockPos nodeLocation;
  final double radius;
  final double dropoffRate;

  @Override
  public @Nonnull Stream<IterativeGenerator> iterate(
      @Nonnull RandomSource random,
      @Nonnull GiantRedwoodGenerationParameters parameters,
      @Nonnull TreeSpace tree) {
    Stream.Builder<IterativeGenerator> remainingGenerators = Stream.builder();

    int xMin = nodeLocation.getX() - currentEnvelopeRadius;
    int xMax = nodeLocation.getX() + currentEnvelopeRadius;
    int yMin = nodeLocation.getY() - currentEnvelopeRadius;
    int yMax = nodeLocation.getY() + currentEnvelopeRadius;
    int zMin = nodeLocation.getZ() - currentEnvelopeRadius;
    int zMax = nodeLocation.getZ() + currentEnvelopeRadius;

    for (int y = yMin; y < yMax; y++) {
      if (y == yMin || y == yMax - 1) {
        for (int x = xMin; x < xMax; x++)
          for (int z = zMin; z < zMax; z++) {
            setBlock(random, tree, x, y, z);
          }
      } else {
        for (int x = xMin; x < xMax; x++) {
          setBlock(random, tree, x, y, zMin);
          setBlock(random, tree, x, y, zMax - 1);
        }
        for (int z = zMin + 1; z < zMax - 1; z++) {
          setBlock(random, tree, xMin, y, z);
          setBlock(random, tree, xMax - 1, y, z);
        }
      }
    }

    currentEnvelopeRadius++;
    if (currentEnvelopeRadius <= Math.ceil(radius)) remainingGenerators.add(this);

    return remainingGenerators.build();
  }

  private void setBlock(
      @NotNull RandomSource random, @NotNull TreeSpace tree, int x, int y, int z) {
    TreeSpace.Cell cell = tree.cell(x, y, z);
    if (cell.distanceToSqr(nodeLocation) <= radius * radius
        && cell.isEmpty()
        && cell.isTouching(b -> !b.isEmpty())
        && cell.isSurroundedBy(b -> b != DEAD_LEAF_SPACE)) {
      GiantRedwoodGenerator.TreeBlock block;
      if (random.nextDouble() < dropoffRate) {
        block = DEAD_LEAF_SPACE;
      } else {
        block = LEAVES;
      }
      cell.setIfEmpty(block);
    }
  }

  static @Nonnull Stream<IterativeGenerator> initializeAndStreamLeafNodes(
      @Nonnull RandomSource random,
      @Nonnull BlockPos location,
      @Nonnull GiantRedwoodGenerationParameters parameters) {
    Stream.Builder<IterativeGenerator> r = Stream.builder();
    initializeLeafNodes(random, location, parameters, r);
    return r.build();
  }

  static void initializeLeafNodes(
      @Nonnull RandomSource random,
      @Nonnull BlockPos location,
      @Nonnull GiantRedwoodGenerationParameters parameters,
      @Nonnull Stream.Builder<IterativeGenerator> sink) {
    sink.add(
        LeafNode.builder()
            .nodeLocation(location)
            .radius(parameters.leafClusterRadius.sample(random))
            .currentEnvelopeRadius(2)
            .dropoffRate(parameters.leafClusterDropOffProbability.sample(random))
            .build());
  }
}
