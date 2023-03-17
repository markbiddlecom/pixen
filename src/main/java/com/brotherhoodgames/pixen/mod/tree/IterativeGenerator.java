package com.brotherhoodgames.pixen.mod.tree;

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.minecraft.util.RandomSource;

/** An object that can iteratively generate sections of a tree. */
/*package*/ interface IterativeGenerator {
  /**
   * Instructs the generator to add the next sequence of blocks to the given tree space.
   *
   * @param random a seeded random number source that can be used to build repeatably random
   *     results.
   * @param parameters the generation parameters that constrain the resulting tree.
   * @param tree the tree space the generator should query and populate.
   * @return a potentially empty stream of generators that should continue processing in the next
   *     generator iteration.
   */
  @Nonnull
  Stream<IterativeGenerator> iterate(
      @Nonnull RandomSource random,
      @Nonnull GiantRedwoodGenerationParameters parameters,
      @Nonnull TreeSpace tree);
}
