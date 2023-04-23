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
        McCombatLevel.inst().getLogger().info("Player: " + mcMMOProfile.getPlayerName());
        McCombatLevel.inst().getLogger().info("Swords: " + swords);
        McCombatLevel.inst().getLogger().info("Axes: " + axes);
        McCombatLevel.inst().getLogger().info("Unarmed: " + unarmed);
        McCombatLevel.inst().getLogger().info("Archery: " + archery);
        McCombatLevel.inst().getLogger().info("Taming: " + taming);
        McCombatLevel.inst().getLogger().info("Acrobatics: " + acrobatics);
        McCombatLevel.inst().getLogger().info("Guild Rank: " + guildRank);
        McCombatLevel.inst().getLogger().info("Elite Prestige: " + elitePrestige);
        McCombatLevel.inst().getLogger().info("Threat Tier: " + threatTier);
        McCombatLevel.inst().getLogger().info("Slimefun Level: " + getSlimefunLevel(mcMMOProfile.getPlayerName()));



        double sum = unarmed + swords + axes + archery + 0.25 * acrobatics + 0.25 * taming + 100 * guildRank * (elitePrestige + 1) + 30 * threatTier + 10 * slimefun;
        return NumberConversions.round(sum / 1.5);
    }

    public static int getLevel(PlayerProfile mcMMOProfile, PrimarySkillType skillType) {
        int skillLevel = mcMMOProfile.getSkillLevel(skillType);
        return Math.min(skillLevel, MAX_LEVEL);
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
