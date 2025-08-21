package com.livemusicalinstruments;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor {

    private final LiveMusicalInstruments plugin;

    public Commands(LiveMusicalInstruments plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.GOLD + "Usage: " + ChatColor.YELLOW + "/live help");
            return true;
        }

        String action = args[0].toLowerCase();

        // Help command
        if (action.equals("help")) {
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "Usage of the command " + ChatColor.GOLD + "/live" + ChatColor.YELLOW + ":");
            player.sendMessage("");
            player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "Start / Stop playing an electronic instrument live:");
            player.sendMessage(ChatColor.YELLOW + "/live " + ChatColor.GOLD + "<" + ChatColor.YELLOW + "start" + ChatColor.GOLD + "|" + ChatColor.YELLOW + "stop" + ChatColor.GOLD + "> <" + ChatColor.YELLOW + "instrument_name" + ChatColor.GOLD + ">");
            player.sendMessage(ChatColor.YELLOW + "- drums");
            player.sendMessage(ChatColor.YELLOW + "- synth (NOT IMPLEMENTED)");
            player.sendMessage(ChatColor.YELLOW + "- electric_guitar (NOT IMPLEMENTED)");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Please specify an instrument.");
            return true;
        }

        String instrument = args[1].toLowerCase();

        if (!instrument.equals("drums")) {
            player.sendMessage(ChatColor.GOLD + "Usage: " + ChatColor.YELLOW + "/live help");
            return true;
        }

        if (action.equals("start")) {
            plugin.addActivePlayer(player.getName(), instrument);
            player.sendMessage(ChatColor.YELLOW + "You have started playing " + instrument + ". Waiting for your MIDI connection...");
            return true;
        }
        else if (action.equals("stop")) {
            plugin.removeActivePlayer(player.getName());
            player.sendMessage(ChatColor.GREEN + "You have stopped playing.");
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "Usage: " + ChatColor.YELLOW + "/live help");
        return true;
    }
}