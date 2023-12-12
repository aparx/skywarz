package io.github.aparx.skywarz;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-12 18:11
 * @since 1.0
 */
public interface RegisterNotifiable {

  /** Called when this object has been registered in some kind of collection or map */
  void notifyRegister();

  /** Called when this object has been removed off some kind of collection or map */
  void notifyRemoval();

}
