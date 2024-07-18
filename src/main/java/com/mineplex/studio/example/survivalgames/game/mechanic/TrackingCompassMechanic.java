package com.mineplex.studio.example.survivalgames.game.mechanic;

import com.mineplex.studio.sdk.modules.game.MineplexGame;
import com.mineplex.studio.sdk.modules.game.mechanics.GameMechanic;
import com.mineplex.studio.sdk.util.MinecraftTimeUnit;
import it.unimi.dsi.fastutil.doubles.DoubleObjectPair;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The {@link TrackingCompassMechanic} adds a new {@link org.bukkit.inventory.ItemStack} that can be used by the {@link Player} to locate other game participants.
 */
@RequiredArgsConstructor
public class TrackingCompassMechanic implements GameMechanic<MineplexGame> {
    /**
     * Represents the key used for storing and retrieving information about usages of the tracking compass {@link ItemStack}.
     */
    private static final NamespacedKey USAGE_KEY = new NamespacedKey("tracking_compass_mechanic", "usages");

    /**
     * The {@link JavaPlugin} the {@link GameMechanic} is created from.
     */
    private final JavaPlugin plugin;

    /**
     * The {@link MineplexGame} this ability is used in.
     */
    @Getter
    private MineplexGame game;

    /**
     * Represents the {@link Material} of the tracking compass {@link ItemStack}.
     */
    @Getter
    @Setter
    @NonNull private Material itemMaterial = Material.COMPASS;

    /**
     * The cooldown of the tracking compass item in ticks.
     */
    @Getter
    @Setter
    private int cooldownInTicks = (int) MinecraftTimeUnit.SECONDS.toTicks(10);

    /**
     * A {@link BiPredicate} used to determine if a {@link Player} matches a target.
     * This is combined with a default predicate inside {@link this#createCombinedPredicate}
     */
    @Getter
    @Setter
    private BiPredicate<Player, Player> playerToTargetPredicate = null;

    /**
     * Method to be called when this mechanic is set up for a {@link MineplexGame}
     *
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
     * Method to handle {@link Player} interaction with compass {@link ItemStack}.
     *
     * @param event The PlayerInteractEvent triggered by the player's interaction.
     */
    @EventHandler
    public void interactCompass(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        // Check if the player is in state where he can use the item
        if (!this.canUse(player)) {
            return;
        }

        // Only trigger the mechanic on right click
        if (!event.getAction().isRightClick()) {
            return;
        }

        final EquipmentSlot hand = event.getHand();
        if (hand == null) {
            return;
        }

        final ItemStack itemStack = event.getItem();
        // TODO: Improve check. ItemStack::isSimilar(itemStack) is not an option here, since the lore contains the use
        // count.
        if (itemStack == null || itemStack.getType() != this.itemMaterial) {
            return;
        }

        if (player.hasCooldown(this.itemMaterial)) {
            return;
        }

        player.setCooldown(this.itemMaterial, this.cooldownInTicks);
        final int uses = this.getUses(itemStack);
        this.findClosetPlayer(player)
                .ifPresentOrElse(
                        target -> {
                            final int newUses = uses - 1;

                            player.setCompassTarget(target.right().getLocation());
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
                            TrackingCompassMessageComponent.FOUND_TARGET.send(
                                    player, target.right().displayName(), (int) target.firstDouble(), newUses);
                            final ItemStack newItem;
                            if (newUses <= 0) {
                                newItem = null;
                            } else {
                                newItem = this.createTrackingCompass(newUses);
                            }

                            player.getInventory().setItem(hand, newItem);
                        },
                        () -> TrackingCompassMessageComponent.NO_TARGET.send(player));
    }

    /**
     * Method to handle combining two compasses in the player's inventory.
     *
     * @param event The InventoryClickEvent triggered by the player's inventory click.
     */
    @EventHandler
    public void combineCompasses(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof final Player player)) {
            return;
        }

        // Check if the player is in state where he can use the item
        if (!this.canUse(player)) {
            return;
        }

        final ItemStack cursor = event.getCursor();
        final ItemStack currentItem = event.getCurrentItem();

        // Verify that both items are valid.
        if (currentItem == null
                || cursor.getType() != this.itemMaterial
                || currentItem.getType() != this.itemMaterial) {
            return;
        }

        final int usesOne = this.getUses(cursor);
        final int usesTwo = this.getUses(currentItem);
        final ItemStack newItemStack = this.createTrackingCompass(usesOne + usesTwo);

        event.setCursor(newItemStack);
        event.setCurrentItem(null);

        TrackingCompassMessageComponent.COMBINE.send(player);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
    }

    /**
     * Method to create a combined predicate for comparing two players.
     *
     * @return a BiPredicate<Player, Player> that represents the combined predicate.
     */
    private BiPredicate<Player, Player> createCombinedPredicate() {
        BiPredicate<Player, Player> predicate = (pl, other) -> {
            // Don't target the player itself
            if (pl.equals(other)) {
                return false;
            }

            // Never target a player who is not alive
            return this.getGame().getPlayerState(other).isAlive();
        };

        // Add user defined predicate
        if (this.playerToTargetPredicate != null) {
            predicate = predicate.and(this.playerToTargetPredicate);
        }

        return predicate;
    }

    /**
     * Method to find the closest {@link Player} to a given {@link Player}.
     *
     * @param player the player for whom to find the closest player.
     * @return an Optional containing a {@link DoubleObjectPair} representing the distance and the closest player, or an empty Optional if no closest player is found.
     */
    private Optional<DoubleObjectPair<Player>> findClosetPlayer(final Player player) {
        final BiPredicate<Player, Player> predicate = this.createCombinedPredicate();

        Player target = null;
        double distance = Double.MAX_VALUE;
        for (final Player other : player.getWorld().getPlayers()) {
            if (!predicate.test(player, other)) {
                continue;
            }

            final double playerDistance = player.getLocation().distance(other.getLocation());
            if (distance > playerDistance) {
                distance = playerDistance;
                target = other;
            }
        }

        if (target == null) {
            return Optional.empty();
        }

        return Optional.of(DoubleObjectPair.of(distance, target));
    }

    /**
     * Determines whether a given player can use the tracking compass.
     *
     * @param player the player to check.
     * @return true if the player can use the feature, false otherwise.
     */
    private boolean canUse(final Player player) {
        return this.game.getPlayerState(player).isAlive();
    }

    /**
     * Retrieves the number of uses of a given item stack.
     *
     * @param itemStack the item stack to get the uses from.
     * @return the number of uses of the item stack, or 0 if the item stack does not have item meta.
     */
    private int getUses(final ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) {
            return 0;
        }

        final ItemMeta itemMeta = itemStack.getItemMeta();
        return itemMeta.getPersistentDataContainer().getOrDefault(USAGE_KEY, PersistentDataType.INTEGER, 0);
    }

    /**
     * Creates a tracking compass item with the specified number of uses.
     *
     * @param uses the number of uses for the tracking compass.
     * @return the created tracking compass item.
     */
    public ItemStack createTrackingCompass(final int uses) {
        final ItemStack itemStack = ItemStack.of(this.itemMaterial);
        itemStack.editMeta(itemMeta -> {
            itemMeta.displayName(TrackingCompassMessageComponent.ITEM_NAME.apply());

            itemMeta.lore(List.of(
                    TrackingCompassMessageComponent.ITEM_LORE_0.apply(uses),
                    Component.empty(),
                    TrackingCompassMessageComponent.ITEM_LORE_1.apply(),
                    TrackingCompassMessageComponent.ITEM_LORE_2.apply(),
                    TrackingCompassMessageComponent.ITEM_LORE_3.apply(),
                    TrackingCompassMessageComponent.ITEM_LORE_4.apply()));

            itemMeta.getPersistentDataContainer().set(USAGE_KEY, PersistentDataType.INTEGER, uses);
        });

        return itemStack;
    }

    /**
     * Sets the cooldown for the specified time period.
     *
     * @param cooldown the cooldown duration.
     * @param timeUnit the unit of time for the cooldown
     */
    public void setCooldown(final int cooldown, final MinecraftTimeUnit timeUnit) {
        this.setCooldownInTicks((int) timeUnit.toTicks(cooldown));
    }
}
