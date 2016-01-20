package com.gmail.mrphpfan.mccombatlevel.tasks;

import com.gmail.mrphpfan.mccombatlevel.McCombatLevel;
import com.gmail.nossr50.datatypes.database.PlayerStat;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import org.bukkit.entity.Player;

public class LeaderboardUpdateTask implements Runnable {

    private final McCombatLevel plugin;

    private final ConcurrentMap<UUID, PlayerStat> toSave = Maps.newConcurrentMap();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public LeaderboardUpdateTask(McCombatLevel plugin) {
        this.plugin = plugin;
    }

    public void addToSave(Player player, int level) {
        UUID playerUUID = player.getUniqueId();

        synchronized (this) {
            toSave.put(playerUUID, new PlayerStat(player.getName(), level));
        }
    }

    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    @Override
    public void run() {
        if (toSave.isEmpty()) {
            return;
        }

        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            File tempFile = File.createTempFile("temp_", "", plugin.getDataFolder());
            File originalFile = new File(plugin.getDataFolder(), "leaderboardIndex.txt");
            if (!originalFile.exists()) {
                originalFile.createNewFile();
            }

            reader = Files.newReader(originalFile, Charsets.UTF_8);
            writer = Files.newWriter(tempFile, Charsets.UTF_8);

            updateExistingEntries(reader, writer);
            appendNewEntries(writer);

            toSave.clear();

            try {
                readWriteLock.writeLock().lock();

                originalFile.delete();
                tempFile.renameTo(originalFile);
            } finally {
                readWriteLock.writeLock().unlock();
            }
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Error saving leaderboard", ex);
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException ex) {
                    //ignore
                }
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    //ignore
                }
            }
        }
    }

    private void updateExistingEntries(BufferedReader reader, BufferedWriter writer) throws IOException {
        String line = reader.readLine();
        while (line != null && !line.isEmpty()) {
            String uuidString = line.substring(0, line.indexOf(':'));
            UUID savedUUID = UUID.fromString(uuidString);

            PlayerStat playerStats = toSave.remove(savedUUID);
            if (playerStats != null) {
                writer.append(uuidString);
                writer.append(':');
                writer.append(playerStats.name);
                writer.append(':');
                writer.append(Integer.toString(playerStats.statVal));
                writer.newLine();
            } else {
                writer.append(line);
            }

            line = reader.readLine();
        }
    }

    private void appendNewEntries(BufferedWriter writer) throws IOException {
        ImmutableMap<UUID, PlayerStat> copyToSave;
        synchronized (this) {
            //require a lock for changes with new add requests
            copyToSave = ImmutableMap.copyOf(toSave);
            toSave.clear();
        }

        for (Map.Entry<UUID, PlayerStat> entry : copyToSave.entrySet()) {
            UUID uuid = entry.getKey();
            PlayerStat value = entry.getValue();

            writer.append(uuid.toString());
            writer.append(':');
            writer.append(value.name);
            writer.append(':');
            writer.append(Integer.toString(value.statVal));
            writer.newLine();
        }
    }
}
