package ru.bedepay.wgaddon;

import org.bukkit.plugin.java.JavaPlugin;
import ru.bedepay.wgaddon.listener.FallingBlockListener;
import ru.bedepay.wgaddon.listener.PistonListener;
import ru.bedepay.wgaddon.listener.ViserListener;

public final class WgAddon extends JavaPlugin {
   public void onEnable() {
      this.saveDefaultConfig();
      new FallingBlockListener(this);
      new PistonListener(this);
      new ViserListener(this);
   }
}
