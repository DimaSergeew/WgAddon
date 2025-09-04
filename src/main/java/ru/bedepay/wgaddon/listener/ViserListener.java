package ru.bedepay.wgaddon.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class ViserListener implements Listener {
    private final RegionContainer container;

    public ViserListener(JavaPlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin cannot be null");
        }
        
        boolean isEnabled = plugin.getConfig().getBoolean("enable_viser_break_fix", true);
        if (isEnabled) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        if (container == null) {
            throw new IllegalStateException("RegionContainer cannot be null");
        }
        this.container = container;
    }

    @EventHandler(
        priority = EventPriority.HIGHEST
    )
    public final void on(EntityChangeBlockEvent event) {
        if (event == null) {
            return;
        }
        
        Entity entity = event.getEntity();
        if (entity == null || !(entity instanceof Wither)) {
            return;
        }
        
        Location blockLocation = event.getBlock().getLocation();
        if (blockLocation == null) {
            return;
        }
        
        // Проверяем, находится ли блок в защищенном регионе
        if (isLocationInProtectedRegion(blockLocation)) {
            // Разрешаем Wither'у ломать блок в любом защищенном регионе
            event.setCancelled(false);
        }
    }

    @EventHandler(
        priority = EventPriority.HIGHEST
    )
    public final void on(EntityExplodeEvent event) {
        if (event == null) {
            return;
        }
        
        Entity entity = event.getEntity();
        if (entity == null || !(entity instanceof Wither)) {
            return;
        }
        
        // Разрешаем Wither'у взрываться в любых регионах
        event.setCancelled(false);
    }

    /**
     * Проверяет, находится ли локация в защищенном регионе
     */
    private boolean isLocationInProtectedRegion(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return false;
        }
        
        RegionManager regionManager = this.container.get(BukkitAdapter.adapt(loc.getWorld()));
        if (regionManager == null) {
            return false;
        }
        
        ApplicableRegionSet regions = regionManager.getApplicableRegions(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        
        // Проверяем, есть ли регионы в этой локации (если есть - значит это защищенная территория)
        return regions.size() > 0;
    }
}
