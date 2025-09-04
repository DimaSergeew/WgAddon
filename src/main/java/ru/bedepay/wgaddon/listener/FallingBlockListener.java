package ru.bedepay.wgaddon.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class FallingBlockListener implements Listener {
   private final RegionContainer container;
   private final List<Location> fallingBlockLocations;

   public FallingBlockListener(JavaPlugin plugin) {
      if (plugin == null) {
         throw new IllegalArgumentException("plugin cannot be null");
      }
      
      boolean isEnabled = plugin.getConfig().getBoolean("enable_falling_blocks_fix");
      if (isEnabled) {
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
      }

      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      if (container == null) {
         throw new IllegalStateException("RegionContainer cannot be null");
      }
      this.container = container;
      this.fallingBlockLocations = new ArrayList<>();
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public final void on(EntityChangeBlockEvent event) {
      if (event == null || !(event.getEntity() instanceof FallingBlock)) {
         return;
      }
      
      Block block = event.getBlock();
      if (block == null) {
         return;
      }
      
      Location blockLocation = block.getLocation().toBlockLocation();
      if (blockLocation == null) {
         return;
      }
      
      // Запоминаем локацию падающего блока
      this.fallingBlockLocations.add(blockLocation);
      
      // Если блок в защищенном регионе - разрешаем падающему блоку его изменить
      if (this.isLocationInRegion(blockLocation)) {
         event.setCancelled(false);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public final void on(ItemSpawnEvent event) {
      if (event == null) {
         return;
      }
      
      Location eventLocation = event.getLocation().toBlockLocation();
      if (eventLocation == null) {
         return;
      }
      
      // Проверяем, есть ли в этой локации падающий блок
      Location foundLocation = null;
      for (Location location : this.fallingBlockLocations) {
         if (this.isLocationsSimilar(location, eventLocation)) {
            foundLocation = location;
            break;
         }
      }
      
      // Если нашли падающий блок в этой локации - отменяем спавн предмета
      if (foundLocation != null) {
         this.fallingBlockLocations.remove(foundLocation);
         event.setCancelled(true);
      }
   }

   private final boolean isLocationInRegion(Location loc) {
      if (loc == null || loc.getWorld() == null) {
         return false;
      }
      
      RegionManager regionManager = this.container.get(BukkitAdapter.adapt(loc.getWorld()));
      if (regionManager == null) {
         return false;
      }
      
      ApplicableRegionSet regions = regionManager.getApplicableRegions(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
      return regions.size() > 0;
   }

   private final boolean isLocationsSimilar(Location loc1, Location loc2) {
      if (loc1 == null || loc2 == null) {
         return false;
      }
      return (int)loc1.getX() == (int)loc2.getX() && (int)loc1.getZ() == (int)loc2.getZ();
   }
}
