package io.github.aparx.skywarz.handler;

import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.bufig.configurable.field.Document;
import io.github.aparx.bufig.configurable.object.ConfigObject;
import io.github.aparx.skywarz.Skywars;
import lombok.Getter;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 03:37
 * @since 1.0
 */
@Getter
public class MainConfig extends ConfigObject {

  @Getter
  public static final MainConfig instance = new MainConfig();

  @ConfigMapping("celebration.enabled")
  @Document("If enabled, effects will spawn to celebrate a winner at the lobby when a match is done")
  private boolean celebrationEnabled = true;

  @ConfigMapping("celebration.firework spawn radius")
  @Document("The radius in which fireworks randomly spawn to celebrate the winner")
  private double celebrationFireworkRadius = 20.0;

  @ConfigMapping("celebration.firework height")
  @Document("The minimum height offset to the lobby spawn at which fireworks spawn")
  private double celebrationFireworkHeight = 5;

  private MainConfig() {
    super((proxy) -> Skywars.getInstance().getConfigHandler().getOrCreate("main"));
  }

  @Override
  public void save() {
    setHeader(SkywarsConfigHandler.createHeader(
        "The main configuration of Skywarz (by @bonedfps)",
        "Here contained are general purpose settings"
    ));
    super.save();
  }
}
