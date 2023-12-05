package io.github.aparx.skywarz.handler.configs;

import io.github.aparx.bufig.configurable.object.ConfigId;
import io.github.aparx.bufig.configurable.object.ConfigObject;
import io.github.aparx.skywarz.Skywars;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-05 09:30
 * @since 1.0
 */
@ConfigId("messages")
public class MessagesConfig extends ConfigObject {

  public MessagesConfig() {
    super(Skywars.getInstance().getConfigHandler());
  }



}
