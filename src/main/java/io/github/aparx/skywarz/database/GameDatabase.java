package io.github.aparx.skywarz.database;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.support.ConnectionSource;
import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.bufig.configurable.field.Document;
import io.github.aparx.bufig.configurable.object.ConfigObject;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.database.stats.PlayerStatsManager;
import io.github.aparx.skywarz.handler.SkywarsConfigHandler;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-09 08:30
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public final class GameDatabase {

  static {
    Logger.setGlobalLogLevel(com.j256.ormlite.logger.Level.ERROR);
  }

  private final PlayerStatsManager statsManager = new PlayerStatsManager(this);

  @Getter
  @Setter
  private volatile DatabaseLoadingState state = DatabaseLoadingState.DISABLED;

  @Getter(onMethod_ = {@Synchronized})
  private volatile ConnectionSource source;

  /** Queue defining consumers that are executed when the database is loaded */
  @Getter(AccessLevel.NONE)
  private final Queue<Consumer<GameDatabase>> queue = new LinkedList<>();

  /** Queues given {@code operation} and returns true if the operation is actually executable. */
  @CanIgnoreReturnValue
  public boolean queue(Consumer<GameDatabase> operation) {
    if (!getState().isEnabled())
      return false; // discard database queue: database is not enabled
    if (isLoaded()) operation.accept(this);
    else queue.add(operation);
    return true;
  }

  private void executeQueue() {
    for (Consumer<GameDatabase> c; (c = queue.poll()) != null; )
      c.accept(this);
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Void> connect() {
    Config.instance.load();
    if (!Config.instance.isActive()) {
      setState(DatabaseLoadingState.DISABLED);
      return CompletableFuture.completedFuture(null);
    }
    setState(DatabaseLoadingState.LOADING);
    return executeAsync(() -> {
      this.source = new JdbcConnectionSource(
          Config.instance.getJdbc(),
          Config.instance.getUsername(),
          Config.instance.getPassword());
      Skywars.logger().info("Initialized database");
    }).thenCompose((x) -> {
      try {
        return statsManager.register();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }).whenComplete((v, t) -> {
      setState(t != null ? DatabaseLoadingState.ERROR : DatabaseLoadingState.LOADED);
      if (isLoaded()) executeQueue();
      else queue.clear();
    });
  }

  public void disconnect() {
    if (isLoaded()) source.closeQuietly();
    statsManager.unregister();
  }

  public boolean isState(DatabaseLoadingState state) {
    return getState() == state;
  }

  public boolean isLoaded() {
    return source != null && isState(DatabaseLoadingState.LOADED);
  }

  public boolean isEnabled() {
    return source != null && getState().isEnabled();
  }

  public <T> CompletableFuture<T> executeAsync(SupplyingAction<T> action) {
    Plugin plugin = Skywars.plugin();
    if (!plugin.isEnabled()) {
      Skywars.logger().log(Level.FINER, "Cannot execute database action (plugin disabled)");
      return CompletableFuture.completedFuture(null);
    }
    CompletableFuture<T> future = new CompletableFuture<>();
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      try {
        future.complete(action.execute());
      } catch (Exception e) {
        future.completeExceptionally(e);
        throw new RuntimeException(e);
      }
    });
    return future;
  }

  public <T> CompletableFuture<T> executeAsync(RunningAction action) {
    return executeAsync(() -> {
      action.run();
      return null;
    });
  }

  public interface SupplyingAction<R> {
    R execute() throws Exception;
  }

  public interface RunningAction {
    void run() throws Exception;
  }

  @Getter
  private static final class Config extends ConfigObject {

    private static final Config instance = new Config();

    @ConfigMapping
    @Document({
        "True to enable this configured database.",
        "Note: Skywarz will shut down if the connection cannot be established!"
    })
    private boolean active = false;

    @ConfigMapping
    @Document({
        "The Java Database Connectivity (JDBC) URL that identifies your target database.",
        "For MySQL, simply replace \"<hostname>\" and \"<database>\" with your respective values.",
        "Read: https://tableplus.com/blog/2019/09/jdbc-connection-strings.html"
    })
    private String jdbc = "jdbc:mysql://<hostname>/<database>";

    @ConfigMapping
    private String username = "<username>";

    @ConfigMapping
    private String password = "<password>";

    private Config() {
      super((proxy) -> Skywars.getInstance().getConfigHandler().getOrCreate("database"));
    }

    @Override
    public void load() {
      setHeader(SkywarsConfigHandler.createHeader(
          "The primary database configuration.",
          "Change the values to have Skywarz connected to your database.",
          "Supported: MySQL, Postgres, Microsoft SQL, H2, Derby, HSQLDB and Sqlite."
      ));
      super.load();
    }
  }

}
