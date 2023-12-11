package io.github.aparx.skywarz.database;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-10 07:16
 * @since 1.0
 */
public enum DatabaseLoadingState {

  DISABLED,
  LOADING,
  LOADED,
  ERROR;

  public final boolean isEnabled() {
    return this == LOADING || this == LOADED;
  }

}
