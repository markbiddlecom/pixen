package com.brotherhoodgames.pixen.mod.tree;

import com.brotherhoodgames.pixen.mod.util.DoubleRange;
import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class GiantRedwoodGenerator {
  public static final int MAX_TREE_RADIUS = 60;
  public static final int MAX_TREE_HEIGHT = 200;
  public static final int MAX_BRANCH_ITERATIONS = 1000;

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
    TreeSpace tree = new TreeSpace(Math.abs(parameters.trunkDiameter.sample(random)) / 2.0);

    generateTrunk(parameters, random, tree);
    generateBranches(parameters, tree, random);

    // Copy the generated tree to the collaborator
    tree.streamCells()
        .filter(TreeSpace.Cell::isFilled)
        .forEach(
            cell ->
                collaborator.setBlock(
                    cell.getNonnull(),
                    cell.treeX + treeWorldX,
                    cell.y + treeWorldY,
                    cell.treeZ + treeWorldZ));
  }

  private static void generateTrunk(
      GiantRedwoodGenerationParameters parameters,
      @Nonnull RandomSource random,
      @Nonnull TreeSpace tree) {
    List<TrunkChord> treeChords = initializeRings(parameters, random);
    double id = Math.abs(parameters.heartwoodDiameter.sample(random)) / 2.0;
    double trunkRadius = tree.trunkBaseRadius;
    double setback = parameters.trunkSetback.sample(random);

    TreeSpace.Slice slice = tree.baseSlice();

    final TrunkChord maxTrunkChord =
        TrunkChord.builder().atOrigin().size(trunkRadius * 1.8, 0).unmoving().build();

    while (trunkRadius > 0.5 && slice.allocate().isPresent()) {
      fillTrunk(slice, treeChords, maxTrunkChord, trunkRadius);
      findBarkRing(slice);

      trunkRadius -= setback;
      setback += parameters.trunkSetbackAcceleration.sample(random);
      treeChords.forEach(TrunkChord::climb);

      slice = slice.above();
    }
    tree.markTrunkHeight();
  }

  private static void fillTrunk(
      @Nonnull TreeSpace.Slice slice,
      @Nonnull List<TrunkChord> treeChords,
      @Nonnull TrunkChord maxTrunkChord,
      double trunkRadius) {
    slice
        .streamCells()
        .forEach(
            cell -> {
              if (maxTrunkChord.distance(cell.treeX, cell.treeZ) <= 0) {
                double d =
                    treeChords.stream()
                        .mapToDouble(
                            c -> c.distance(cell.treeX / trunkRadius, cell.treeZ / trunkRadius))
                        .min()
                        .orElse(1);
                if (d <= 0) {
                  cell.set(TreeBlock.WOOD);
                }
              }
            });
  }

  private static void findBarkRing(@Nonnull TreeSpace.Slice slice) {
    slice
        .streamCells()
        .forEach(
            cell -> {
              if (cell.isFilled() && cell.isTouchingInSlice(TreeBlock.AIR)) {
                cell.set(TreeBlock.BARK);
              }
            });
  }

  private static void generateBranches(
      @Nonnull GiantRedwoodGenerationParameters parameters,
      @Nonnull TreeSpace tree,
      @Nonnull RandomSource random) {
    int branchCount = (int) parameters.branchCount.sample(random);
    double branchSeparation = (Math.PI * 2) / (branchCount + 1);

    List<IterativeGenerator> branches =
        IntStream.range(0, branchCount)
            .mapToObj(
                branchIndex ->
                    initializeBranch(
                        parameters,
                        tree,
                        random,
                        branchIndex,
                        random.nextDouble() * Math.toRadians(45),
                        branchSeparation))
            .toList();

    int i = 0;
    while (!branches.isEmpty() && i++ < MAX_BRANCH_ITERATIONS) {
      branches =
          branches.stream().unordered().parallel().flatMap(b -> b.iterate(random, tree)).toList();
    }
  }

  private static @Nonnull IterativeGenerator initializeBranch(
      @Nonnull GiantRedwoodGenerationParameters parameters,
      @Nonnull TreeSpace tree,
      @Nonnull RandomSource random,
      int branchIndex,
      double initialBranchAngle,
      double branchSeparation) {
    TreeSpace.Slice slice =
        tree.slice(
            (int) (tree.getTrunkHeight() * parameters.branchHeightDistribution.sampleCdf(random)));

    // Branch length is primarily a function of tree size and the branch's y position in the trunk.
    // The length factor is scaled using the branch distribution function, so that branches are
    // longest where they're most likely to grow. Finally, the default output is scaled by sampling
    // the branch length function.
    double targetLength =
        parameters
                .branchHeightDistribution
                .pdfToFunction(
                    DoubleRange.fromOrigin().spanning(tree.getTrunkHeight()),
                    DoubleRange.from(0.5).to(1.0))
                .apply((double) slice.y)
            * parameters.branchLength.sample(random);

    // Find the starting position for the branch. Move out from the tree center along a set
    // direction until we hit an air block or until we've gone past the base radius.
    double branchXzPlaneAngle = initialBranchAngle + branchIndex * branchSeparation;
    Vec2 searchDirection =
        new Vec2((float) Math.sin(branchXzPlaneAngle), (float) Math.cos(branchXzPlaneAngle))
            // Scale down so that we have a better chance of finding the correct starting point
            .scale(0.8f);

    double maxSearchDistance = tree.trunkBaseRadius * 1.2;
    Vec2 currentLocation = new Vec2(0, 0);
    while (currentLocation.length() < maxSearchDistance && slice.cell(currentLocation).isFilled()) {
      currentLocation = currentLocation.add(searchDirection);
    }
    BlockPos startPos = new BlockPos(currentLocation.x, slice.y, currentLocation.y);

    // Pick the starting orientation using the cardinal direction closest (via subtraction) to the
    // selected growth direction, and then apply the vertical deflection.
    final Vec3 searchDirNormalized = new Vec3(searchDirection.x, 0, searchDirection.y).normalize();
    Vector3f deflectionAxis = searchDirNormalized.toVector3f().rotateY((float) -Math.PI / 2);
    Vec3 baseDirection =
        new Vec3(
            searchDirNormalized
                .toVector3f()
                .rotateAxis(
                    (float) parameters.branchYDeflectionRadians.sample(random),
                    deflectionAxis.x,
                    deflectionAxis.y,
                    deflectionAxis.z));
    Vec3 growthDirection =
        Stream.of(
                GrowthDirections.NORTH,
                GrowthDirections.EAST,
                GrowthDirections.SOUTH,
                GrowthDirections.WEST)
            .min(Comparator.comparing(dir -> searchDirNormalized.vectorTo(dir).lengthSqr()))
            .orElse(GrowthDirections.NORTH);

    return Branch.builder()
        .fromParameters(random, parameters)
        .targetLength(targetLength)
        .basePosition(new Vec3(0, slice.y, 0))
        .currentLocation(startPos)
        .baseDirection(baseDirection)
        .growthDirection(growthDirection)
        .turnSelectionFunction(GiantRedwoodGenerator::turnSelectionFunction)
        .build();
  }

  @Nonnull
  private static Vec3 turnSelectionFunction(
      @Nonnull RandomSource r,
      @Nonnull BlockPos curPosition,
      @Nonnull Vec3 curDirection,
      @Nonnull Vec3 turnBias) {
    // Choose whatever direction will move us closer to the turn bias.
    // TODO: make this a little more random
    Vec3 normalizedBias = turnBias.normalize();
    return Stream.of(
            GrowthDirections.NORTH,
            GrowthDirections.EAST,
            GrowthDirections.SOUTH,
            GrowthDirections.WEST,
            GrowthDirections.UP,
            GrowthDirections.DOWN)
        .min(Comparator.comparing(dir -> normalizedBias.vectorTo(dir).lengthSqr()))
        .orElse(GrowthDirections.NORTH);
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
    DEBUG_LOG_TURN,
    DEBUG_LOG_SPLIT
  }

  @FunctionalInterface
  public interface GenerationCollaborator {
    void setBlock(@Nonnull TreeBlock treeBlock, int x, int y, int z);
  }
}
