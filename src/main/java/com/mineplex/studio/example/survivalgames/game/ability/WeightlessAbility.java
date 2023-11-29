package com.mineplex.studio.example.survivalgames.game.ability;

import com.mineplex.studio.sdk.modules.game.MineplexGame;
import com.mineplex.studio.sdk.modules.game.mechanics.ability.Ability;
import com.mineplex.studio.sdk.modules.game.mechanics.ability.AbilityMechanic;
import com.mineplex.studio.sdk.modules.game.mechanics.ability.PassiveAbility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

/**
 * The WeightlessAbility cancels all fall damage the {@link LivingEntity} would receive.
 */
@AllArgsConstructor
@Getter
public class WeightlessAbility implements PassiveAbility<MineplexGame> {
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
        return "Weightless";
    }

    /**
     * Method to be called when this ability is first set up for a {@link MineplexGame}
     * @param game the {@link MineplexGame} to set this ability up for
     */
    @Override
    public void setup(final @NotNull MineplexGame game) {
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
    public void start(final @NotNull LivingEntity livingEntity) {
        // This ability doesn't do anything to the host when it starts
    }

    /**
     * Method called when this ability is removed from a given {@link LivingEntity}
     * @param livingEntity the {@link LivingEntity} losing this ability
     */
    @Override
    public void stop(final @NotNull LivingEntity livingEntity) {
        // This ability doesn't do anything to the host that needs to be removed
    }

    /**
     * A method called every tick that this {@link Ability} is active
     * @param livingEntity The {@link LivingEntity} using this {@link Ability}
     */
    @Override
    public void tick(final @NotNull LivingEntity livingEntity) {
        // This ability doesn't do anything on every tick
    }

    /**
     * Event handler for the {@link EntityDamageEvent} event
     * @param event The {@link EntityDamageEvent} event
     */
    @EventHandler
    public void onFallDamage(final EntityDamageEvent event) {
        // Verify if the damage was caused by fall
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        // Verify if the entity has the ability
        if (event.getEntity() instanceof final LivingEntity livingEntity && this.hasAbility(livingEntity)) {
            // Stop fall damage
            event.setCancelled(true);
        }
    }
}
