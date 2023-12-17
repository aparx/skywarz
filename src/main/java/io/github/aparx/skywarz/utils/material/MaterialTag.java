package io.github.aparx.skywarz.utils.material;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-09 04:39
 * @since 1.0
 */
@Getter
public final class MaterialTag implements Predicate<Material> {

  /** Materials that depend on other blocks and are thus connected in some form */
  public static final MaterialTag connected = new MaterialTag()
      .addAll("ANVIL", "VINES", "CORAL", "BANNER", "TORCH", "CARPET", "BUTTON", "FLOWER",
          "ROOTS", "SAPLING", "SIGN", "PRESSURE_PLATE", "RAIL")
      .addAll(Material.VINE, Material.SEA_PICKLE, Material.LILY_PAD, Material.LADDER,
          Material.FLOWER_POT, Material.GRAVEL, Material.FERN, Material.BAMBOO,
          Material.LEVER, Material.SNOW, Material.REDSTONE, Material.REDSTONE_WIRE,
          Material.DAYLIGHT_DETECTOR, Material.COMPARATOR, Material.REPEATER, Material.TALL_GRASS,
          Material.GRASS, Material.TALL_SEAGRASS, Material.SEAGRASS, Material.SCAFFOLDING,
          Material.CACTUS, Material.STRING)
      .register();

  /** Materials that represent items, that contain content that can be emptied (i.e. buckets) */
  public static final MaterialTag emptyableBucket = new MaterialTag()
      .add((x) -> x != Material.MILK_BUCKET && ArrayUtils.contains(splitToSegments(x), "BUCKET"))
      .register();

  private final Set<Material> involved = new HashSet<>();
  private final List<Predicate<Material>> predicates = new ArrayList<>();

  public static Predicate<Material> newPredicate(String segment) {
    return (test) -> ArrayUtils.contains(splitToSegments(test), segment);
  }

  public static String[] splitToSegments(Material material) {
    return material.name().split("_");
  }

  @CanIgnoreReturnValue
  public MaterialTag register() {
    if (predicates.isEmpty())
      return this;
    Material[] values = Material.values();
    predicates.forEach((predicate) -> {
      for (Material material : values)
        if (predicate.test(material))
          involved.add(material);
    });
    return this;
  }

  public boolean isTagged(Material material) {
    return involved.contains(material);
  }

  @Override
  public boolean test(Material material) {
    return isTagged(material);
  }

  @CanIgnoreReturnValue
  public MaterialTag addAll(Material... materials) {
    if (ArrayUtils.isNotEmpty(materials))
      involved.addAll(Arrays.asList(materials));
    return this;
  }

  @CanIgnoreReturnValue
  @SuppressWarnings("unchecked")
  public MaterialTag addAll(Predicate<Material>... predicates) {
    if (ArrayUtils.isNotEmpty(predicates))
      this.predicates.addAll(Arrays.asList(predicates));
    return this;
  }

  @CanIgnoreReturnValue
  @SuppressWarnings("unchecked")
  public MaterialTag addAll(String... materialSegments) {
    if (ArrayUtils.isNotEmpty(materialSegments))
      return addAll(Arrays.stream(materialSegments)
          .map(MaterialTag::newPredicate)
          .toArray(Predicate[]::new));
    return this;
  }

  @CanIgnoreReturnValue
  public MaterialTag add(Predicate<Material> predicate) {
    this.predicates.add(predicate);
    return this;
  }


}
