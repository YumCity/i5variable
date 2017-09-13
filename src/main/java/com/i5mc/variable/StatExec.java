package com.i5mc.variable;

import lombok.SneakyThrows;
import lombok.val;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by on 2017/9/12.
 */
public class StatExec extends EZPlaceholderHook {

    public StatExec(Plugin plugin) {
        super(plugin, "i5stat");
    }

    public enum Label {

        MY(Label::myFunc);

        private final IFunc<String> func;

        Label(IFunc<String> func) {
            this.func = func;
        }

        @SneakyThrows
        public static String myFunc(CommandSender p, Iterator<String> input) {
            val load = L2Pool.INST.load(((Player) p)).get().object.get(input.next().toUpperCase());
            if (load == null) return "-1";
            return String.valueOf(load);
        }
    }

    @Override
    public String onPlaceholderRequest(Player p, String input) {
        val itr = Arrays.asList(input.split("_")).iterator();
        try {
            return Label.valueOf(itr.next().toUpperCase()).func.apply(p, itr);
        } catch (IllegalArgumentException ign) {
        }
        return null;
    }

}
