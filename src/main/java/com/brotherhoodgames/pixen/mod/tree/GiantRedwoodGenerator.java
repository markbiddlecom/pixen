package com.brotherhoodgames.pixen.mod.tree;

import static com.brotherhoodgames.pixen.mod.tree.Branch.isVertical;

import com.brotherhoodgames.pixen.mod.util.Randomness;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class GiantRedwoodGenerator {
  public static final int MAX_TREE_RADIUS = 40;
  public static final int MAX_TREE_HEIGHT = 200;
  public static final int MAX_BRANCH_ITERATIONS = 1000;

  private static final Vec3 NORTH = new Vec3(0, 0, 1);
  private static final Vec3 WEST = new Vec3(1, 0, 0);
  private static final Vec3 SOUTH = new Vec3(0, 0, -1);
  private static final Vec3 EAST = new Vec3(-1, 0, 0);
  private static final Vec3 UP = new Vec3(0, 1, 0);
  private static final Vec3 DOWN = new Vec3(0, -1, 0);

  private final GiantRedwoodGenerationParameters parameters;

  public GiantRedwoodGenerator(@Nonnull GiantRedwoodGenerationParameters parameters) {
    this.parameters = parameters;
  }

  public GiantRedwoodGenerator() {
    this(GiantRedwoodGenerationParameters.DEFAULT_PARAMETERS);
  }

  public void generate(
      int treeWorldX,
      int treeWorldY,
      int treeWorldZ,
      @Nonnull RandomSource random,
      @Nonnull GenerationCollaborator collaborator) {
    double trunkRadius = Math.abs(parameters.trunkDiameter.sample(random)) / 2.0;
    int maxTreeRadius = (int) Math.min(MAX_TREE_RADIUS, Math.ceil(trunkRadius * 3));
    int maxSliceIndex = maxTreeRadius * 2 + 1;
    List<TreeBlock[][]> slices = Lists.newArrayList();

    generateTrunk(parameters, random, trunkRadius, maxTreeRadius, maxSliceIndex, slices);
    generateBranches(parameters, slices, maxTreeRadius, maxSliceIndex, random);

    // Copy the generated tree to the collaborator
    for (int sy = 0; sy < slices.size(); sy++) {
      TreeBlock[][] slice = slices.get(sy);
      for (int sx = 0; sx < maxSliceIndex; sx++) {
        for (int sz = 0; sz < maxSliceIndex; sz++) {
          TreeBlock b = slice[sx][sz];
          if (b != null) {
            collaborator.setBlock(
                b,
                -maxTreeRadius + sx + treeWorldX,
                treeWorldY + sy,
                -maxTreeRadius + sz + treeWorldZ);
          }
        }
      }
    }
  }

  private static void generateTrunk(
      GiantRedwoodGenerationParameters parameters,
      @NotNull RandomSource random,
      double trunkRadius,
      int maxTreeRadius,
      int maxSliceIndex,
      List<TreeBlock[][]> slices) {
    List<TrunkChord> treeChords = initializeRings(parameters, random);
    double id = Math.abs(parameters.heartwoodDiameter.sample(random)) / 2.0;
    double setback = parameters.trunkSetback.sample(random);

    TreeBlock[][] slice = new TreeBlock[maxSliceIndex][maxSliceIndex];

    final TrunkChord maxTrunkChord =
        TrunkChord.builder().atOrigin().size(trunkRadius * 1.8, 0).unmoving().build();

    while (trunkRadius > 0.5) {
      findTrunk(trunkRadius, maxTreeRadius, maxSliceIndex, treeChords, slice, maxTrunkChord);
      findBarkRing(maxSliceIndex, slice);

      slices.add(Arrays.stream(slice).map(TreeBlock[]::clone).toArray(TreeBlock[][]::new));
      if (slices.size() >= MAX_TREE_HEIGHT) break;

      trunkRadius -= setback;
      setback += parameters.trunkSetbackAcceleration.sample(random);
      treeChords.forEach(TrunkChord::climb);
    }
  }

  private static void findTrunk(
      double trunkRadius,
      int maxTreeRadius,
      int maxSliceIndex,
      List<TrunkChord> treeChords,
      TreeBlock[][] slice,
      TrunkChord maxTrunkChord) {
    for (int sx = 0; sx < maxSliceIndex; sx++) {
      for (int sz = 0; sz < maxSliceIndex; sz++) {
        if (maxTrunkChord.distance(-maxTreeRadius + sx, -maxTreeRadius + sz) <= 0) {
          double nx = (-maxTreeRadius + sx) / trunkRadius;
          double nz = (-maxTreeRadius + sz) / trunkRadius;
          double d = treeChords.stream().mapToDouble(c -> c.distance(nx, nz)).min().orElse(1);
          slice[sx][sz] = d <= 0 ? TreeBlock.WOOD : null;
        }
      }
    }
  }

  private static void findBarkRing(int maxSliceIndex, TreeBlock[][] slice) {
    for (int sx = 0; sx < maxSliceIndex; sx++) {
      for (int sz = 0; sz < maxSliceIndex; sz++) {
        if (slice[sx][sz] == TreeBlock.WOOD
            && (sx <= 0
                || slice[sx - 1][sz] == null
                || sz <= 0
                || slice[sx][sz - 1] == null
                || sx >= maxSliceIndex - 1
                || slice[sx + 1][sz] == null
                || sz >= maxSliceIndex - 1
                || slice[sx][sz + 1] == null))
          // If this block is touching air, it's bark
          slice[sx][sz] = TreeBlock.BARK;
      }
    }
  }

  private static void generateBranches(
      GiantRedwoodGenerationParameters parameters,
      List<TreeBlock[][]> slices,
      int maxTreeRadius,
      int maxSliceIndex,
      RandomSource random) {
    List<Branch> branches =
        IntStream.range(0, (int) parameters.branchCount.sample(random))
            .mapToObj(
                _ignored ->
                    Branch.builder()
                        .fromParameters(random, parameters)
                        .currentLocation(
                            new BlockPos(
                                0,
                                slices.size() * parameters.branchHeightDistribution.sample(random),
                                0))
                        .growthDirection(Randomness.oneOf(random, WEST, NORTH, EAST, SOUTH))
                        .turnSelectionFunction(GiantRedwoodGenerator::turnSelectionFunction)
                        .build())
            .toList();
    int i = 0;
    while (!branches.isEmpty() && i++ < MAX_BRANCH_ITERATIONS) {
      branches =
          branches.stream()
              .flatMap(b -> b.crawl(random, maxTreeRadius, maxSliceIndex, slices).stream())
              .toList();
    }
  }

  @Nonnull
  private static Vec3 turnSelectionFunction(
      @Nonnull RandomSource r,
      @Nonnull BlockPos curPosition,
      @Nonnull Vec3 curDirection,
      @Nonnull Vec3 turnBias) {
    final float ninetyDegrees = (float) Math.PI / 2;
    Vec3 turnBasis =
        (curDirection.multiply(1, 0, 1).length() >= 1e-9
                ? curDirection
                : new Vec3(curPosition.getX(), 0, curPosition.getZ()))
            .multiply(1, 0, 1)
            .normalize();
    Vec3 turnLeft = turnBasis.yRot(-ninetyDegrees);
    Vec3 turnRight = turnBasis.yRot(ninetyDegrees);
    Vec3 turnUp = isVertical(curDirection) ? Vec3.ZERO : UP;
    Vec3 turnDown = isVertical(curDirection) ? Vec3.ZERO : DOWN;

    return Optional.ofNullable(
            Randomness.oneOf(
                r,
                Stream.of(turnLeft, turnRight, turnUp, turnDown)
                    .map(v -> v.add(v.scale(2 * turnBias.normalize().dot(v))).scale(10))
                    .flatMap(
                        turn ->
                            IntStream.range(0, (int) turn.length()).mapToObj(i -> turn.normalize()))
                    .toArray(Vec3[]::new)))
        .orElse(curDirection);
  }

  private static @Nonnull ImmutableList<TrunkChord> initializeRings(
      @Nonnull GiantRedwoodGenerationParameters parameters, @Nonnull RandomSource random) {
    int numChords = 1 + (int) Math.abs(parameters.trunkChords.sample(random));
    ImmutableList.Builder<TrunkChord> chords = ImmutableList.builderWithExpectedSize(numChords);

    // Initialize the central chord
    chords.add(
        TrunkChord.builder()
            .atOrigin()
            .size(1, parameters.trunkChordEccentricity.sample(random))
            .a(0)
            .unmoving()
            .build());

    // And now add the orbiting rings
    for (int c = 1; c < numChords; c++) {
      TrunkChord.Builder ring = TrunkChord.builder();

      // Origin
      double r = parameters.trunkChordRadii.sample(random);
      double t = random.nextDouble() * 2 * Math.PI;
      ring.x(r * Math.cos(t)).z(r * Math.sin(t)).a(random.nextDouble() * 2 * Math.PI);

      // Size
      ring.size(
          parameters.trunkChordScale.sample(random),
          parameters.trunkChordEccentricity.sample(random));

      // Movement
      ring.va(parameters.trunkChordAngularVelocity.sample(random))
          .ad(parameters.trunkChordAngularVelocityDampening.sample(random));
      double defl = t + parameters.trunkChordInitialVelocityDeflection.sample(random);
      double v = parameters.trunkChordInitialVelocityMagnitude.sample(random);
      ring.vx(v * Math.cos(defl))
          .vz(v * Math.sin(defl))
          .g(parameters.trunkChordGravity.sample(random));

      chords.add(ring.build());
    }

    return chords.build();
  }

  public enum TreeBlock {
    AIR,
    EXTERNAL,
    BARK,
    WOOD,
    HEARTWOOD,
    LOG,
    MOSSY_BARK,
    COMPOSTED_LOG,
    LEAVES,
    AMBER,
  }

  @FunctionalInterface
  public interface GenerationCollaborator {
    void setBlock(@Nonnull TreeBlock treeBlock, int x, int y, int z);
  }
}
