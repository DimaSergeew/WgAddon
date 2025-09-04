package ru.bedepay.wgaddon.listener;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wither;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.bedepay.wgaddon.util.Utils;

public final class WitherListener implements Listener {
    private final JavaPlugin plugin;

    public WitherListener(JavaPlugin plugin) {
        this.plugin = plugin;
        if (plugin.getConfig().getBoolean("features.wither", true)) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void on(EntityChangeBlockEvent event) {
        Entity e = event.getEntity();
        if (!(e instanceof Wither)) return;

        Location loc = event.getBlock().getLocation();
        if (Utils.isGriefAllowed(plugin, loc)) {
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void on(EntityExplodeEvent event) {
        Entity e = event.getEntity();
        if (!(e instanceof Wither)) return;

        Location loc = event.getLocation();
        if (Utils.isGriefAllowed(plugin, loc)) {
            event.setCancelled(false);
        }
    }
}