package com.gmail.mrphpfan.mccombatlevel.tasks;

import com.gmail.mrphpfan.mccombatlevel.McCombatLevel;
import com.gmail.nossr50.datatypes.database.PlayerStat;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.NumberConversions;

public class LeaderboardReadTask implements Runnable {

    private final McCombatLevel plugin;
    private final CommandSender sender;
    private final int requestedPage;

    public LeaderboardReadTask(McCombatLevel plugin, CommandSender sender, int requestedPage) {
        this.plugin = plugin;
        this.sender = sender;
        this.requestedPage = requestedPage;
    }

    @Override
    public void run() {
        File file = new File(plugin.getDataFolder(), "leaderboardIndex.txt");
        if (file.exists()) {
            int startIndex = (requestedPage - 1) * 10 + 1;

            readLeaderboard(file, startIndex, startIndex + 10);
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "Leaderboard is not generated yet");
        }
    }

    private void readLeaderboard(File leaderboardFile, int startPos, int endPos) {
        BufferedReader reader = null;
        try {
            List<PlayerStat> results = Lists.newArrayListWithExpectedSize(startPos - endPos);
            int position = 1;

            try {
                plugin.getLeaderboardUpdateTask().getReadWriteLock().readLock().lock();
                reader = Files.newReader(leaderboardFile, Charsets.UTF_8);

                String line = reader.readLine();
                while (line != null && !line.isEmpty()) {
                    if (position >= startPos && position <= endPos) {
                        String[] components = line.split(":");
                        //first component is the uuid
                        String playerName = components[1];
                        String levelString = components[2];
                        int level = Integer.parseInt(levelString);

                        results.add(new PlayerStat(playerName, level));
                    }

                    line = reader.readLine();
                    position++;
                }
            } finally {
                plugin.getLeaderboardUpdateTask().getReadWriteLock().readLock().unlock();
            }

            int maxPages = NumberConversions.floor((double) position / 10);
            display(results, maxPages);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Error loading leaderboard", ex);
            sender.sendMessage(ChatColor.DARK_RED + "Error loading leaderboard");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    //ignore
                }
            }
        }
    }

    private void display(List<PlayerStat> results, int maxPages) {
        sender.sendMessage(ChatColor.DARK_GREEN + "=== Page " + requestedPage + " / " + maxPages + " ===");

        int rank = 10 * (requestedPage - 1);
        for (PlayerStat result : results) {
            String playerName = result.name;
            int combatLevel = result.statVal;

            sender.sendMessage(ChatColor.GOLD + "" + rank + ". " + playerName + " level: " + combatLevel);
            rank++;
        }
    }
}
