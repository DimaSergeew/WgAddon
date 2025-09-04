package ru.bedepay.wgaddon.listener;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.bedepay.wgaddon.util.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class FallingBlockListener implements Listener {
    private final JavaPlugin plugin;
    private final Map<Key, Long> marks = new HashMap<>();

    private record Key(UUID world, int x, int y, int z) {}

    public FallingBlockListener(JavaPlugin plugin) {
        this.plugin = plugin;
        if (plugin.getConfig().getBoolean("features.falling_blocks", true)) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void on(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof FallingBlock)) return;

        Block b = event.getBlock();
        Location loc = b.getLocation();
        long now = System.currentTimeMillis();
        long ttl = plugin.getConfig().getLong("cleanup.falling_block_mark_ms", 1000);

        // пометим точку падения
        marks.put(new Key(loc.getWorld().getUID(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), now);

        // чистка старых меток «по пути»
        if (marks.size() > 2048) sweep(now, ttl);

        // разрешаем механику, если тут гриф разрешён
        if (Utils.isGriefAllowed(plugin, loc)) {
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void on(ItemSpawnEvent event) {
        Location loc = event.getLocation();
        Key k = new Key(loc.getWorld().getUID(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        Long t = marks.remove(k);
        if (t != null) {
            long ttl = plugin.getConfig().getLong("cleanup.falling_block_mark_ms", 1000);
            if (System.currentTimeMillis() - t <= ttl) {
                // предмет родился как следствие падения блока: если гриф тут разрешён — убираем айтем
                if (Utils.isGriefAllowed(plugin, loc)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private void sweep(long now, long ttl) {
        Iterator<Map.Entry<Key, Long>> it = marks.entrySet().iterator();
        while (it.hasNext()) {
            if (now - it.next().getValue() > ttl) it.remove();
        }
    }
}