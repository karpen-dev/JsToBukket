package com.karpen.tsToBukket;

import com.karpen.tsToBukket.jsFunctions.JsLogger;
import com.karpen.tsToBukket.jsFunctions.JsServer;
import io.methvin.watcher.DirectoryChangeEvent;
import io.methvin.watcher.DirectoryWatcher;
import org.bukkit.plugin.java.JavaPlugin;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class TsToBukket extends JavaPlugin {

    private Context jsContext;
    private Map<String, Value> plugins = new HashMap<>();
    private File pluginsDir;
    private Logger jsLogger;

    static {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
        System.setProperty("polyglotimpl.DisableClassPathIsolation", "true");
    }

    @Override
    public void onEnable() {
        jsLogger = getLogger();
        pluginsDir = new File(getDataFolder(), "typescript-plugins");
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs();
        }

        getLogger().info("Java VM name: " + System.getProperty("java.vm.name"));
        getLogger().info("Java Home: " + System.getProperty("java.home"));
        getLogger().info("Java version: " + System.getProperty("java.version"));
        getLogger().info("GraalVM version: " + System.getProperty("graalvm.version"));
        getLogger().info("Current working dir: " + System.getProperty("user.dir"));

        try {
            Class.forName("org.graalvm.polyglot.Context");
            getLogger().info("GraalVM Polyglot is available!");
        } catch (ClassNotFoundException e) {
            getLogger().info("Polyglot not found.");
        }

        initJsEngine();

        loadAllPlugins();

        setupFileWatcher();
    }

    private void initJsEngine() {
        try {
            Engine engine = Engine.newBuilder()
                    .useSystemProperties(true)
                    .option("engine.WarnInterpreterOnly", "false")
                    .option("engine.DisableClassPathIsolation", "true")
                    .build();

            jsContext = Context.newBuilder("js")
                    .engine(engine)
                    .allowAllAccess(true)
                    .allowIO(true)
                    .allowHostClassLoading(true)
                    .option("js.esm-eval-returns-exports", "true")
                    .build();

            getLogger().info("GraalVM JavaScript engine initialized successfully");

        } catch (Exception e) {
            getLogger().severe("Failed to initialize JS engine: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("JS engine initialization failed", e);
        }
    }

    private void loadAllPlugins() {
        File[] pluginFiles = pluginsDir.listFiles((dir, name) -> name.endsWith(".js"));
        if (pluginFiles != null) {
            for (File pluginFile : pluginFiles) {
                loadPlugin(pluginFile);
            }
        }
    }

    private void loadPlugin(File pluginFile) {
        try {
            String pluginName = pluginFile.getName().replace(".js", "");
            jsLogger.info("Loading TypeScript plugin: " + pluginName);

            Value pluginExports = jsContext.eval("js", (CharSequence) new FileReader(pluginFile));
            Value pluginInstance = pluginExports.getMember("default");

            if (pluginInstance != null && pluginInstance.canExecute()) {
                plugins.put(pluginName, pluginInstance);
                pluginInstance.invokeMember("onEnable");
            }
        } catch (Exception e) {
            jsLogger.severe("Failed to load plugin " + pluginFile.getName() + ": " + e.getMessage());
        }
    }

    private void setupFileWatcher() {
        try {
            DirectoryWatcher watcher = DirectoryWatcher.builder()
                    .path(pluginsDir.toPath())
                    .listener(event -> {
                        if (event.eventType() == DirectoryChangeEvent.EventType.CREATE || event.eventType() == DirectoryChangeEvent.EventType.MODIFY) {
                            Path filePath = event.path();
                            if (filePath.toString().endsWith(".js")) {
                                getServer().getScheduler().runTask(this, () -> {
                                    reloadPlugin(filePath.toFile());
                                });
                            }
                        }
                    })
                    .build();

            new Thread(watcher::watch).start();
        } catch (IOException e) {
            jsLogger.warning("Failed to setup file watcher: " + e.getMessage());
        }
    }

    private void reloadPlugin(File pluginFile) {
        String pluginName = pluginFile.getName().replace(".js", "");
        Value oldPlugin = plugins.get(pluginName);

        if (oldPlugin != null) {
            oldPlugin.invokeMember("onDisable");
        }

        loadPlugin(pluginFile);
    }

    @Override
    public void onDisable() {
        plugins.values().forEach(plugin -> {
            try {
                plugin.invokeMember("onDisable");
            } catch (Exception e) {
                jsLogger.warning("Error disabling plugin: " + e.getMessage());
            }
        });

        if (jsContext != null) {
            jsContext.close();
        }
    }
}
