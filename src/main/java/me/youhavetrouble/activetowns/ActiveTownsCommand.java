package me.youhavetrouble.activetowns;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ActiveTownsCommand extends Command {

    private final ActiveTowns plugin;

    protected ActiveTownsCommand(ActiveTowns plugin) {
        super(
                "activetowns",
                "ActiveTowns command",
                "/activetowns <arg>",
                new ArrayList<>()
        );
        this.plugin = plugin;
        setPermission("activetowns.command");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("activetowns.command.reload")) {
                plugin.reloadConfig();
                sender.sendMessage(Component.text("ActiveTowns reloaded. Now set to %s days".formatted(plugin.getDaysInactive())));
                return true;
            }
            if (args[0].equalsIgnoreCase("makesafe") && sender.hasPermission("activetowns.command.makesafe")) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Only usable by players"));
                    return true;
                }
                handleMakeSafe(player, null);
                return true;
            }
        }
        if (args.length == 2) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Only usable by players"));
                return true;
            }
            if (args[0].equalsIgnoreCase("makesafe") && sender.hasPermission("activetowns.command.makesafe")) {

                if (args[1].equalsIgnoreCase("true")) {
                    handleMakeSafe(player, true);
                    return true;
                } else if (args[1].equalsIgnoreCase("false")) {
                    handleMakeSafe(player, false);
                    return true;
                }
                sender.sendMessage(Component.text("Available options: true, false"));
                return true;
            }
        }
        sender.sendMessage(getUsage());
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("activetowns.command.reload")) {
                completions.add("reload");
            }
            if (sender.hasPermission("activetowns.command.makesafe")) {
                completions.add("makesafe");
            }
            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("makesafe") && sender.hasPermission("activetowns.command.makesafe")) {
                completions.add("true");
                completions.add("false");
            }
            return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
        }

        return completions;
    }

    private void handleMakeSafe(Player player, @Nullable Boolean safe) {
        Town town = TownyAPI.getInstance().getTown(player.getLocation());
        if (town == null) {
            player.sendMessage(Component.text("Not standing in a town"));
            return;
        }

        if (safe == null) {
            if (plugin.getTownImmuneFromDeletion(town)) {
                player.sendMessage(Component.text("Town you're standing in is currently protected from automatic deletion."));
                return;
            }
            player.sendMessage(Component.text("Town you're standing in is currently NOT protected from automatic deletion."));
            return;
        }
        plugin.makeTownImmuneFromDeletion(town, safe);
        if (safe) {
            player.sendMessage(Component.text("Town %s is now protected from automatic deletion".formatted(town.getName())));
        } else {
            player.sendMessage(Component.text("Town %s is no longer protected from automatic deletion".formatted(town.getName())));
        }
    }
}
