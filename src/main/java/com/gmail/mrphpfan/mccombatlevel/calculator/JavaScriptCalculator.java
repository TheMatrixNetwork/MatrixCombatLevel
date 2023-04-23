package com.gmail.mrphpfan.mccombatlevel.calculator;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.gmail.mrphpfan.mccombatlevel.McCombatLevel;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.skills.data.managers.SkilledPlayer;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static com.magmaguy.elitemobs.adventurersguild.GuildRank.getActiveGuildRank;
import static com.magmaguy.elitemobs.adventurersguild.GuildRank.getGuildPrestigeRank;
import static java.util.stream.Collectors.toMap;

public class JavaScriptCalculator implements LevelCalculator {

    private final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
    private final String formula;

    public JavaScriptCalculator(String formula) {
        this.formula = formula;
    }

    //max of 1000 level
    private static final int MAX_LEVEL = 10000;

    @Override
    public int calculateLevel(PlayerProfile mcMMOProfile) {
        int guildRank = getGuildRank(mcMMOProfile.getPlayerName());
        int elitePrestige = getElitePrestige(mcMMOProfile.getPlayerName());
        int threatTier = getThreatTier(mcMMOProfile.getPlayerName());
        int slimefun = getSlimefunLevel(mcMMOProfile.getPlayerName());

        double sum = getMcMMOLevel(mcMMOProfile) + 100 * guildRank * (elitePrestige + 1) + 30 * threatTier + 10 * slimefun;
        return NumberConversions.round(sum / 1.5);
    }

    public static int getLevel(PlayerProfile mcMMOProfile, PrimarySkillType skillType) {
        int skillLevel = mcMMOProfile.getSkillLevel(skillType);
        return Math.min(skillLevel, MAX_LEVEL);
    }

    public static double getMcMMOLevel(PlayerProfile mcMMOProfile) {
        int swords = getLevel(mcMMOProfile, PrimarySkillType.SWORDS);
        int axes = getLevel(mcMMOProfile, PrimarySkillType.AXES);
        int unarmed = getLevel(mcMMOProfile, PrimarySkillType.UNARMED);
        int archery = getLevel(mcMMOProfile, PrimarySkillType.ARCHERY);
        int taming = getLevel(mcMMOProfile, PrimarySkillType.TAMING);
        int acrobatics = getLevel(mcMMOProfile, PrimarySkillType.ACROBATICS);
        return unarmed + swords + axes + archery + 0.25 * acrobatics + 0.25 * taming + 100;
    }

    public static int getGuildRank(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return 0;
        } else {
            // Get the player's guild level from elite mobs
            return getActiveGuildRank(player, true);
        }
    }

    public static int getSkillLevel(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return 0;
        } else {
            return SkilledPlayer.getSkilledPlayer(player).getLevel();
        }
    }

    public static String getSkillClass(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return "Human";
        } else {
            return SkilledPlayer.getSkilledPlayer(player).getSkillName();
        }
    }

    public static Map<String, Integer> getSkillStats(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return null;
        } else {
            return SkilledPlayer.getSkilledPlayer(player).getStats();
        }
    }

    public static String getGuildRankName(String playerName) {
        return GuildRank.getRankName(getElitePrestige(playerName), getGuildRank(playerName));
    }



    public static int getElitePrestige(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return 0;
        } else {
            // Get the player's elite prestige from elite mobs
            return getGuildPrestigeRank(player, true);
        }
    }

    public static int getThreatTier(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return 0;
        } else {
            return ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getFullPlayerTier(true);
        }
    }

    public static int getSlimefunLevel(String playerName) {
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

    public static String getSlimefunTitle(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return "N/A";
        } else {
            try {
                io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile slimefunProfile = Slimefun.getRegistry().getPlayerProfiles().get(player.getUniqueId());
                List<String> titles = Slimefun.getRegistry().getResearchRanks();
                float fraction = (float) slimefunProfile.getResearches().size() / Slimefun.getRegistry().getResearches().size();
                return titles.get((int) (fraction * (titles.size() - 1)));
            } catch(NullPointerException e){
                return "N/A";
            }

        }
    }
//    @Override
//    public int calculateLevel(PlayerProfile mcMMOProfile) {
//        Bindings variables = scriptEngine.createBindings();
//
//        Map<String, Integer> collect = Stream.of(PrimarySkillType.values())
//                .collect(toMap(skill -> skill.name().toLowerCase(), mcMMOProfile::getSkillLevel));
//        variables.putAll(collect);
//
//        try {
//            Object result = scriptEngine.eval(formula, variables);
//            if (result instanceof Number) {
//                return ((Number) result).intValue();
//            } else {
//                throw new RuntimeException("Formula doesn't returned a number");
//            }
//        } catch (ScriptException ex) {
//            throw new RuntimeException("Combat level cannot be calculated", ex.getCause());
//        }
//    }

    public boolean isScriptEnabled() {
        return scriptEngine != null;
    }
}
