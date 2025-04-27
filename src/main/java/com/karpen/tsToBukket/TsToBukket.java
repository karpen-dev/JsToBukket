package com.karpen.tsToBukket;

import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.mozilla.javascript.*;
import java.io.*;
import java.util.*;

public final class TsToBukket extends JavaPlugin {
    private Context rhino;
    private final Map<String, Scriptable> loadedScripts = new HashMap<>();
    private final Map<String, List<Function>> eventHandlers = new HashMap<>();
    private final Map<String, Class<? extends Event>> eventClasses = new HashMap<>();

    @Override
    public void onEnable() {
        rhino = Context.enter();
        rhino.setLanguageVersion(Context.VERSION_ES6);

        registerCommonEvents();

        Scriptable sharedScope = rhino.initStandardObjects();
        ScriptableObject.putProperty(sharedScope, "plugin", this);
        ScriptableObject.putProperty(sharedScope, "logger", getLogger());

        String initScript = "if (typeof exports !== 'undefined') delete exports;\n" +
                "if (typeof module !== 'undefined') delete module;\n" +
                "function onEvent(eventName, callback) {\n" +
                "  plugin._registerEvent(eventName, callback);\n" +
                "}";
        rhino.evaluateString(sharedScope, initScript, "init.js", 1, null);

        loadAllScripts(sharedScope);
    }

    private void registerCommonEvents() {
        try {
            registerEventClass("PlayerJoinEvent");
            registerEventClass("PlayerQuitEvent");
            registerEventClass("PlayerMoveEvent");
            registerEventClass("PlayerInteractEvent");

            registerEventClass("BlockBreakEvent");
            registerEventClass("BlockPlaceEvent");

            registerEventClass("EntityDamageEvent");
            registerEventClass("InventoryClickEvent");
        } catch (Exception e) {
            getLogger().warning("Events register error: " + e.getMessage());
        }
    }

    private void registerEventClass(String simpleName) throws ClassNotFoundException {
        String[] packages = {
                "org.bukkit.event.player.",
                "org.bukkit.event.block.",
                "org.bukkit.event.entity.",
                "org.bukkit.event.inventory."
        };

        for (String pkg : packages) {
            try {
                Class<? extends Event> eventClass = (Class<? extends Event>) Class.forName(pkg + simpleName);
                eventClasses.put(simpleName, eventClass);
                return;
            } catch (ClassNotFoundException ignored) {}
        }

        throw new ClassNotFoundException("Class not found: " + simpleName);
    }

    public void _registerEvent(String eventName, Function callback) {
        eventHandlers.computeIfAbsent(eventName, k -> new ArrayList<>()).add(callback);

        Class<? extends Event> eventClass = eventClasses.get(eventName);
        if (eventClass == null) {
            getLogger().warning("Unknown event: " + eventName);
            return;
        }

        Bukkit.getPluginManager().registerEvent(
                eventClass,
                new Listener() {},
                EventPriority.NORMAL,
                (listener, event) -> {
                    if (event.getClass().getSimpleName().equals(eventName)) {
                        for (Function handler : eventHandlers.getOrDefault(eventName, Collections.emptyList())) {
                            try {
                                handler.call(rhino, handler, handler, new Object[]{event});
                            } catch (Exception e) {
                                getLogger().severe("Reading error " + eventName + ": " + e.getMessage());
                            }
                        }
                    }
                },
                this
        );
    }

    private void loadAllScripts(Scriptable sharedScope) {
        File[] jsFiles = getDataFolder().listFiles((dir, name) -> name.endsWith(".js"));

        if (jsFiles == null) {
            getLogger().warning("Scripts folder not found!");
            return;
        }

        for (File jsFile : jsFiles) {
            loadScript(jsFile, sharedScope);
        }
    }

    private void loadScript(File jsFile, Scriptable sharedScope) {
        try (FileReader reader = new FileReader(jsFile)) {
            Scriptable scope = rhino.newObject(sharedScope);
            scope.setPrototype(sharedScope);
            scope.setParentScope(null);

            StringBuilder scriptContent = new StringBuilder();
            try (BufferedReader br = new BufferedReader(reader)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().startsWith("exports.") && !line.trim().startsWith("module.exports")) {
                        scriptContent.append(line).append("\n");
                    }
                }
            }

            rhino.evaluateString(scope, scriptContent.toString(), jsFile.getName(), 1, null);

            if (!(scope.get("onEnable", scope) instanceof Function)) {
                throw new Exception("onEnable not found!");
            }

            if (!(scope.get("onDisable", scope) instanceof Function)) {
                throw new Exception("onDisable not found!");
            }

            ((Function)scope.get("onEnable", scope)).call(rhino, scope, scope, new Object[]{});

            loadedScripts.put(jsFile.getName(), scope);
            getLogger().info("Script " + jsFile.getName() + " loaded!");

        } catch (Exception e) {
            getLogger().severe("Error loading script " + jsFile.getName() + ": " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        loadedScripts.forEach((name, scope) -> {
            try {
                Object onDisable = scope.get("onDisable", scope);
                if (onDisable instanceof Function) {
                    ((Function)onDisable).call(rhino, scope, scope, new Object[]{});
                }
            } catch (Exception e) {
                getLogger().warning("Ошибка при вызове onDisable в скрипте " + name + ": " + e.getMessage());
            }
        });

        eventHandlers.clear();
        Context.exit();
    }
}