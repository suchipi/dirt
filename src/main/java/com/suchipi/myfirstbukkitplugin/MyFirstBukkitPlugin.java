package com.suchipi.myfirstbukkitplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

@SuppressWarnings("unused")
public final class MyFirstBukkitPlugin extends JavaPlugin {
    Context context;
    Scriptable scope;
    Object jsPlugin;

    @Override
    public void onEnable() {
        context = Context.enter();
        scope = context.initStandardObjects(null);
        jsPlugin = Context.javaToJS(this, scope);
        ScriptableObject.putProperty(scope, "plugin", jsPlugin);
    }

    @Override
    public void onDisable() {
        Context.exit();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            Object result = context.evaluateString(scope, String.join(" ", args), "command", 1, null);
            sender.sendMessage(Context.toString(result));
        } catch (Exception error) {
            sender.sendMessage("Error: " + error.getMessage());
        }

        return true;
    }
}
