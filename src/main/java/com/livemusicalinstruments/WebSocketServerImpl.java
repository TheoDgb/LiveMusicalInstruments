package com.livemusicalinstruments;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketServerImpl extends WebSocketServer {

    private final LiveMusicalInstruments plugin;
    // WebSocket association with player name
    private final Map<WebSocket, String> connectionToPlayer = new ConcurrentHashMap<>();

    public WebSocketServerImpl(int port, LiveMusicalInstruments plugin) {
        super(new InetSocketAddress(port));
        this.plugin = plugin;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        plugin.getLogger().info("New WebSocket connection from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String playerName = connectionToPlayer.remove(conn);
        if (playerName != null) {
            plugin.removeActivePlayer(playerName);
            plugin.getLogger().info("Connection closed for " + playerName);
        }
    }

    public void closeConnectionForPlayer(String playerName) {
        WebSocket toClose = null;
        for (Map.Entry<WebSocket, String> entry : connectionToPlayer.entrySet()) {
            if (entry.getValue().equals(playerName)) {
                toClose = entry.getKey();
                break;
            }
        }
        if (toClose != null) {
            toClose.close(1000, "Player stopped or disconnected");
            connectionToPlayer.remove(toClose);
            plugin.getLogger().info("Closed WebSocket connection for player " + playerName);
        }
    }

    public void onMessage(WebSocket conn, String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String playerName = connectionToPlayer.get(conn);

            // Si pas de connexion associée à ce joueur
            if (playerName == null) {
                // Connexion non encore associée à un joueur
                if (json.has("player")) {
                    // Récupérer le pseudo du joueur et l'associer à cette connexion dans connectionToPlayer
                    playerName = json.get("player").getAsString();
                    if (playerName != null && !playerName.isEmpty()) {
                        connectionToPlayer.put(conn, playerName);
                        plugin.getLogger().info("Associated connection with player: " + playerName);

                        Player player = plugin.getServer().getPlayerExact(playerName);
                        if (player != null && player.isOnline()) {
                            player.sendMessage(ChatColor.GREEN + "Your device is connected! You can start playing drums.");
                        }
                    } else {
                        plugin.getLogger().warning("Received empty player name on new connection.");
                        return;
                    }
                }
                else {
                    plugin.getLogger().warning("Received message from unassociated connection without player info: " + message);
                    return;
                }
            } else { // Connexion déjà associée à un joueur
                if (json.has("player")) {
                    // Message "player" reçu de nouveau: on peut ignorer ou logger
                    plugin.getLogger().warning("Received duplicate player identification from " + playerName + ": " + message);
                    return;
                }
            }

            // playerName est non nul et connecté

            // Gestion des notes
            if (json.has("note")) {
                String instrument = plugin.getInstrumentForPlayer(playerName);
                String note = json.get("note").getAsString();
                int velocity = json.has("velocity") ? json.get("velocity").getAsInt() : -1;

                plugin.getLogger().info(playerName + " (" + instrument + "): Note received: " + note + " with velocity: " +  velocity);

                Player player = plugin.getServer().getPlayerExact(playerName);
                if (player != null && player.isOnline()) {
//                    Bukkit.getScheduler().runTask(plugin, () -> {
//                        InstrumentSoundManager.playSoundForPlayer(player, instrument, note, velocity);
//                    });

                     InstrumentSoundManager.playSoundForPlayer(player, instrument, note, velocity);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid message: " + message);
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        plugin.getLogger().severe("WebSocket error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        plugin.getLogger().info("WebSocket server started!");
    }
}