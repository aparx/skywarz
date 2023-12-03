package io.github.aparx.skywarz.handler.configs;

import io.github.aparx.bufig.configurable.field.ConfigMapping;
import io.github.aparx.bufig.configurable.field.Document;
import io.github.aparx.bufig.configurable.object.ConfigId;
import io.github.aparx.bufig.configurable.object.ConfigObject;
import io.github.aparx.bufig.handler.ConfigHandler;
import io.github.aparx.skywarz.Skywars;
import io.github.aparx.skywarz.handler.SkywarsConfigHandler;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-01 03:37
 * @since 1.0
 */
@Getter
@ConfigId("main")
public class MainConfig extends ConfigObject {

  public static final String DEFAULT_LANGUAGE_NAME = "languages/english";

  @ConfigMapping("language file")
  @Document({
      "The target language config that is to be used. This value refers to a filename",
      "relative to the directory of Skywarz, but just without its extension. This language",
      "will be used throughout Skywarz. The file is created if it is not existing already.",
  })
  private String language = DEFAULT_LANGUAGE_NAME;

  public MainConfig() {
    this(Skywars.getInstance().getConfigHandler());
  }

  public MainConfig(@NonNull ConfigHandler<?> handler) {
    super(handler);
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
