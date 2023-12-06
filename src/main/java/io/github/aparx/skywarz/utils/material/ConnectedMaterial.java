package io.github.aparx.skywarz.utils.material;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Material;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 09:10
 * @since 1.0
 */
public final class ConnectedMaterial {

  private static final Set<Structure> COMPILE = Set.of(
      Structure.of("ANVIL"),
      Structure.of("SAND"),
      Structure.of("VINES"),
      Structure.of("CORAL"),
      Structure.of("BANNER"),
      Structure.of("TORCH"),
      Structure.of("CARPET"),
      Structure.of("BUTTON"),
      Structure.of("FLOWER"),
      Structure.of("ROOTS"),
      Structure.of("SAPLING"),
      Structure.of("SIGN"),
      Structure.of("PRESSURE_PLATE"),
      Structure.of("RAIL")
  );

  private static final Set<Material> structures = new HashSet<>();

  static {
    structures.addAll(Set.of(
        Material.VINE,
        Material.SEA_PICKLE,
        Material.LILY_PAD,
        Material.LADDER,
        Material.FLOWER_POT,
        Material.GRAVEL,
        Material.FERN,
        Material.BAMBOO,
        Material.LEVER,
        Material.SNOW,
        Material.REDSTONE,
        Material.REDSTONE_WIRE,
        Material.DAYLIGHT_DETECTOR,
        Material.COMPARATOR,
        Material.REPEATER,
        Material.TALL_GRASS,
        Material.TALL_SEAGRASS,
        Material.SEAGRASS,
        Material.SCAFFOLDING,
        Material.CACTUS,
        Material.STRING
    ));
    Arrays.stream(Material.values()).forEach((material) -> {
      if (COMPILE.stream().anyMatch((x) -> x.test(material)))
        structures.add(material);
    });
  }

  public static boolean isStructure(Material material) {
    return structures.contains(material);
  }

  @RequiredArgsConstructor(staticName = "of")
  private static final class Structure implements Predicate<Material> {
    private final @NonNull Predicate<Material> predicate;

    public static Structure of(String segment) {
      return of((test) -> ArrayUtils.contains(test.name().split("_"), segment));
    }

    public boolean test(Material material) {
      return predicate.test(material);
    }
  }

}
