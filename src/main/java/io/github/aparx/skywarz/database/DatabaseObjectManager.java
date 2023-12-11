package io.github.aparx.skywarz.database;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-09 09:25
 * @since 1.0
 */
@Getter
public abstract class DatabaseObjectManager {

  private final @NonNull SkywarsDatabase database;

  public DatabaseObjectManager(@NonNull SkywarsDatabase database) {
    Preconditions.checkNotNull(database, "Database must not be null");
    this.database = database;
  }

  public abstract CompletableFuture<Void> register() throws SQLException;

  public abstract void unregister();
}
