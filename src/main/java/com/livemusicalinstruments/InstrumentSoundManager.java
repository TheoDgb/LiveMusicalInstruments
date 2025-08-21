package com.livemusicalinstruments;

import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class InstrumentSoundManager {
    private static final Map<String, Integer> roundRobinCounters = new HashMap<>();
    private static LiveMusicalInstruments plugin;

    public static void init(LiveMusicalInstruments pl) {
        plugin = pl;
    }

    private static String getActiveDrumKit() {
        return plugin.getConfig().getString("drum-kit", "drum_kit_default");
    }

    private static int getDrumsTotalVelocities(String note) {
        return plugin.getConfig().getInt("drums." + note + ".velocities", 1);
    }

    private static int getDrumsRoundRobinCount(String note) {
        return plugin.getConfig().getInt("drums." + note + ".roundrobin", 1);
    }

    private static int getNextRoundRobin(String note, int rrCount) {
        int current = roundRobinCounters.getOrDefault(note, 0) + 1;
        if (current > rrCount) current = 1;
        roundRobinCounters.put(note, current);
        return current;
    }

    public static void playSoundForPlayer(Player player, String instrument, String note, int velocity) {
        if (instrument == null) { return; }

        switch (instrument.toLowerCase()) {
            case "drums" -> playDrumSound(player, note, velocity);
//            case "synth" -> playSynthSound(player, note, velocity);
//            case "electric_guitar" -> playGuitarSound(player, note, velocity);
            default -> playDrumSound(player, note, velocity);
        }
    }

    public static void playDrumSound(Player player, String note, int velocity) {
        // Stop all hi_hat_open before playing the hi_hat_foot
        if (note.equals("hi_hat_foot")) {
            String[] openTypesToStop = { "hi_hat_open", "hi_hat_loose" };
            for (Player p : Bukkit.getOnlinePlayers()) {
                for (String openType : openTypesToStop) {
                    for (int rr = 1; rr <= getDrumsRoundRobinCount(openType); rr++) {
                        for (int vel = 1; vel <= getDrumsTotalVelocities(openType); vel++) {
                            String openSound = String.format("minecraft:live.drums.%s.%s.rr_%d.%s_%02d", getActiveDrumKit(), openType, rr, openType, vel);
                            p.stopSound(openSound, SoundCategory.AMBIENT);
                        }
                    }
                }
            }
        }

        int totalVelocities = getDrumsTotalVelocities(note);
        int rrCount = getDrumsRoundRobinCount(note);

        // Mapping vélocité -> index fichier
        int index = (int) Math.ceil((velocity / 127.0) * totalVelocities);
        if (index < 1) index = 1;
        if (index > totalVelocities) index = totalVelocities;

        // Round robin séquentiel
        int rrIndex = getNextRoundRobin(note, rrCount);

        // Nom complet du son
        String soundName = String.format("minecraft:live.drums.%s.%s.rr_%d.%s_%02d", getActiveDrumKit(), note, rrIndex, note, index);

        // Jouer
        // close to 0 latency but plays it only for yourself (IN AN EXTERNAL THREAD)
        // player.playSound(player.getLocation(), soundName, SoundCategory.AMBIENT, 4.0f, 1.0f);

        // a tiny bit of latency due to the server tickrate (20 per second) (NEED TO BE IN A BUKKIT THREAD)
        // player.getWorld().playSound(player.getLocation(), soundName, SoundCategory.AMBIENT, 4.0f, 1.0f);

        // close to 0 latency (IN AN EXTERNAL THREAD)
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(player.getLocation(), soundName, SoundCategory.AMBIENT, 4.0f, 1.0f);
        }
    }

//    private static void playSynthSound(Player player, String note, int velocity) {
//        String soundName = "minecraft:synth." + note;
//        player.playSound(player.getLocation(), soundName, 1.0f, 1.0f);
//    }

//    private static void playElectricGuitarSound(Player player, String note, int velocity) {
//        String soundName = "minecraft:electric_guitar." + note;
//        player.playSound(player.getLocation(), soundName, 1.0f, 1.0f);
//    }
}