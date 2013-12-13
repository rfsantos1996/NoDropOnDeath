package com.jabyftw.nodrop;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Rafael
 */
public class NoDropOnDeath extends JavaPlugin implements Listener {

    private Map<Player, ItemStack[]> inventory = new HashMap();
    private Map<Player, ItemStack[]> equipment = new HashMap();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (p.hasPermission("nodropondeath.restore")) {
            inventory.put(p, p.getInventory().getContents());
            equipment.put(p, p.getInventory().getArmorContents());
            e.getDrops().clear();
        } else if (p.hasPermission("nodropondeath.use")) {
            e.getDrops().clear();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("nodropondeath.restore")) {
            if (equipment.containsKey(p)) {
                p.getInventory().setArmorContents(equipment.get(p));
            }
            equipment.remove(p);
            if (inventory.containsKey(p)) {
                for (ItemStack is : inventory.get(p)) {
                    if (is != null) {
                        p.getInventory().addItem(is);
                    }
                }
            }
            inventory.remove(p);
        }
    }
}
