package com.brotherhoodgames.pixen.mod.tree;

import static com.brotherhoodgames.pixen.mod.tree.GiantRedwoodGenerator.MAX_TREE_HEIGHT;
import static com.brotherhoodgames.pixen.mod.tree.GiantRedwoodGenerator.TreeBlock.AIR;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * A data container that describes the structure of a giant Redwood while it is being generated.
 * Defines utility methods for navigating the space during the generation process.
 */
@Getter
@ToString
public class TreeSpace {
  /*package*/ final double trunkBaseRadius;
  /*package*/ final int maxTreeRadius;
  /*package*/ final int maxSliceIndex;

  private final ImmutableList<Object> locks =
      IntStream.range(0, 10).mapToObj(i -> new Object()).collect(ImmutableList.toImmutableList());

  private final List<GiantRedwoodGenerator.TreeBlock[][]> slices;
  private int trunkHeight;

  /*package*/ TreeSpace(double trunkBaseRadius) {
    this.trunkBaseRadius = trunkBaseRadius;
    this.maxTreeRadius =
        (int) Math.min(GiantRedwoodGenerator.MAX_TREE_RADIUS, Math.ceil(trunkBaseRadius * 6));
    this.maxSliceIndex = maxTreeRadius * 2 + 1;
    this.slices = Lists.newArrayList();
  }

  /**
   * @return {@code true} IFF the given block position describes a point contained within this tree
   *     space. The block coordinate is assumed to be defined in the tree's coordinate system, with
   *     the origin at the center of the tree's minimum XZ plane.
   */
  public boolean areValidTreeCoordinates(@Nullable BlockPos treeCoordinates) {
    return treeCoordinates != null
        && areValidTreeCoordinates(
            treeCoordinates.getX(), treeCoordinates.getY(), treeCoordinates.getZ());
  }

  /**
   * @return {@code true} IFF the given coordinates describe a point contained within this tree
   *     space. The coordinates are assumed to be defined in the tree's coordinate system, with the
   *     origin at the center of the tree's minimum XZ plane.
   */
  public boolean areValidTreeCoordinates(int treeX, int treeY, int treeZ) {
    return areValidSliceIndices(
        treeCoordinateToUnsafeSliceIndex(treeX), treeY, treeCoordinateToUnsafeSliceIndex(treeZ));
  }

  /**
   * @return a (potentially invalid) tree cell representing the space at the given tree coordinates.
   */
  public @Nonnull Cell cell(@Nonnull BlockPos treeCoordinates) {
    return cell(treeCoordinates.getX(), treeCoordinates.getY(), treeCoordinates.getZ());
  }

  /**
   * @return a (potentially invalid) tree cell representing the space at the given tree coordinates.
   */
  public @Nonnull Cell cell(int treeX, int treeY, int treeZ) {
    return new Cell(
        this,
        treeCoordinateToUnsafeSliceIndex(treeX),
        treeY,
        treeCoordinateToUnsafeSliceIndex(treeZ));
  }

  /**
   * @return the stored tree block at the given coordinates within this tree space, or {@code null}
   *     if the coordinates are {@linkplain #areValidTreeCoordinates(int, int, int) not valid} or if
   *     no specific block was calculated.
   */
  public @Nullable GiantRedwoodGenerator.TreeBlock get(int treeX, int treeY, int treeZ) {
    return getFromSliceCoords(
        treeCoordinateToUnsafeSliceIndex(treeX), treeY, treeCoordinateToUnsafeSliceIndex(treeZ));
  }

  /**
   * Updates the stored block at the given coordinates within this tree space.
   *
   * @return the block previously stored at the coordinates, or {@code null} when the coordinates
   *     are {@linkplain #areValidTreeCoordinates(int, int, int) not valid} or when there was no
   *     previously stored block.
   */
  public @Nullable GiantRedwoodGenerator.TreeBlock set(
      @Nullable BlockPos treeCoordinates, @Nullable GiantRedwoodGenerator.TreeBlock block) {
    if (treeCoordinates == null) return null;
    else return set(treeCoordinates.getX(), treeCoordinates.getY(), treeCoordinates.getZ(), block);
  }

  /**
   * Updates the stored block at the given coordinates within this tree space.
   *
   * @return the block previously stored at the coordinates, or {@code null} when the coordinates
   *     are {@linkplain #areValidTreeCoordinates(int, int, int) not valid} or when there was no
   *     previously stored block.
   */
  public @Nullable GiantRedwoodGenerator.TreeBlock setIfEmpty(
      @Nullable BlockPos treeCoordinates, @Nullable GiantRedwoodGenerator.TreeBlock block) {
    if (treeCoordinates == null) return null;
    else
      return setIfEmpty(
          treeCoordinates.getX(), treeCoordinates.getY(), treeCoordinates.getZ(), block);
  }

  /**
   * Updates the stored block at the given coordinates within this tree space.
   *
   * @return the block previously stored at the coordinates, or {@code null} when the coordinates
   *     are {@linkplain #areValidTreeCoordinates(int, int, int) not valid} or when there was no
   *     previously stored block.
   */
  public @Nullable GiantRedwoodGenerator.TreeBlock set(
      int treeX, int treeY, int treeZ, @Nullable GiantRedwoodGenerator.TreeBlock block) {
    return setFromSliceCoords(
        treeCoordinateToUnsafeSliceIndex(treeX),
        treeY,
        treeCoordinateToUnsafeSliceIndex(treeZ),
        block);
  }

  /**
   * Updates the stored block at the given coordinates within this tree space.
   *
   * @return the block previously stored at the coordinates, or {@code null} when the coordinates
   *     are {@linkplain #areValidTreeCoordinates(int, int, int) not valid} or when there was no
   *     previously stored block.
   */
  public @Nullable GiantRedwoodGenerator.TreeBlock setIfEmpty(
      int treeX, int treeY, int treeZ, @Nullable GiantRedwoodGenerator.TreeBlock block) {
    return cell(treeX, treeY, treeZ).setIfEmpty(block);
  }

  /*package*/ double sliceIndexToTreeCoordinate(double sliceIndex) {
    return -maxTreeRadius + sliceIndex;
  }

  /*package*/ int sliceIndexToTreeCoordinate(int sliceIndex) {
    return (int) sliceIndexToTreeCoordinate((double) sliceIndex);
  }

  /*package*/ double treeCoordinateToUnsafeSliceIndex(double treeCoordinate) {
    return treeCoordinate + maxTreeRadius;
  }

  /*package*/ int treeCoordinateToUnsafeSliceIndex(int treeCoordinate) {
    return (int) treeCoordinateToUnsafeSliceIndex((double) treeCoordinate);
  }

  /*package*/ @Nonnull
  Optional<Integer> treeCoordinateToSliceIndex(double treeCoordinate) {
    int c = treeCoordinateToUnsafeSliceIndex((int) treeCoordinate);
    if (c < 0 || c >= maxSliceIndex) {
      return Optional.empty();
    } else {
      return Optional.of(c);
    }
  }

  /*package*/ boolean areValidSliceIndices(int sliceXIndex, int treeY, int sliceZIndex) {
    return sliceXIndex >= 0
        && sliceXIndex < maxSliceIndex
        && sliceZIndex >= 0
        && sliceZIndex < maxSliceIndex
        && treeY >= 0
        && treeY < MAX_TREE_HEIGHT;
  }

  private @Nonnull Object getSyncLockFromSliceCoords(int sliceXIndex, int treeY, int sliceZIndex) {
    int hash = Integer.hashCode(sliceXIndex + treeY + sliceZIndex);
    return locks.get(hash % locks.size());
  }

  /*package*/ @Nullable
  GiantRedwoodGenerator.TreeBlock getFromSliceCoords(int sliceXIndex, int treeY, int sliceZIndex) {
    if (treeY >= slices.size() || !areValidSliceIndices(sliceXIndex, treeY, sliceZIndex))
      return null;
    else return slices.get(treeY)[sliceXIndex][sliceZIndex];
  }

  /*package*/ @Nullable
  GiantRedwoodGenerator.TreeBlock setFromSliceCoords(
      int sliceXIndex,
      int treeY,
      int sliceZIndex,
      @Nullable GiantRedwoodGenerator.TreeBlock block) {
    if (!areValidSliceIndices(sliceXIndex, treeY, sliceZIndex)) return null;
    else {
      if (treeY >= slices.size()) slice(treeY).allocate();

      GiantRedwoodGenerator.TreeBlock prev;
      synchronized (getSyncLockFromSliceCoords(sliceXIndex, treeY, sliceZIndex)) {
        prev = slices.get(treeY)[sliceXIndex][sliceZIndex];
        slices.get(treeY)[sliceXIndex][sliceZIndex] = block;
      }

      return prev;
    }
  }

  @Nonnull
  Slice baseSlice() {
    return slice(0);
  }

  @Nonnull
  Slice slice(int y) {
    return new Slice(this, y);
  }

  @Nonnull
  Stream<Slice> streamSlices() {
    Slice base = baseSlice();
    base.allocate();
    return Stream.iterate(base, Slice::hasAbove, Slice::above);
  }

  @Nonnull
  Stream<Cell> streamCells() {
    return streamSlices().flatMap(Slice::streamCells);
  }

  @Nonnull
  Stream<Cell> streamSlice(int y) {
    if (y < 0 || y >= slices.size()) return Stream.empty();
    else {
      int[] sx = {0}, sz = {0};
      return Stream.iterate(
          new Cell(this, 0, y, 0),
          _ignored -> sx[0] < maxSliceIndex - 1 || sz[0] < maxSliceIndex - 1,
          _ignored -> {
            sz[0]++;
            if (sz[0] >= maxSliceIndex) {
              sx[0]++;
              sz[0] = 0;
            }
            return new Cell(this, sx[0], y, sz[0]);
          });
    }
  }

  void markTrunkHeight() {
    this.trunkHeight = slices.size();
  }

  /**
   * Represents an individual "slice," or XZ plane, within a {@link TreeSpace}. To help avoid
   * accidental null references, slices may refer to {@linkplain #isValid() invalid} y-coordinates
   * within the space.
   */
  @Getter
  @ToString
  @EqualsAndHashCode
  @AllArgsConstructor
  public static class Slice {
    final @Nonnull TreeSpace tree;
    final int y;

    /**
     * @return {@code true} IFF the {@link #y} coordinate for this slice can validly exist within a
     *     tree space.
     */
    boolean isValid() {
      return y >= 0 && y < MAX_TREE_HEIGHT;
    }

    /**
     * @return a (potentially empty) stream of all the valid cells within this slice.
     */
    public @Nonnull Stream<Cell> streamCells() {
      return tree.streamSlice(y);
    }

    /**
     * @return a (potentially {@linkplain #isValid() invalid} slice representing the XZ plane
     *     immediately above (positive {@code y}) this slice in the tree space.
     */
    public @Nonnull Slice above() {
      return new Slice(tree, y + 1);
    }

    /**
     * @return {@code true} IFF the XZ plane {@link #above() immediately above} this slice
     *     represents a valid {@code y} coordinate.
     */
    public boolean hasAbove() {
      return y < tree.slices.size() - 1;
    }

    /**
     * @return a (potentially {@linkplain #isValid() invalid} slice representing the XZ plane
     *     immediately below (negativea {@code y}) this slice in the tree space.
     */
    @Nonnull
    Slice below() {
      return new Slice(tree, y - 1);
    }

    /**
     * @see TreeSpace#get(int, int, int)
     */
    public @Nullable GiantRedwoodGenerator.TreeBlock get(int treeX, int treeZ) {
      return tree.get(treeX, y, treeZ);
    }

    /**
     * @see TreeSpace#set(int, int, int, GiantRedwoodGenerator.TreeBlock)
     */
    public @Nullable GiantRedwoodGenerator.TreeBlock set(
        int treeX, int treeZ, @Nullable GiantRedwoodGenerator.TreeBlock block) {
      return tree.set(treeX, y, treeZ, block);
    }

    /**
     * @return the (potentially invalid) cell within this slice at the given 2D coordinates. The
     *     {@code z} coordinate is taken from the vector's {@link #getY() y} component.
     */
    public @Nonnull Cell cell(@Nonnull Vec2 treeCoordinates) {
      return new Cell(
          tree,
          (int) tree.treeCoordinateToUnsafeSliceIndex(treeCoordinates.x),
          y,
          (int) tree.treeCoordinateToUnsafeSliceIndex(treeCoordinates.y));
    }

    /**
     * Ensures that the tree space associated with this slice has allocated memory to store block
     * data. Does nothing if the slice is not {@linkplain #isValid() valid}.
     *
     * @return an empty optional if this slice is not valid, or an optional wrapping {@code this}
     *     instance (for method chaining) if the slice is valid and space was allocated.
     */
    /*package*/ @Nonnull
    Optional<Slice> allocate() {
      synchronized (tree.slices) {
        if (!isValid()) return Optional.empty();
        else {
          for (int y = tree.slices.size() - 1; y < this.y; y++)
            tree.slices.add(
                new GiantRedwoodGenerator.TreeBlock[tree.maxSliceIndex][tree.maxSliceIndex]);

          return Optional.of(this);
        }
      }
    }
  }

  /**
   * Represents an individual storage slot within a {@link TreeSpace}. Cells may represent
   * coordinates that are not valid.
   */
  @ToString
  @EqualsAndHashCode
  public static class Cell {
    /*package*/ @Getter final TreeSpace tree;
    /*package*/ @Getter final int y;
    /*package*/ @Getter final int treeX;
    /*package*/ @Getter final int treeZ;

    /*package*/ final int sliceXIndex;
    /*package*/ final int sliceZIndex;

    /*package*/ Cell(@Nonnull TreeSpace tree, int sliceXIndex, int treeY, int sliceZIndex) {
      this.tree = tree;
      this.y = treeY;
      this.sliceXIndex = sliceXIndex;
      this.sliceZIndex = sliceZIndex;
      this.treeX = tree.sliceIndexToTreeCoordinate(sliceXIndex);
      this.treeZ = tree.sliceIndexToTreeCoordinate(sliceZIndex);
    }

    /**
     * @return {@code true} IFF these coordinates reference a valid location within the parent tree
     *     space.
     */
    public boolean isValid() {
      return tree.areValidSliceIndices(sliceXIndex, y, sliceZIndex);
    }

    /**
     * @return the tree block stored at this location within the tree space, or {@code null} when
     *     the cell is not valid or when the space does not define a specific block.
     */
    public @Nullable GiantRedwoodGenerator.TreeBlock get() {
      return tree.getFromSliceCoords(sliceXIndex, y, sliceZIndex);
    }

    /**
     * @return the tree block stored at this location within the tree space. When this cell is not
     *     valid, or when the space does not define a specific block, returns {@link
     *     GiantRedwoodGenerator.TreeBlock#AIR}.
     */
    public @Nonnull GiantRedwoodGenerator.TreeBlock getNonnull() {
      return Optional.ofNullable(get()).orElse(AIR);
    }

    /**
     * Sets the tree block stored at this location within the tree space. Does nothing when this
     * cell is not valid.
     *
     * @return the tree block previously stored at this location, or {@code null} when the cell is
     *     not valid or when no specific block was stored.
     */
    public @Nullable GiantRedwoodGenerator.TreeBlock set(
        @Nullable GiantRedwoodGenerator.TreeBlock block) {
      return tree.setFromSliceCoords(sliceXIndex, y, sliceZIndex, block);
    }

    /**
     * Sets the tree block stored at this location within the tree space, unless the current cell is
     * {@linkplain #isFilled() currently occupied}.
     *
     * @return
     *     <ol>
     *       <li>when this location is invalid, does not set and returns {@code null}
     *       <li>when this location is valid and previously {@linkplain #isEmpty() empty}, sets and
     *           returns {@code null} or {@code AIR} as appropriate
     *       <li>when this location is valid and previously {@linkplain #isFilled() filled}, does
     *           <em>not</em> set and returns the previous block
     *     </ol>
     */
    public @Nullable GiantRedwoodGenerator.TreeBlock setIfEmpty(
        @Nullable GiantRedwoodGenerator.TreeBlock block) {
      synchronized (tree.getSyncLockFromSliceCoords(sliceXIndex, y, sliceZIndex)) {
        GiantRedwoodGenerator.TreeBlock previous = get();
        if (previous == null || previous.isEmpty()) set(block);
        return previous;
      }
    }

    /**
     * @return {@code true} IFF the {@linkplain #get() stored block at this location} is non-{@code
     *     null} and not {@linkplain
     *     com.brotherhoodgames.pixen.mod.tree.GiantRedwoodGenerator.TreeBlock#isEmpty() empty}.
     */
    public boolean isEmpty() {
      GiantRedwoodGenerator.TreeBlock b = get();
      return b == null || b.isEmpty();
    }

    /**
     * @return {@code true} if the {@linkplain #get() stored block at this location} is neither
     *     {@code null} nor {@linkplain
     *     com.brotherhoodgames.pixen.mod.tree.GiantRedwoodGenerator.TreeBlock#isEmpty() empty}.
     */
    public boolean isFilled() {
      return !isEmpty();
    }

    /**
     * @return {@code true} IFF the {@linkplain #get() stored block at this location} is exactly
     *     equal to the given reference.
     */
    boolean is(@Nonnull GiantRedwoodGenerator.TreeBlock predicate) {
      return get() == predicate;
    }

    /**
     * @return a non-{@code null} stream of the blocks immediately surrounding this location in the
     *     XZ plane (the north, east, south, and west locations). Each of the provided blocks will
     *     be non-{@code null}; {@code AIR} is returned for any invalid or empty locations.
     */
    public @Nonnull Stream<GiantRedwoodGenerator.TreeBlock> surroundingBlocksInSlice() {
      return Stream.of(north(), east(), south(), west()).map(c -> c.map(Cell::get).orElse(AIR));
    }

    /**
     * @return {@code true} if any of the {@linkplain #surroundingBlocksInSlice() blocks surrounding
     *     this location} is exactly equal to the given reference.
     */
    public boolean isTouchingInSlice(@Nonnull GiantRedwoodGenerator.TreeBlock block) {
      return surroundingBlocksInSlice().anyMatch(b -> b == block);
    }

    /**
     * @return {@code true} if the given predicate returns {@code true} for any of the {@linkplain
     *     #surroundingCells() blocks surrounding this location in 3D space}.
     */
    public boolean isTouching(@Nonnull Predicate<GiantRedwoodGenerator.TreeBlock> predicate) {
      return surroundingCells()
          .map(c -> Optional.ofNullable(c.get()).orElse(AIR))
          .anyMatch(predicate);
    }

    /**
     * @return {@code true} IFF the given predicate returns {@code true} for all {@linkplain
     *     #isValid() valid} cells {@linkplain #surroundingCells() surrounding this location in 3D
     *     space}.
     */
    public boolean isSurroundedBy(@Nonnull Predicate<GiantRedwoodGenerator.TreeBlock> predicate) {
      return surroundingCells()
          .map(c -> Optional.ofNullable(c.get()).orElse(AIR))
          .allMatch(predicate);
    }

    /**
     * @return a potentially empty stream containing all the valid cells immediately surrounding
     *     this location (the north, east, south, and west cells).
     */
    public @Nonnull Stream<Cell> surroundingCellsInSlice() {
      return Stream.of(north(), east(), south(), west())
          .filter(Optional::isPresent)
          .map(Optional::get);
    }

    /**
     * @return a potentially empty stream containing all the valid cells immediately surrounding
     *     this location in 3D space (the north, east, south, west, up, and down cells).
     */
    public @Nonnull Stream<Cell> surroundingCells() {
      return Stream.of(north(), east(), south(), west(), up(), down())
          .filter(Optional::isPresent)
          .map(Optional::get);
    }

    /**
     * @return a (potentially empty) stream of non-null, valid tree cells surrounding this cell in
     *     the parent tree space. The returned envelope is a cube with side length {@code
     *     envelopeSize} and centered on this cell.
     */
    public @Nonnull Stream<Cell> streamCellEnvelope(double envelopeSize) {
      final double halfEnvelope = envelopeSize / 2;

      final int startSx = (int) Math.round(sliceXIndex - halfEnvelope);
      final int endSx = (int) Math.round(sliceXIndex + halfEnvelope);
      final int startSz = (int) Math.round(sliceZIndex - halfEnvelope);
      final int endSz = (int) Math.round(sliceZIndex + halfEnvelope);
      final int startY = (int) Math.round(y - halfEnvelope);
      final int endY = (int) Math.round(y + halfEnvelope);

      return Stream.iterate(
              new Cell(tree, startSx, startY, startSz),
              cell -> cell.sliceZIndex < endSz && (cell.y < endY || cell.sliceXIndex < endSx),
              cell -> {
                int sx = cell.sliceXIndex, y = cell.y, sz = cell.sliceZIndex;
                sx++;
                if (sx >= endSx) {
                  sx = 0;
                  y++;
                  if (y >= endY) {
                    y = 0;
                    sz++;
                  }
                }
                return new Cell(tree, sx, y, sz);
              })
          .filter(Cell::isValid);
    }

    /**
     * @return an optional wrapping the valid cell location immediately to the {@linkplain
     *     net.minecraft.core.Direction#NORTH north} of this location, or an empty optional if that
     *     cell is not valid within the tree space.
     */
    public @Nonnull Optional<Cell> north() {
      return Optional.ofNullable(
          sliceZIndex > 0 ? new Cell(tree, sliceXIndex, y, sliceZIndex - 1) : null);
    }

    /**
     * @return an optional wrapping the valid cell location immediately to the {@linkplain
     *     net.minecraft.core.Direction#EAST north} of this location, or an empty optional if that
     *     cell is not valid within the tree space.
     */
    public @Nonnull Optional<Cell> east() {
      return Optional.ofNullable(
          sliceXIndex < tree.maxSliceIndex - 1
              ? new Cell(tree, sliceXIndex + 1, y, sliceZIndex)
              : null);
    }

    /**
     * @return an optional wrapping the valid cell location immediately to the {@linkplain
     *     net.minecraft.core.Direction#SOUTH north} of this location, or an empty optional if that
     *     cell is not valid within the tree space.
     */
    public @Nonnull Optional<Cell> south() {
      return Optional.ofNullable(
          sliceZIndex < tree.maxSliceIndex - 1
              ? new Cell(tree, sliceXIndex, y, sliceZIndex + 1)
              : null);
    }

    /**
     * @return an optional wrapping the valid cell location immediately to the {@linkplain
     *     net.minecraft.core.Direction#WEST north} of this location, or an empty optional if that
     *     cell is not valid within the tree space.
     */
    public @Nonnull Optional<Cell> west() {
      return Optional.ofNullable(
          sliceXIndex > 0 ? new Cell(tree, sliceXIndex - 1, y, sliceZIndex) : null);
    }

    /**
     * @return an optional wrapping the valid cell location immediately {@linkplain
     *     net.minecraft.core.Direction#UP above} of this location, or an empty optional if that
     *     cell is not valid within the tree space.
     */
    public @Nonnull Optional<Cell> up() {
      return Optional.ofNullable(
          y < MAX_TREE_HEIGHT - 1 ? new Cell(tree, sliceXIndex, y + 1, sliceZIndex) : null);
    }

    /**
     * @return an optional wrapping the valid cell location immediately {@linkplain
     *     net.minecraft.core.Direction#DOWN below} of this location, or an empty optional if that
     *     cell is not valid within the tree space.
     */
    public @Nonnull Optional<Cell> down() {
      return Optional.ofNullable(y > 0 ? new Cell(tree, sliceXIndex, y - 1, sliceZIndex) : null);
    }

    /**
     * @return the square of the distance between this and the given location.
     */
    public double distanceToSqr(@Nonnull BlockPos treeCoordinates) {
      int dx = treeX - treeCoordinates.getX();
      int dy = y - treeCoordinates.getY();
      int dz = treeZ - treeCoordinates.getZ();
      return dx * dx + dy * dy + dz * dz;
    }

    /**
     * @return the square of the distance between this and the given location.
     */
    public double distanceToSqr(@Nonnull Vec3 target) {
      return toTreePos().getCenter().distanceToSqr(target);
    }

    /**
     * @return a random point contained within this coordinate based on deviation from the center
     *     point.
     */
    public @Nonnull Vec3 randomContainedTreeCoordinate(@Nonnull RandomSource randomSource) {
      double dx = -0.5 + randomSource.nextDouble();
      double dy = -0.5 + randomSource.nextDouble();
      double dz = -0.5 + randomSource.nextDouble();
      return toTreePos().getCenter().add(dx, dy, dz);
    }

    /**
     * @return a block position initialized to this location's coordinates in tree space.
     */
    public @Nonnull BlockPos toTreePos() {
      return new BlockPos(treeX, y, treeZ);
    }
  }
}
