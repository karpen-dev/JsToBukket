package com.karpen.tsToBukket.commands;

import com.karpen.tsToBukket.TsToBukket;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.Configuration;
import org.mozilla.javascript.Scriptable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Js implements CommandExecutor, TabCompleter {

    private TsToBukket plugin;
    private Map<String, Scriptable> scripts;
    private Configuration config;
    private Scriptable scriptable;

    public Js(TsToBukket plugin, Scriptable scriptable, Map<String, Scriptable> scripts) {
        this.plugin = plugin;
        this.scriptable = scriptable;
        this.scripts = scripts;

        plugin.saveDefaultConfig();

        config = plugin.getConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("msg.args"))));

            return true;
        }

        if (!sender.hasPermission("jstobukkit.*")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("msg.perms"))));

            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                config = plugin.getConfig();
                plugin.loadAllScripts(scriptable);

                break;
            case "reloadscript":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("msg.args"))));

                    break;
                }

                plugin.loadScript(new File(plugin.getDataFolder() + "\\" + args[1]), scriptable);

                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggests = new ArrayList<>();

        if (!sender.hasPermission("jstobukkit.*")) {
            return List.of();
        }

        if (args.length == 1) {
            suggests.add("reload");
            suggests.add("reloadscript");
        }

        if (args.length == 2) {
            if (scripts != null) {
                for (Object val : scripts.values()) {
                    if (val != null) {
                        if (val instanceof String)
                            suggests.add(val.toString());
                    }
                }
            }
        }

        return suggests;
    }
}
