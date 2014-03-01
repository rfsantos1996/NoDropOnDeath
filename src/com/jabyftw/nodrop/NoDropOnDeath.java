package com.jabyftw.nodrop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
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
public class NoDropOnDeath extends JavaPlugin implements Listener, CommandExecutor {

    private FileConfiguration config;
    private boolean permitir, deletar;
    private final LinkedList<Material> lista = new LinkedList();
    private final Map<Player, ItemStack[]> inventory = new HashMap();
    private final Map<Player, ItemStack[]> equipment = new HashMap();

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        config = getConfig();
        String[] li = {"golden_apple", "diamond_sword"};
        config.addDefault("list", Arrays.asList(li));
        config.addDefault("useAsKeepList", false);
        config.addDefault("deleteDropedItems", true);
        config.options().copyDefaults(true);
        saveConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginCommand("nodrop").setExecutor(this);
        getLogger().log(Level.INFO, "Enabled in {0}ms", (System.currentTimeMillis() - start));
    }

    @Override
    public void onDisable() {
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (p.hasPermission("nodropondeath.restoreall")) {
            inventory.put(p, p.getInventory().getContents());
            equipment.put(p, p.getInventory().getArmorContents());
            e.getDrops().clear();
        } else if (p.hasPermission("nodropondeath.use")) {
            ArrayList<ItemStack> restaurar = new ArrayList();
            Iterator<ItemStack> drops = e.getDrops().iterator();
            if (drops.hasNext()) {
                ItemStack is = drops.next();
                if (permitir) {
                    if (lista.contains(is.getType())) {
                        restaurar.add(is);
                    } else {
                        e.getDrops().remove(is);
                    }
                } else {
                    if (lista.contains(is.getType())) {
                        e.getDrops().remove(is);
                    } else {
                        restaurar.add(is);
                    }
                }
            }
            if (deletar) {
                e.getDrops().clear();
            }
            inventory.put(p, restaurar.toArray(new ItemStack[restaurar.size() - 1]));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (equipment.containsKey(p)) {
            p.getInventory().setArmorContents(equipment.get(p));
            equipment.remove(p);
        }
        if (inventory.containsKey(p)) {
            for (ItemStack is : inventory.get(p)) {
                if (is != null) {
                    p.getInventory().addItem(is);
                }
            }
            inventory.remove(p);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender.hasPermission("nodropondeath.reload")) {
            loadConfig();
            sender.sendMessage("§6Reloaded.");
            return true;
        } else {
            return false;
        }
    }

    private void loadConfig() {
        reloadConfig();
        permitir = config.getBoolean("useAsKeepList");
        deletar = config.getBoolean("deleteDropedItems");
        for (String s : config.getStringList("list")) {
            lista.add(Material.valueOf(s.toUpperCase()));
        }
    }
}