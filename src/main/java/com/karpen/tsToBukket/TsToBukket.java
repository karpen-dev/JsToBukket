package com.karpen.tsToBukket;

import com.karpen.tsToBukket.commands.Js;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.command.defaults.PluginsCommand;
import org.bukkit.event.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mozilla.javascript.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public final class TsToBukket extends JavaPlugin implements CommandExecutor {

    private Context rhino;
    private final Map<String, Scriptable> loadedScripts = new HashMap<>();
    private final Map<String, List<Function>> eventHandlers = new HashMap<>();
    private final Map<String, Class<? extends Event>> eventClasses = new HashMap<>();
    private final Map<String, Function> commandHandlers = new HashMap<>();
    private Js jsCmd;

    @Override
    public void onEnable() {
        rhino = Context.enter();
        rhino.setLanguageVersion(Context.VERSION_ES6);
        registerCommonEvents();

        Scriptable sharedScope = rhino.initStandardObjects();

        initNodeJsEnvironment(sharedScope);

        loadAllScripts(sharedScope);

        jsCmd = new Js(this, sharedScope, loadedScripts);
        Objects.requireNonNull(getCommand("js")).setExecutor(jsCmd);
        Objects.requireNonNull(getCommand("js")).setTabCompleter(jsCmd);
    }

    private void initNodeJsEnvironment(Scriptable scope) {
        try {
            ScriptableObject pluginApi = new NativeObject();

            ScriptableObject.putProperty(pluginApi, "onEvent", new BaseFunction() {
                public Object call(Context cx, Scriptable caller, Scriptable thisObj, Object[] args) {
                    if (args.length >= 2) {
                        registerEvent(args[0].toString(), (Function)args[1]);
                    }
                    return Undefined.instance;
                }
            });

            ScriptableObject.putProperty(pluginApi, "onCommand", new BaseFunction() {
                public Object call(Context cx, Scriptable caller, Scriptable thisObj, Object[] args) {
                    if (args.length >= 2) {
                        registerJsCommand(args[0].toString(), (Function)args[1]);
                    }
                    return Undefined.instance;
                }
            });

            ScriptableObject.putProperty(pluginApi, "logger", getLogger());

            ScriptableObject.putProperty(scope, "__pluginAPI__", pluginApi);

            String moduleSystem = "var plugin = __pluginAPI__;\n" +
                    "function require(module) {\n" +
                    "    if (module === 'plugin') return __pluginAPI__;\n" +
                    "    return null;\n" +
                    "}";

            rhino.evaluateString(scope, moduleSystem, "moduleSystem.js", 1, null);

        } catch (Exception e) {
            getLogger().severe("Error initializing JS environment: " + e.getMessage());
        }
    }

    public void registerJsCommand(String commandName, Function callback) {
        commandHandlers.put(commandName.toLowerCase(), callback);

        try {
            Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getPluginManager());

            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PluginCommand cmd = constructor.newInstance(commandName, this);

            cmd.setDescription("Command from JavaScript");
            cmd.setUsage("/" + commandName);
            cmd.setExecutor(this);

            commandMap.register(this.getName(), cmd);

            getLogger().info("Command registered: /" + commandName);
        } catch (Exception e) {
            getLogger().severe("Error registration: " + e.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Function handler = commandHandlers.get(cmd.getName().toLowerCase());
        if (handler != null) {
            try {
                Scriptable commandInfo = rhino.newObject(rhino.initStandardObjects());
                ScriptableObject.putProperty(commandInfo, "sender", Context.javaToJS(sender, commandInfo));
                ScriptableObject.putProperty(commandInfo, "command", Context.javaToJS(cmd, commandInfo));
                ScriptableObject.putProperty(commandInfo, "label", label);
                ScriptableObject.putProperty(commandInfo, "args", Context.javaToJS(args, commandInfo));

                Object result = handler.call(rhino, rhino.initStandardObjects(), handler, new Object[]{commandInfo});
                return result instanceof Boolean ? (Boolean) result : true;
            } catch (Exception e) {
                getLogger().severe("Error with execution command: " + e.getMessage());
                sender.sendMessage("§cError with execution command");
                return true;
            }
        }
        return false;
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

    public void registerEvent(String eventName, Function callback) {
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

    public void loadAllScripts(Scriptable sharedScope) {
        File[] jsFiles = getDataFolder().listFiles((dir, name) -> name.endsWith(".js"));

        if (jsFiles == null) {
            getLogger().warning("Scripts folder not found!");
            return;
        }

        for (File jsFile : jsFiles) {
            loadScript(jsFile, sharedScope);
        }
    }

    public void loadScript(File jsFile, Scriptable sharedScope) {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(jsFile), StandardCharsets.UTF_8)) {
            Scriptable scope = rhino.newObject(sharedScope);
            scope.setPrototype(sharedScope);
            scope.setParentScope(null);

            String scriptContent = new BufferedReader(reader).lines()
                    .collect(Collectors.joining("\n"));

            String wrappedScript = "(function() {\n" + scriptContent + "\n})();";

            rhino.evaluateString(scope, wrappedScript, jsFile.getName(), 1, null);

            Object onEnable = scope.get("onEnable", scope);
            if (onEnable instanceof Function) {
                ((Function)onEnable).call(rhino, scope, scope, new Object[]{});
            }

            loadedScripts.put(jsFile.getName(), scope);
            getLogger().info("Script loaded: " + jsFile.getName());

        } catch (Exception e) {
            getLogger().severe("Script load error (" + jsFile.getName() + "): " + e.getMessage());
        }
    }

    private String readResourceFile(String filename) {
        try (InputStream is = getResource(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            getLogger().severe("Failed to read resource file: " + filename);
            return "";
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
                getLogger().warning("Error with onDisable " + name + ": " + e.getMessage());
            }
        });

        eventHandlers.clear();
        Context.exit();
    }
}