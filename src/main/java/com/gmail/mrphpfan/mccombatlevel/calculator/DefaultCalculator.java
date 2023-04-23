package com.gmail.mrphpfan.mccombatlevel.calculator;

import com.gmail.mrphpfan.mccombatlevel.McCombatLevel;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;

import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import java.util.List;
import java.util.Optional;

import static com.magmaguy.elitemobs.adventurersguild.GuildRank.getActiveGuildRank;
import static com.magmaguy.elitemobs.adventurersguild.GuildRank.getGuildPrestigeRank;

public class DefaultCalculator implements LevelCalculator {

    //max of 1000 level
    private static final int MAX_LEVEL = 10000;

    @Override
    public int calculateLevel(PlayerProfile mcMMOProfile) {
        int swords = getLevel(mcMMOProfile, PrimarySkillType.SWORDS);
        int axes = getLevel(mcMMOProfile, PrimarySkillType.AXES);
        int unarmed = getLevel(mcMMOProfile, PrimarySkillType.UNARMED);
        int archery = getLevel(mcMMOProfile, PrimarySkillType.ARCHERY);
        int taming = getLevel(mcMMOProfile, PrimarySkillType.TAMING);
        int acrobatics = getLevel(mcMMOProfile, PrimarySkillType.ACROBATICS);
        int guildRank = getGuildRank(mcMMOProfile.getPlayerName());
        int elitePrestige = getElitePrestige(mcMMOProfile.getPlayerName());
        int threatTier = getThreatTier(mcMMOProfile.getPlayerName());
        int slimefun = getSlimefunLevel(mcMMOProfile.getPlayerName());

        // Log all of this to the console
//        McCombatLevel.inst().getLogger().info("Player: " + mcMMOProfile.getPlayerName());
//        McCombatLevel.inst().getLogger().info("Swords: " + swords);
//        McCombatLevel.inst().getLogger().info("Axes: " + axes);
//        McCombatLevel.inst().getLogger().info("Unarmed: " + unarmed);
//        McCombatLevel.inst().getLogger().info("Archery: " + archery);
//        McCombatLevel.inst().getLogger().info("Taming: " + taming);
//        McCombatLevel.inst().getLogger().info("Acrobatics: " + acrobatics);
//        McCombatLevel.inst().getLogger().info("Guild Rank: " + guildRank);
//        McCombatLevel.inst().getLogger().info("Elite Prestige: " + elitePrestige);
//        McCombatLevel.inst().getLogger().info("Threat Tier: " + threatTier);
//        McCombatLevel.inst().getLogger().info("Slimefun Level: " + getSlimefunLevel(mcMMOProfile.getPlayerName()));
//


        double sum = unarmed + swords + axes + archery + 0.25 * acrobatics + 0.25 * taming + 100 * guildRank * (elitePrestige + 1) + 30 * threatTier + 10 * slimefun;
        return NumberConversions.round(sum / 1.5);
    }

    private int getLevel(PlayerProfile mcMMOProfile, PrimarySkillType skillType) {
        int skillLevel = mcMMOProfile.getSkillLevel(skillType);
        return Math.min(skillLevel, MAX_LEVEL);
    }

    private int getGuildRank(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return 0;
        } else {
            // Get the player's guild level from elite mobs
            return getActiveGuildRank(player, true);
        }
    }

    private int getElitePrestige(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return 0;
        } else {
            // Get the player's elite prestige from elite mobs
            return getGuildPrestigeRank(player, true);
        }
    }

    private int getThreatTier(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return 0;
        } else {
            return ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getFullPlayerTier(true);
        }
    }

    private int getSlimefunLevel(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return 0;
        } else {
            try {
                io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile slimefunProfile = Slimefun.getRegistry().getPlayerProfiles().get(player.getUniqueId());
                List<String> titles = Slimefun.getRegistry().getResearchRanks();
                float fraction = (float) slimefunProfile.getResearches().size() / Slimefun.getRegistry().getResearches().size();
                return (int) (fraction * (titles.size() - 1));
            } catch(NullPointerException e){
                return 0;
            }

        }
    }


}
