package ru.bedepay.wgaddon;

import org.bukkit.plugin.java.JavaPlugin;
import ru.bedepay.wgaddon.listener.FallingBlockListener;
import ru.bedepay.wgaddon.listener.PistonListener;
import ru.bedepay.wgaddon.listener.WitherListener;

public final class WgAddon extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("WgAddon loaded. Mode: " + getConfig().getString("grief.mode"));
        new FallingBlockListener(this);
        new PistonListener(this);
        new WitherListener(this);
    }
}