package com.mineplex.studio.example.survivalgames.game.ability;

import com.mineplex.studio.sdk.modules.game.MineplexGame;
import com.mineplex.studio.sdk.modules.game.mechanics.ability.Ability;
import com.mineplex.studio.sdk.modules.game.mechanics.ability.AbilityMechanic;
import com.mineplex.studio.sdk.modules.game.mechanics.ability.ActiveAbility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

/**
 * The SneakyAbility applies {@link PotionEffectType#INVISIBILITY} when a {@link Player} is sneaking.
 */
@Getter
@AllArgsConstructor
public class SneakyAbility implements ActiveAbility<MineplexGame> {
    /**
     * The {@link AbilityMechanic} is responsible for managing the game {@link com.mineplex.studio.sdk.modules.game.mechanics.ability.Ability}.
     */
    private final AbilityMechanic abilityMechanic;
    /**
     * The {@link MineplexGame} this ability is used in.
     */
    private final MineplexGame game;

    /**
     * Returns the name of the {@link com.mineplex.studio.sdk.modules.game.mechanics.ability.Ability}.
     *
     * @return the name of the ability
     */
    @Override
    public @NotNull String getName() {
        return "Sneaky";
    }

    /**
     * Method to be called when this ability is first set up for a {@link MineplexGame}
     * @param game the {@link MineplexGame} to set this ability up for
     */
    @Override
    public void setup(@NonNull final MineplexGame game) {
        // This ability doesn't need to allocate any additional resources
    }

    /**
     * Method to be called when this ability instance is no longer needed
     */
    @Override
    public void teardown() {
        // This ability has no additionally allocated resources to clean up
    }

    /**
     * Method called when this ability is first granted to a given {@link LivingEntity}
     * @param livingEntity the {@link LivingEntity} granted this ability
     */
    @Override
    public void start(@NonNull final LivingEntity livingEntity) {
        // This ability doesn't do anything to the host when it starts
    }

    /**
     * Method called when this ability is removed from a given {@link LivingEntity}
     * @param livingEntity the {@link LivingEntity} losing this ability
     */
    @Override
    public void stop(@NonNull final LivingEntity livingEntity) {
        livingEntity.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    /**
     * Method called when this ability activates
     * @param livingEntity The {@link LivingEntity} activating this {@link Ability}
     */
    @Override
    public void activate(final LivingEntity livingEntity) {
        livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10 * 20, 0, false, false, true));
    }

    /**
     * Method called when this ability deactivates
     * @param livingEntity The {@link LivingEntity} whose {@link Ability} should be deactivated
     */
    @Override
    public void deactivate(final LivingEntity livingEntity) {
        livingEntity.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    /**
     * Event handler for the {@link PlayerToggleSneakEvent} event
     * @param event The {@link PlayerToggleSneakEvent} event
     */
    @EventHandler
    public void onCrouch(final PlayerToggleSneakEvent event) {
        final Player player = event.getPlayer();
        // We need to verify if the player has the ability or not
        if (!this.hasAbility(player)) {
            return;
        }

        // We don't want to apply the ability if a player is respawning
        if (!this.game.getPlayerState(player).isAlive()) {
            return;
        }

        if (event.isSneaking()) {
            this.activate(player);
        } else {
            this.deactivate(player);
        }
    }
}
