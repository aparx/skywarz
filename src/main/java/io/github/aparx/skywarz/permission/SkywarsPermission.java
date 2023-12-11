package io.github.aparx.skywarz.permission;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.github.aparx.bufig.ArrayPath;
import io.github.aparx.skywarz.entity.SkywarsPlayer;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.bukkit.permissions.Permissible;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Collections;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-05 16:06
 * @since 1.0
 */
@Getter
public final class SkywarsPermission {

  private static final ArrayPath PERMISSION_PREFIX = ArrayPath.of("skywarz");

  /** Users with this permission are able to kick other players when a lobby is full */
  public static final SkywarsPermission PRIORITY = parse("priority");

  /** Users with this permission are able to create, delete and generally manage Skywarz */
  public static final SkywarsPermission SETUP = parse("setup");

  /** Users with this permission are able to skip the waiting phase */
  public static final SkywarsPermission QUICKSTART = parse("quickstart");

  /** Users with this permission are able to see their own (total) statistics */
  public static final SkywarsPermission STATS_SELF = parse("stats.self");

  /** Users with this permission are able to see others' (total) statistics */
  public static final SkywarsPermission STATS_OTHER = parse("stats.other");

  /** Users with this permission are able to modify the statistics of a player */
  public static final SkywarsPermission STATS_MANIPULATE = parse("stats.modify");

  private final @NonNull ImmutableList<ArrayPath> permissions;

  private SkywarsPermission(@NonNull ImmutableList<ArrayPath> permissions) {
    Preconditions.checkNotNull(permissions, "Permissions must not be null");
    Preconditions.checkState(!permissions.isEmpty(), "Permissions must not be empty");
    this.permissions = permissions;
  }

  private SkywarsPermission(@NonNull Collection<ArrayPath> permissions) {
    Preconditions.checkNotNull(permissions, "Permissions must not be null");
    Preconditions.checkState(!permissions.isEmpty(), "Permissions must not be empty");
    ImmutableList.Builder<ArrayPath> builder = new ImmutableList.Builder<>();
    permissions.forEach((string) -> createWildcards(string, builder));
    builder.addAll(permissions);
    this.permissions = builder.build();
  }

  public static SkywarsPermission parse(@NonNull String permission) {
    return new SkywarsPermission(Collections.singletonList(ArrayPath.parse(permission)));
  }

  public static SkywarsPermission parse(@NonNull String @NonNull ... permissions) {
    Validate.notEmpty(permissions, "Permissions must not be empty");
    ImmutableList.Builder<ArrayPath> perms =
        ImmutableList.builderWithExpectedSize(permissions.length);
    for (String string : permissions)
      perms.add(PERMISSION_PREFIX.add(ArrayPath.parse(string)));
    return new SkywarsPermission(perms.build());
  }

  public static SkywarsPermission merge(@NonNull SkywarsPermission @NonNull ... permissions) {
    Preconditions.checkNotNull(permissions, "Permissions must not be null");
    ImmutableList.Builder<ArrayPath> builder = ImmutableList.builder();
    for (SkywarsPermission permission : permissions)
      builder.addAll(permission.getPermissions());
    return new SkywarsPermission(builder.build());
  }

  private static void createWildcards(
      ArrayPath permission, ImmutableList.Builder<ArrayPath> builder) {
    for (ArrayPath path = permission; !(path = path.parent()).isEmpty(); )
      builder.add(path.add("*"));
  }

  public boolean has(@NonNull SkywarsPlayer player) {
    return player.findOnline().filter(this::has).isPresent();
  }

  public boolean has(@NonNull Permissible permissible) {
    return permissions.stream().anyMatch((perm) -> permissible.hasPermission(perm.join()));
  }
}
