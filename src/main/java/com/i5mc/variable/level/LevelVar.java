package com.i5mc.variable.level;

import com.i5mc.variable.L2Pool;
import com.i5mc.variable.Main;
import com.i5mc.variable.PluginHelper;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;

import static java.util.logging.Level.SEVERE;

public class LevelVar extends EZPlaceholderHook implements PluginHelper.IExec {

    public LevelVar(Main main) {
        super(main, "i5level");
    }

    public void exec(CommandSender sender, List<String> list) {
        Iterator<String> itr = list.iterator();
        try {
            Exec.valueOf(itr.next().toUpperCase().replace('-', '_')).apply(sender, itr);
        } catch (IllegalArgumentException err) {
            Bukkit.getLogger().log(SEVERE, "" + err, err);
        }
    }

    public String onPlaceholderRequest(Player p, String label) {
        try {
            return Label.valueOf(label.toUpperCase()).apply(p);
        } catch (IllegalArgumentException ignored) {
        }
        return label;
    }

    enum Exec {

        LEVEL {
            public void apply(CommandSender send, Iterator<String> input) {
                if (!(send instanceof Player)) {
                    return;
                }

                send.sendMessage("" + L2Pool.INSTANCE.level((Player) send).getLevel());
            }
        },

        LEVEL_UP {
            public void apply(CommandSender send, Iterator<String> input) {
                if (!(send instanceof Player)) {
                    return;
                }

                Level level = L2Pool.INSTANCE.level((Player) send);
                int i = L2Pool.INSTANCE.nextLevel(level);
                int xp = level.getXp();
                if (xp >= i) {
                    level.setXp(xp - i);
                    level.setLevel(level.getLevel() + 1);
                    L2Pool.INSTANCE.save(level);
                    Main.getMessenger().send(send, "level_up", "等级提升");
                } else {
                    Main.getMessenger().send(send, "xp", "经验不足");
                }
            }
        },

        XP {
            public void apply(CommandSender send, Iterator<String> input) {
                if (!(send instanceof Player)) {
                    return;
                }

                send.sendMessage("" + L2Pool.INSTANCE.level((Player) send).getXp());
            }
        },

        XP_ADD {
            public void apply(CommandSender send, Iterator<String> input) {
                if (!send.hasPermission("i5level.admin")) {
                    return;
                }

                Player p = Bukkit.getPlayerExact(input.next());
                int value = Integer.parseInt(input.next());

                Level level = L2Pool.INSTANCE.level(p);
                level.setXp(level.getXp() + value);
                level.setXpTotal(level.getXpTotal() + value);

                L2Pool.INSTANCE.save(level);

                String line = Main.getMessenger().find("xp_up", "获得%xp%点经验值");

                p.sendMessage(line.replace("%xp%", "" + value));
            }
        };

        public void apply(CommandSender send, Iterator<String> input) {
            throw new AbstractMethodError("apply");
        }
    }

    enum Label {

        LEVEL {
            public String apply(Player p) {
                Level level = L2Pool.INSTANCE.level(p);
                return "" + level.getLevel();
            }
        },

        XP {
            public String apply(Player p) {
                Level level = L2Pool.INSTANCE.level(p);
                return "" + level.getXp();
            }
        },


        XP_TOTAL {
            public String apply(Player p) {
                Level level = L2Pool.INSTANCE.level(p);
                return "" + level.getXpTotal();
            }
        },

        XP_NEXT_LEVEL {
            public String apply(Player p) {
                Level level = L2Pool.INSTANCE.level(p);
                return "" + L2Pool.INSTANCE.nextLevel(level);
            }
        },;

        public String apply(Player p) {
            throw new AbstractMethodError("apply");
        }
    }
}
