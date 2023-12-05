package io.github.aparx.skywarz.utils.tick;

import io.github.aparx.skywarz.utils.material.ColoredMaterial;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-05 08:41
 * @since 1.0
 */
public class ColoredMaterialTests {

  @Test
  public void test_getMaterial() {
    ColoredMaterial test = ColoredMaterial.WOOL;
    Assertions.assertEquals(Material.RED_WOOL, test.getMaterial(DyeColor.RED));
    Assertions.assertEquals(Material.GREEN_WOOL, test.getMaterial(DyeColor.GREEN));
    Assertions.assertEquals(Material.BLUE_WOOL, test.getMaterial(DyeColor.BLUE));
    Assertions.assertEquals(Material.LIME_WOOL, test.getMaterial(DyeColor.LIME));
  }

}
