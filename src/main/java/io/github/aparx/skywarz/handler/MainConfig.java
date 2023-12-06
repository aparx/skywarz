package io.github.aparx.skywarz.handler;

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
