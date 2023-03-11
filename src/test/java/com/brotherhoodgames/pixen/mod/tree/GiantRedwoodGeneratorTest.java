package com.brotherhoodgames.pixen.mod.tree;

import java.util.Arrays;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class GiantRedwoodGeneratorTest {
  @Test
  void testGenerate() {
    GiantRedwoodGenerator subject = new GiantRedwoodGenerator();
    char[][][] tree = new char[100][20][20];
    int maxY[] = {0};
    for (int y = 0; y < 100; y++)
      for (int x = 0; x < 20; x++) for (int z = 0; z < 20; z++) tree[y][x][z] = ' ';
    subject.generate(
        10,
        0,
        10,
        RandomSource.create(),
        (b, x, y, z) -> {
          tree[y][x][z] = b == GiantRedwoodGenerator.TreeBlock.BARK ? 'X' : 'o';
          if (y > maxY[0]) maxY[0] = y;
        });
    Arrays.stream(tree)
        .limit(maxY[0])
        .flatMap(slice -> Arrays.stream(slice).map(String::new))
        .forEach(System.out::println);
  }
}
