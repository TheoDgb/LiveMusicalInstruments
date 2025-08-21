package com.livemusicalinstruments;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class LiveMusicalInstruments extends JavaPlugin {

    private Integer webSocketPort;
    private WebSocketServerImpl wsServer;
    private final Map<String, String> activePlayers = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("LiveMusicInstruments enabled!");
        loadConfig();

        // Init InstrumentSoundManager
        InstrumentSoundManager.init(this);

        webSocketPort = getConfig().getInt("websocket-port");
        if (webSocketPort == 0) {
            getLogger().warning("config.yml error: The WebSocket port is not defined.");
        }

        Objects.requireNonNull(this.getCommand("live")).setExecutor(new Commands(this));

        // Managing player disconnection
        getServer().getPluginManager().registerEvents(new PlayerDisconnectListener(this), this);
    }

    @Override
    public void onDisable() {
        stopWebSocketIfRunning();
        getLogger().info("LiveMusicInstruments disabled!");
    }

    public void addActivePlayer(String playerName, String instrument) {
        activePlayers.put(playerName, instrument);
        if (wsServer == null) {
            startWebSocket();
        }
    }

    public String getInstrumentForPlayer(String playerName) {
        return activePlayers.get(playerName);
    }

    public void removeActivePlayer(String playerName) {
        activePlayers.remove(playerName);
        if (wsServer != null) {
            wsServer.closeConnectionForPlayer(playerName);
        }
        if (activePlayers.isEmpty()) {
            stopWebSocketIfRunning();
        }
    }

    private void startWebSocket() {
        if (wsServer != null) {
            getLogger().warning("WebSocket server is already running.");
            return;
        }
        wsServer = new WebSocketServerImpl(webSocketPort, this);
        new Thread(wsServer::start).start();
        getLogger().info("WebSocket server started on port " + webSocketPort);
    }

    private void stopWebSocketIfRunning() {
        if (wsServer != null) {
            try {
                wsServer.stop();
                wsServer = null;
                getLogger().info("WebSocket server stopped.");
            } catch (InterruptedException e) {
                getLogger().severe("Failed to stop WebSocket server: " + e.getMessage());
            }
        }
    }

    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        getDataFolder().mkdirs();

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                String configContent =
                        "# Configuration file for LiveMusicalInstruments plugin.\n" +
                                "\n" + "\n" +
                                "websocket-port: 3000\n" +
                                "\n" + "\n" +
                                "# Active drum kit (folder name)\n" +
                                "drum-kit: \"drum_kit_default\"\n" +
                                "\n" +
                                "drums:\n" +
                                "  kick:\n" +
                                "    velocities: 32\n" +
                                "    roundrobin: 8\n" +
                                "  snare:\n" +
                                "    velocities: 32\n" +
                                "    roundrobin: 2\n" +
                                "  side_stick:\n" +
                                "    velocities: 29\n" +
                                "    roundrobin: 4\n" +
                                "  tom_high:\n" +
                                "    velocities: 25\n" +
                                "    roundrobin: 4\n" +
                                "  tom_medium:\n" +
                                "    velocities: 30\n" +
                                "    roundrobin: 4\n" +
                                "  tom_low:\n" +
                                "    velocities: 26\n" +
                                "    roundrobin: 4\n" +
                                "  hi_hat_foot:\n" +
                                "    velocities: 31\n" +
                                "    roundrobin: 8\n" +
                                "  hi_hat_tight:\n" +
                                "    velocities: 49\n" +
                                "    roundrobin: 6\n" +
                                "  hi_hat_loose:\n" +
                                "    velocities: 40\n" +
                                "    roundrobin: 6\n" +
                                "  hi_hat_open:\n" +
                                "    velocities: 14\n" +
                                "    roundrobin: 4\n" +
                                "  crash_1:\n" +
                                "    velocities: 22\n" +
                                "    roundrobin: 4\n" +
                                "  crash_2:\n" +
                                "    velocities: 22\n" +
                                "    roundrobin: 4\n" +
                                "  ride_bow:\n" +
                                "    velocities: 23\n" +
                                "    roundrobin: 4\n" +
                                "  ride_bell:\n" +
                                "    velocities: 23\n" +
                                "    roundrobin: 4\n";
                try (FileWriter writer = new FileWriter(configFile)) {
                    writer.write(configContent);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}