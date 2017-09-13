package com.i5mc.variable;

import lombok.SneakyThrows;
import lombok.experimental.var;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by on 2017/9/12.
 */
public class StatCommand extends Command {

    public StatCommand() {
        super("i5stat");
        setPermission("i5stat.admin");
    }

    enum Label {

        GET(Label::getFunc),
        SET(Label::setFunc),
        ADD(Label::addFunc);

        private final IFunc<Void> func;

        Label(IFunc<Void> func) {
            this.func = func;
        }

        public static Void getFunc(CommandSender p, Iterator<String> itr) {
            val who = Bukkit.getPlayerExact(itr.next());
            if (who == null) {
                p.sendMessage(ChatColor.RED + "玩家不在线");
            } else {
                p.sendMessage(StatExec.Label.myFunc(who, itr));
            }
            return null;
        }

        @SneakyThrows
        public static Void addFunc(CommandSender p, Iterator<String> itr) {
            val who = Bukkit.getPlayerExact(itr.next());
            if (who == null) {
                p.sendMessage(ChatColor.RED + "玩家不在线");
            } else {
                val load = L2Pool.INST.load(who).get();
                val key = itr.next().toUpperCase();
                var value = (Integer) load.object.get(key);
                int add = Integer.parseInt(itr.next());
                if (value == null) {
                    value = add;
                } else {
                    value = add + value;
                }
                load.object.put(key, value);
                L2Pool.INST.save(load);
                p.sendMessage(ChatColor.GREEN + "Okay");
            }
            return null;
        }

        @SneakyThrows
        public static Void setFunc(CommandSender p, Iterator<String> itr) {
            val who = Bukkit.getPlayerExact(itr.next());
            if (who == null) {
                p.sendMessage(ChatColor.RED + "玩家不在线");
            } else {
                val load = L2Pool.INST.load(who).get();
                load.object.put(itr.next().toUpperCase(), Integer.valueOf(itr.next()));
                L2Pool.INST.save(load);
                p.sendMessage(ChatColor.GREEN + "Okay");
            }
            return null;
        }

    }

    @Override
    public boolean execute(CommandSender who, String lab, String[] input) {
        if (!testPermission(who)) return false;
        val itr = Arrays.asList(input).iterator();
        try {
            Label.valueOf(itr.next().toUpperCase()).func.apply(who, itr);
        } catch (IllegalArgumentException ign) {
        } catch (Exception err) {
            who.sendMessage(ChatColor.RED + "" + err);
            return false;
        }
        return true;
    }
}
