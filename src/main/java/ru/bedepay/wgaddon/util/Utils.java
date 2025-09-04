// Utils.java
package ru.bedepay.wgaddon.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public final class Utils {
    private Utils() {}

    public static boolean isGriefAllowed(JavaPlugin plugin, Location loc) {
        if (loc == null || loc.getWorld() == null) return false;

        String mode = plugin.getConfig().getString("grief.mode", "allow_everywhere_except_safe");
        Set<String> safeWorlds = Set.copyOf(plugin.getConfig().getStringList("grief.safe-worlds"));
        Set<String> safeRegions = Set.copyOf(plugin.getConfig().getStringList("grief.safe-regions"));
        Set<String> allowedRegions = Set.copyOf(plugin.getConfig().getStringList("grief.allowed-regions"));

        World world = loc.getWorld();
        if (safeWorlds.contains(world.getName())) return false;

        RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (rm == null) return mode.equals("allow_everywhere_except_safe");

        ApplicableRegionSet set = rm.getApplicableRegions(BukkitAdapter.asBlockVector(loc));
        boolean inAnyRegion = set.size() > 0;

        // соберём id активных регионов в точке
        java.util.HashSet<String> here = new java.util.HashSet<>();
        set.forEach(r -> here.add(r.getId())); // region ids в lower-case

        if ("allow_everywhere_except_safe".equalsIgnoreCase(mode)) {
            // запрещаем, если попали в любой "safe" регион
            for (String id : here) if (safeRegions.contains(id)) return false;
            return true; // везде, где не safe
        } else { // "allow_only_listed"
            if (!inAnyRegion) return false;
            for (String id : here) if (allowedRegions.contains(id)) return true;
            return false;
        }
    }
}