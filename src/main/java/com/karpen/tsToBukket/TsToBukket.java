package com.karpen.tsToBukket;

import org.bukkit.plugin.java.JavaPlugin;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.File;
import java.io.FileReader;

public final class TsToBukket extends JavaPlugin {

    @Override
    public void onEnable() {
        Context rhino = Context.enter();
        try {
            Scriptable scope = rhino.initStandardObjects();

            ScriptableObject.putProperty(scope, "plugin", this);

            if (!getDataFolder().exists()){
                getDataFolder().mkdirs();
            }

            String jsFilePath = getDataFolder().getAbsolutePath() + "/plugin.js";
            rhino.evaluateReader(scope, new FileReader(jsFilePath), "plugin.js", 1, null);

            Object jsOnEnable = scope.get("onEnable", scope);
            if (jsOnEnable instanceof Function) {
                ((Function) jsOnEnable).call(rhino, scope, scope, new Object[]{});
            }
        } catch (Exception e) {
            getLogger().severe("Ошибка при выполнении JavaScript: " + e.getMessage());
        } finally {
            Context.exit();
        }
    }

    @Override
    public void onDisable() {

    }
}
