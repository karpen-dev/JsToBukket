package com.karpen.tsToBukket.jsFunctions;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;

public class JsLogger {
    private final JavaPlugin plugin;
    private final String name;
    private final java.util.logging.Logger logger;

    public JsLogger(JavaPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        this.logger = plugin.getLogger();
    }

    public void info(String message) {
        logger.log(Level.INFO, "[" + name + "] " + message);
    }

    public void warning(String message) {
        logger.log(Level.WARNING, "[" + name + "] " + message);
    }

    public void severe(String message) {
        logger.log(Level.SEVERE, "[" + name + "] " + message);
    }
}
