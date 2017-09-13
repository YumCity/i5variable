package com.i5mc.variable;

import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Created by on 2017/9/1.
 */
public class VarLocalExec extends EZPlaceholderHook {

    private final Plugin plugin;

    public VarLocalExec(Plugin plugin) {
        super(plugin, "i5local");
        this.plugin = plugin;
    }

    @Override
    public String onPlaceholderRequest(Player player, String input) {
        return plugin.getConfig().getString("local." + input);
    }

}
