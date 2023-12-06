package io.github.aparx.skywarz;

import lombok.experimental.UtilityClass;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-06 02:33
 * @since 1.0
 */
@UtilityClass
public final class Magics {

  public static boolean isDevelopment() {
    // use a getter to avoid "PointlessArithmeticExpression" and for future changes
    return true;
  }

}
