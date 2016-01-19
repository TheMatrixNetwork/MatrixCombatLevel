package com.gmail.mrphpfan.mccombatlevel;

import java.util.Locale;
import org.bukkit.Bukkit;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class Effects {

    private static final boolean SPIGOT_SERVER = Bukkit.getVersion().contains("Spigot");

    public static Effects create(ConfigurationSection configSection) {
        boolean lightning = configSection.getBoolean("lightning");

        String effectType = configSection.getString("effect");
        Effect particleEffect = null;
        if (!effectType.isEmpty()) {
            try {
                particleEffect = Effect.valueOf(effectType.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException argumentException) {
                particleEffect = null;
            }
        }

        ConfigurationSection soundSection = configSection.getConfigurationSection("sound");
        String soundType = soundSection.getString("type");

        Sound sound;
        try {
            sound = Sound.valueOf(soundType.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException argumentException) {
            sound = null;
        }

        float pitch = (float) soundSection.getDouble("pitch");
        float volume = (float) soundSection.getDouble("volume");

        return new Effects(lightning, particleEffect, sound, pitch, volume);
    }

    private final boolean lightning;

    private final Effect particleEffect;

    private final Sound sound;
    private final float pitch;
    private final float volume;

    public Effects(boolean lightning, Effect particleEffect, Sound sound, float pitch, float volume) {
        this.lightning = lightning;
        this.particleEffect = particleEffect;

        this.sound = sound;
        this.pitch = pitch;
        this.volume = volume;
    }

    public void playEffect(Player player) {
        Location location = player.getLocation();
        if (lightning) {
            player.getWorld().strikeLightningEffect(location);
        }

        if (sound != null) {
            player.playSound(location, sound, volume, pitch);
        }

        if (particleEffect != null && SPIGOT_SERVER) {
            player.spigot().playEffect(location, particleEffect, 0, 0, 5, 5, 5, 2, 25, 5);
        }
    }
}
