package com.mineplex.studio.example.survivalgames.game.kit;

import com.mineplex.studio.example.survivalgames.game.ability.SneakyAbility;
import com.mineplex.studio.example.survivalgames.game.ability.WeightlessAbility;
import com.mineplex.studio.sdk.modules.game.MineplexGame;
import com.mineplex.studio.sdk.modules.game.mechanics.ability.Ability;
import com.mineplex.studio.sdk.modules.game.mechanics.ability.AbilityMechanic;
import com.mineplex.studio.sdk.modules.game.mechanics.kit.Kit;
import com.mineplex.studio.sdk.modules.game.mechanics.kit.KitMechanic;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * The PlayerKit represents a {@link Kit} for a {@link MineplexGame}.
 * A {@link Kit} is responsible for managing and assigning {@link Ability} to the player and other player kit interactions.
 */
@Getter
@RequiredArgsConstructor
public class PlayerKit implements Kit<MineplexGame> {
    /**
     * The {@link AbilityMechanic} is responsible for managing the game {@link com.mineplex.studio.sdk.modules.game.mechanics.ability.Ability}.
     */
    private final AbilityMechanic abilityMechanic;
    /**
     * The {@link KitMechanic} is responsible for managing the game {@link com.mineplex.studio.sdk.modules.game.mechanics.kit.Kit}.
     */
    private final KitMechanic kitMechanic;
    /**
     * The {@link MineplexGame} this ability is used in.
     */
    private MineplexGame game;

    /**
     * @return the name of this kit
     */
    @Override
    public @NotNull String getName() {
        return "Player";
    }

    /**
     * Method to be called when this kit is first set up for a {@link MineplexGame}
     * @param game the {@link MineplexGame} to set this kit up for
     */
    @Override
    public void setup(@NonNull final MineplexGame game) {
        this.game = game;

        // Setup Abilities used by this kit
        this.abilityMechanic.registerAbility(
                game, WeightlessAbility.class, new WeightlessAbility(this.abilityMechanic, game));
        this.abilityMechanic.registerAbility(game, SneakyAbility.class, new SneakyAbility(this.abilityMechanic, game));
    }

    /**
     * Method to be called when this ability instance is no longer needed
     */
    @Override
    public void teardown() {
        // Destroy Abilities used by this kit
        this.abilityMechanic.destroyAbility(WeightlessAbility.class);
        this.abilityMechanic.destroyAbility(SneakyAbility.class);
    }

    /**
     * Method called every time this kit is given to a {@link LivingEntity}
     * @param livingEntity the {@link LivingEntity} given this kit
     */
    @Override
    public void giveKit(@NonNull final LivingEntity livingEntity) {
        // Grant Abilities
        this.abilityMechanic.grantAbility(livingEntity, WeightlessAbility.class);
        this.abilityMechanic.grantAbility(livingEntity, SneakyAbility.class);

        // Apply gear
        final EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment != null) {
            equipment.setHelmet(ItemStack.of(Material.LEATHER_HELMET));
            equipment.setChestplate(ItemStack.of(Material.LEATHER_CHESTPLATE));
            equipment.setLeggings(ItemStack.of(Material.LEATHER_LEGGINGS));
            equipment.setBoots(ItemStack.of(Material.LEATHER_BOOTS));
            equipment.setItemInMainHand(ItemStack.of(Material.STONE_SWORD));
        }
    }

    /**
     * Method called when this kit is removed from a {@link LivingEntity}
     * @param livingEntity the {@link LivingEntity} this kit is removed from
     */
    @Override
    public void removeKit(@NonNull final LivingEntity livingEntity) {
        // Remove the Abilities from the entity
        this.abilityMechanic.removeAbility(livingEntity, WeightlessAbility.class);
        this.abilityMechanic.removeAbility(livingEntity, SneakyAbility.class);
    }
}
