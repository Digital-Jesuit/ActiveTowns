package me.youhavetrouble.activetowns;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ActiveTownsCommand extends Command {

    private final ActiveTowns plugin;

    protected ActiveTownsCommand(ActiveTowns plugin) {
        super(
                "activetownsreload",
                "Reloads active towns plugin",
                "/activetownsreload",
                new ArrayList<>()
        );
        this.plugin = plugin;
        setPermission("activetowns.reload");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!sender.hasPermission("activetowns.reload")) return false;
        plugin.reloadConfig();
        sender.sendMessage(Component.text("ActiveTowns reloaded. Now set to %s days".formatted(plugin.getDaysInactive())));
        return true;
    }
}
