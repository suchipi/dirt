package com.suchipi.myfirstbukkitplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

@SuppressWarnings("unused")
public final class MyFirstBukkitPlugin extends JavaPlugin {
    ScriptEngine engine;

    @Override
    public void onEnable() {
        System.setProperty("nashorn.args", "--language=es6");
        engine = new ScriptEngineManager().getEngineByName("nashorn");
    }

    @Override
    public void onDisable() {
        engine = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            Object result = engine.eval(String.join(" ", args));
            sender.sendMessage(String.valueOf(result));
        } catch (Exception error) {
            sender.sendMessage("Error: " + error.getMessage());
        }

        return true;
    }
}
