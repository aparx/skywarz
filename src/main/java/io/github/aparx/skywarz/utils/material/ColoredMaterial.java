package io.github.aparx.skywarz.utils.material;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.Validate;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.EnumMap;
import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-05 08:27
 * @since 1.0
 */
public final class ColoredMaterial {

  public static final ColoredMaterial CONCRETE =
      new ColoredMaterial(new DefaultConverter(Material.WHITE_CONCRETE));

  public static final ColoredMaterial WOOL =
      new ColoredMaterial(new DefaultConverter(Material.WHITE_WOOL));

  private final Function<DyeColor, Material> converter;

  private final EnumMap<DyeColor, Material> byColor = new EnumMap<>(DyeColor.class);

  public ColoredMaterial(@NonNull Function<DyeColor, Material> converter) {
    Preconditions.checkNotNull(converter, "Converter must not be null");
    this.converter = converter;
  }

  public Material getMaterial(@NonNull DyeColor color) {
    return byColor.computeIfAbsent(color, converter);
  }

  public static class DefaultConverter implements Function<DyeColor, Material> {
    private final @NonNull String colorlessBaseName;

    public DefaultConverter(@NonNull Material reference) {
      Preconditions.checkNotNull(reference, "Material must not be null");
      this.colorlessBaseName = getMaterialBase(reference.name());
    }

    private static String getMaterialBase(String name) {
      int index = name.indexOf('_');
      Preconditions.checkArgument(index != -1, "Material is not colorable!");
      return Validate.notEmpty(name.substring(1 + index));
    }

    @Override
    public Material apply(DyeColor dyeColor) {
      return Material.valueOf(dyeColor.name() + '_' + colorlessBaseName);
    }
  }
}
