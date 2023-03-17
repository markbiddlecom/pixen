package com.brotherhoodgames.pixen.mod.tree;

import com.brotherhoodgames.pixen.mod.util.Randomness;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class GrowthDirections {
  /*package*/ static final Vec3 NORTH = Vec3.atLowerCornerOf(Direction.NORTH.getNormal());
  /*package*/ static final Vec3 WEST = Vec3.atLowerCornerOf(Direction.WEST.getNormal());
  /*package*/ static final Vec3 SOUTH = Vec3.atLowerCornerOf(Direction.SOUTH.getNormal());
  /*package*/ static final Vec3 EAST = Vec3.atLowerCornerOf(Direction.EAST.getNormal());
  /*package*/ static final Vec3 UP = Vec3.atLowerCornerOf(Direction.UP.getNormal());
  /*package*/ static final Vec3 DOWN = Vec3.atLowerCornerOf(Direction.DOWN.getNormal());

  /**
   * A list of the cardinal growth directions supported by the tree generation algorithms. These are
   * derived from the {@linkplain Direction Minecraft core directions}.
   */
  public static final ImmutableList<Vec3> GROWTH_DIRECTIONS =
      ImmutableList.of(NORTH, WEST, SOUTH, EAST, UP, DOWN);

  /**
   * The number of directions in the {@link #GROWTH_DIRECTIONS} list; in other words, an alias for
   * {@code GROWTH_DIRECTIONS.size()}. Offered as a potentially more performant alternative to
   * retrieving the size via method call.
   */
  public static final int GROWTH_DIRECTION_COUNT = GROWTH_DIRECTIONS.size();

  /**
   * Randomly picks and returns one of the {@linkplain #GROWTH_DIRECTIONS cardinal growth
   * directions}.
   */
  public static @Nonnull Vec3 pickRandomDirection(@Nonnull RandomSource randomSource) {
    return Optional.ofNullable(Randomness.oneOf(randomSource, GROWTH_DIRECTIONS)).orElseThrow();
  }

  /**
   * Returns a stream of the {@linkplain #GROWTH_DIRECTIONS cardinal growth directions} excluding
   * those that are closest to each of the given set of predicate directions. Each predicate is
   * considered independently; that is, if the same candidate is the closest to two separate
   * predicates, only that candidate will be excluded.
   *
   * @param directions a list of the directions to use as filtering predicates.
   * @return a (potentially empty) stream of the directions that were collectively far from the
   *     predicate directions.
   */
  public static @Nonnull Stream<Vec3> streamDirectionsExcludingNearest(
      @Nullable Vec3... directions) {
    if (directions == null || directions.length == 0) return GROWTH_DIRECTIONS.stream();

    final int VALUE_DIR_INDEX = 0;
    final int VALUE_DIST = 1;

    double[][] minIndices = new double[directions.length][2];
    for (int i = 0; i < GROWTH_DIRECTION_COUNT; i++) {
      Vec3 candidate = GROWTH_DIRECTIONS.get(i);
      for (int j = 0; j < directions.length; j++) {
        Vec3 predicate = directions[j];
        if (predicate == null) continue;
        double distSquared = predicate.distanceToSqr(candidate) + 1; // +1 to distinguish from zero
        if (minIndices[j][VALUE_DIST] < 1 || distSquared < minIndices[j][VALUE_DIST]) {
          minIndices[j][VALUE_DIR_INDEX] = i;
          minIndices[j][VALUE_DIST] = distSquared;
        }
      }
    }

    Set<Integer> excludedIndices =
        Arrays.stream(minIndices)
            .filter(predicate -> predicate[VALUE_DIST] > 0)
            .map(predicate -> (int) Math.round(predicate[VALUE_DIR_INDEX]))
            .collect(Collectors.toSet());

    return IntStream.range(0, GROWTH_DIRECTION_COUNT)
        .filter(i -> !excludedIndices.contains(i))
        .mapToObj(GROWTH_DIRECTIONS::get);
  }

  /** Determines if the given growth direction is (mostly) vertical. */
  public static boolean isVertical(@Nullable Vec3 growthDirection) {
    return growthDirection != null && growthDirection.multiply(0, 1, 0).length() >= 0.8;
  }

  private GrowthDirections() {}
}
