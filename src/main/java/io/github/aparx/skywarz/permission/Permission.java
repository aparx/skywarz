package io.github.aparx.skywarz.permission;

import com.google.common.base.Preconditions;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import lombok.Getter;
import org.bukkit.permissions.Permissible;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-05 16:06
 * @since 1.0
 */
@Getter
public final class Permission {

  private static final ArrayPath PERMISSION_PREFIX = ArrayPath.of("skywarz");

  /** Users with this permission are able to kick other players when a lobby is full */
  public static final Permission PRIORITY = of("priority");

  /** Users with this permission are able to create, delete and generally manage Skywarz */
  public static final Permission SETUP = of("setup");

  /** Users with this permission are able to skip the waiting phase */
  public static final Permission QUICKSTART = of("quickstart");

  private final @NonNull ArrayPath permission;

  private Permission(@NonNull ArrayPath permission) {
    Preconditions.checkNotNull(permission, "Permission must not be null");
    this.permission = PERMISSION_PREFIX.add(permission);
  }

  public static Permission of(@NonNull String permission) {
    return new Permission(ArrayPath.parse(permission, ArrayPath.DEFAULT_SEPARATOR));
  }

  public boolean has(@NonNull SkywarsPlayer player) {
    return player.findOnline().filter(this::has).isPresent();
  }

  public boolean has(@NonNull Permissible permissible) {
    return permissible.hasPermission(permission.join());
  }
}
