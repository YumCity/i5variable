package com.i5mc.variable;

import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Created by on 2017/9/1.
 */
public class VarExec extends EZPlaceholderHook {

    private Map<String, Variable> mapping;

    public VarExec(Main main) {
        super(main, "i5var");
    }

    public void setMapping(Map<String, Variable> mapping) {
        this.mapping = mapping;
    }

    public String onPlaceholderRequest(Player p, String input) {
        return mapping.containsKey(input) ? mapping.get(input).getValue() : null;
    }

}
