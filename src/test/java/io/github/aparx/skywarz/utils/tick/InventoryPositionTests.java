package io.github.aparx.skywarz.utils.tick;

import io.github.aparx.skywarz.game.inventory.InventoryPosition;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-18 21:56
 * @since 1.0
 */
public class InventoryPositionTests {

  @Test
  public void toIndex() {
    InventoryPosition position = InventoryPosition.ofPoint(0, 0);
    Assertions.assertEquals(0, position.toIndex());
    position = InventoryPosition.ofPoint(1, 0);
    Assertions.assertEquals(1, position.toIndex());
    position = InventoryPosition.ofPoint(8, 0);
    Assertions.assertEquals(8, position.toIndex());
    position = InventoryPosition.ofPoint(0, 1);
    Assertions.assertEquals(9, position.toIndex());
    position = InventoryPosition.ofPoint(8, 1);
    Assertions.assertEquals(17, position.toIndex());
  }

  @Test
  public void ofIndex() {
    InventoryPosition position = InventoryPosition.ofIndex(0);
    Assertions.assertEquals(0, position.getColumn());
    Assertions.assertEquals(0, position.getRow());

    position = InventoryPosition.ofIndex(9);
    Assertions.assertEquals(0, position.getColumn());
    Assertions.assertEquals(1, position.getRow());

    position = InventoryPosition.ofIndex(9);
    Assertions.assertEquals(0, position.getColumn());
    Assertions.assertEquals(1, position.getRow());

    position = InventoryPosition.ofIndex(30);
    Assertions.assertEquals(3, position.getColumn());
    Assertions.assertEquals(3, position.getRow());
  }

}
