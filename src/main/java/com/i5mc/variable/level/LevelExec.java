package com.i5mc.variable.level;

import com.i5mc.variable.L2Pool;
import com.i5mc.variable.Main;
import com.i5mc.variable.PluginHelper;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.List;

import static java.util.logging.Level.SEVERE;

public class LevelExec extends EZPlaceholderHook implements PluginHelper.IExec {

    public LevelExec(Main main) {
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

                I5Level level = L2Pool.INSTANCE.level((Player) send);
                int i = L2Pool.INSTANCE.nextLevel(level);
                int xp = level.getXp();
                if (xp < i) {
                    Main.getMessenger().send(send, "xp", "经验不足");
                } else {
                    level.setXp(xp - i);
                    level.setLevel(level.getLevel() + 1);
                    L2Pool.INSTANCE.save(level);
                    if (Main.getGlobal().getLevel().isInjectXpBar()) {
                        Main.updateXpBar(((Player) send), level);
                    }
                    Main.getMessenger().send(send, "level_up", "等级提升");
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

                I5Level level = L2Pool.INSTANCE.level(p);
                level.setXp(level.getXp() + value);
                level.setXpTotal(level.getXpTotal() + value);

                L2Pool.INSTANCE.save(level);

                String line = Main.getMessenger().find("xp_up", "获得%xp%点经验值");
                p.sendMessage(line.replace("%xp%", "" + value));

                int i = L2Pool.INSTANCE.nextLevel(level);
                if (i > level.getXp() && Main.getGlobal().getLevel().isAutoLevelUp()) {
                    if (Main.getGlobal().getLevel().isInjectXpBar()) {
                        Main.updateXpBar(p, level);
                    }
                } else {
                    LEVEL_UP.apply(p, input);
                }
            }
        };

        public void apply(CommandSender send, Iterator<String> input) {
            throw new AbstractMethodError("apply");
        }
    }

    enum Label {

        LEVEL {
            public String apply(Player p) {
                I5Level level = L2Pool.INSTANCE.level(p);
                return "" + level.getLevel();
            }
        },

        LEVEL_PERCENT {
            public String apply(Player p) {
                I5Level level = L2Pool.INSTANCE.level(p);
                int i = L2Pool.INSTANCE.nextLevel(level);
                int xp = level.getXp();
                return xp >= i ? "100" : "" + BigDecimal.valueOf(xp).divide(BigDecimal.valueOf(i), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            }
        },

        LEVEL_BAR {
            public String apply(Player p) {
                I5Level level = L2Pool.INSTANCE.level(p);
                int i = L2Pool.INSTANCE.nextLevel(level);
                int xp = level.getXp();
                if (i > xp) {
                    int fill = BigDecimal.valueOf(xp).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(i), 2, BigDecimal.ROUND_HALF_UP).divide(BigDecimal.valueOf(2), BigDecimal.ROUND_HALF_UP).intValue();
                    StringBuilder b = new StringBuilder();
                    b.append("§a");
                    for (int l = fill; l >= 1; l--) {
                        b.append('|');
                    }
                    b.append("§7");
                    fill = 50 - fill;
                    for (int l = fill; l >= 1; l--) {
                        b.append('|');
                    }
                    return String.valueOf(b.append("§r"));
                }
                return "§a||||||||||||||||||||||||||||||||||||||||||||||||||§r";
            }
        },

        XP {
            public String apply(Player p) {
                I5Level level = L2Pool.INSTANCE.level(p);
                return "" + level.getXp();
            }
        },


        XP_TOTAL {
            public String apply(Player p) {
                I5Level level = L2Pool.INSTANCE.level(p);
                return "" + level.getXpTotal();
            }
        },

        XP_NEXT_LEVEL {
            public String apply(Player p) {
                I5Level level = L2Pool.INSTANCE.level(p);
                return "" + L2Pool.INSTANCE.nextLevel(level);
            }
        },;

        public String apply(Player p) {
            throw new AbstractMethodError("apply");
        }
    }
}
