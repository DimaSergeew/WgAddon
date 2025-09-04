package ru.bedepay.wgaddon.listener;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import ru.bedepay.wgaddon.util.Utils;

public final class PistonListener implements Listener {
    private final JavaPlugin plugin;

    public PistonListener(JavaPlugin plugin) {
        this.plugin = plugin;
        if (plugin.getConfig().getBoolean("features.pistons", true)) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void on(BlockPistonExtendEvent event) {
        Vector dir = event.getDirection().getDirection();
        // проверяем все перемещаемые блоки и «новую голову поршня»
        for (Block moved : event.getBlocks()) {
            Location target = moved.getLocation().add(dir);
            if (!Utils.isGriefAllowed(plugin, target)) return; // где-то нельзя — не трогаем cancel
        }
        Location head = event.getBlock().getLocation().add(dir);
        if (Utils.isGriefAllowed(plugin, head)) {
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void on(BlockPistonRetractEvent event) {
        // липкий поршень тянет блоки к себе: проверяем целевые позиции
        Vector dir = event.getDirection().getDirection().multiply(-1); // назад к поршню
        for (Block moved : event.getBlocks()) {
            Location target = moved.getLocation().add(dir);
            if (!Utils.isGriefAllowed(plugin, target)) return;
        }
        if (Utils.isGriefAllowed(plugin, event.getBlock().getLocation())) {
            event.setCancelled(false);
        }
    }
}