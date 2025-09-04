package ru.bedepay.wgaddon.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PistonListener implements Listener {
   private final RegionContainer container;

   public PistonListener(JavaPlugin plugin) {
      if (plugin == null) {
         throw new IllegalArgumentException("plugin cannot be null");
      }
      
      boolean isEnabled = plugin.getConfig().getBoolean("enable_pistons_fix");
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
   public final void on(BlockPistonExtendEvent event) {
      if (event == null) {
         return;
      }
      
      // Проверяем, находится ли поршень в защищенном регионе
      Location pistonLocation = event.getBlock().getLocation();
      if (isLocationInProtectedRegion(pistonLocation)) {
         // Разрешаем поршню работать в защищенном регионе
         event.setCancelled(false);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public final void on(BlockPistonRetractEvent event) {
      if (event == null) {
         return;
      }
      
      // Проверяем, находится ли поршень в защищенном регионе
      Location pistonLocation = event.getBlock().getLocation();
      if (isLocationInProtectedRegion(pistonLocation)) {
         // Разрешаем поршню работать в защищенном регионе
         event.setCancelled(false);
      }
   }

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
