package com.mineplex.studio.example.survivalgames.game.mechanic;

import com.mineplex.studio.sdk.modules.game.GameState;
import com.mineplex.studio.sdk.modules.game.MineplexGame;
import com.mineplex.studio.sdk.modules.game.mechanics.GameMechanic;
import com.mineplex.studio.sdk.modules.game.mechanics.helper.GameStateListenerHelperMechanic;
import com.mineplex.studio.sdk.util.MinecraftTimeUnit;
import lombok.NonNull;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * The {@link HealingSoupMechanic} gives the {@link Player} a temporary {@link PotionEffectType#REGENERATION} when consuming a {@link Material#MUSHROOM_STEW}.
 */
public class HealingSoupMechanic implements GameMechanic<MineplexGame> {
    /**
     * The {@link MineplexGame} the {@link HealingSoupMechanic} is created from.
     */
    private MineplexGame game;
    /**
     * The {@link GameStateListenerHelperMechanic} is a helper class to register {@link GameState} based {@link org.bukkit.event.Listener} and {@link org.bukkit.scheduler.BukkitTask}.
     */
    private GameStateListenerHelperMechanic<MineplexGame> stateHelperMechanic;

    /**
     * Method to be called when this mechanic is set up for a {@link MineplexGame}
     * @param game The {@link MineplexGame} setting up this mechanic
     */
    @Override
    public void setup(@NonNull final MineplexGame game) {
        this.game = game;

        //noinspection unchecked
        this.stateHelperMechanic = (GameStateListenerHelperMechanic<MineplexGame>) game.getGameModule()
                .constructGameMechanic(GameStateListenerHelperMechanic.class, game)
                .orElseThrow();

        // Event listener that is listening during the STARTED GameState
        this.stateHelperMechanic.registerEventListener(this, GameState.STARTED);
        this.stateHelperMechanic.setup(game);
    }

    /**
     * Method to be called when this mechanic is no longer needed by the host {@link MineplexGame}
     */
    @Override
    public void teardown() {
        this.stateHelperMechanic.teardown();
    }

    /**
     * Method to handle {@link Player} interaction with {@link Material#MUSHROOM_STEW}.
     *
     * @param event The PlayerInteractEvent triggered by the player's interaction.
     */
    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        // We don't want to trigger the stew when running over pressure plates
        if (event.getAction() == Action.PHYSICAL) {
            return;
        }

        // Check for the correct item
        final ItemStack itemStack = event.getItem();
        if (itemStack == null || itemStack.getType() != Material.MUSHROOM_STEW) {
            return;
        }

        // Check for the correct player state
        final Player player = event.getPlayer();
        if (!this.game.getPlayerState(player).isAlive()) {
            return;
        }

        // We need to cancel the even since we want to handle the food level here
        event.setCancelled(true);

        // Play sound and eating effects on use
        final Location effectLocation = player.getLocation().add(0, 1.3, 0);
        player.getWorld().playSound(effectLocation, Sound.ENTITY_GENERIC_EAT, 2f, 1f);
        player.getWorld().playEffect(effectLocation, Effect.STEP_SOUND, 39);
        player.getWorld().playEffect(effectLocation, Effect.STEP_SOUND, 40);

        // Apply regeneration effect to player
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.REGENERATION, (int) MinecraftTimeUnit.SECONDS.toTicks(4), 1, false, false, true));

        // Adjust player food level
        player.setFoodLevel(player.getFoodLevel() + 3);

        // Reduce item amount by 1
        itemStack.setAmount(itemStack.getAmount() - 1);
    }
}
