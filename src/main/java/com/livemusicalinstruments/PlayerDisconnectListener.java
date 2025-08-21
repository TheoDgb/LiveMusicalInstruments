package com.livemusicalinstruments;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDisconnectListener implements Listener {

    private final LiveMusicalInstruments plugin;

    public PlayerDisconnectListener(LiveMusicalInstruments plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.removeActivePlayer(event.getPlayer().getName());
    }
}
