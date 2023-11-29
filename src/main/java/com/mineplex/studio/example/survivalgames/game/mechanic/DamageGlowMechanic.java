package com.mineplex.studio.example.survivalgames.game.mechanic;

import com.mineplex.studio.sdk.modules.game.GameState;
import com.mineplex.studio.sdk.modules.game.MineplexGame;
import com.mineplex.studio.sdk.modules.game.PlayerState;
import com.mineplex.studio.sdk.modules.game.mechanics.GameMechanic;
import com.mineplex.studio.sdk.util.MinecraftTimeUnit;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * The DamageGlowMechanic class is responsible for adding a glowing effect to players when they take damage in a game.
 * It implements the GameMechanic interface, which allows it to be used as part of a game.
 */
@RequiredArgsConstructor
public class DamageGlowMechanic implements GameMechanic<MineplexGame> {
    /**
     * The duration of the {@link PotionEffectType#GLOWING} effect in ticks.
     */
    private static final int GLOW_DURATION_IN_TICKS = (int) MinecraftTimeUnit.SECONDS.toTicks(5);

    /**
     * The {@link JavaPlugin} the {@link DamageGlowMechanic} is created from.
     */
    private final JavaPlugin plugin;
    /**
     * The {@link MineplexGame} the {@link DamageGlowMechanic} is created from.
     */
    private MineplexGame game;

    /**
     * Method to be called when this mechanic is set up for a {@link MineplexGame}
     * @param game The {@link MineplexGame} setting up this mechanic
     */
    @Override
    public void setup(@NonNull final MineplexGame game) {
        this.game = game;

        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    /**
     * Method to be called when this mechanic is no longer needed by the host {@link MineplexGame}
     */
    @Override
    public void teardown() {
        HandlerList.unregisterAll(this);
    }

    /**
     * Method to handle damage events inflicted on {@link Player} during a {@link MineplexGame}.
     * It applies a {@link PotionEffect} on the damaged {@link Player} to make them glow for a certain duration.
     *
     * @param event The {@link EntityDamageEvent} instance that triggered this method call
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(final EntityDamageEvent event) {
        if (this.game.getGameState() != GameState.STARTED) {
            return;
        }

        if (!(event.getEntity() instanceof final Player player)) {
            return;
        }

        final PlayerState playerState = this.game.getPlayerState(player);
        if (!playerState.isAlive()) {
            return;
        }

        player.addPotionEffect(
                new PotionEffect(PotionEffectType.GLOWING, GLOW_DURATION_IN_TICKS, 0, false, false, false));
    }
}
