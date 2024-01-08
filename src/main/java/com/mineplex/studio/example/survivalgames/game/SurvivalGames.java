package com.mineplex.studio.example.survivalgames.game;

import com.mineplex.studio.example.survivalgames.SurvivalGamesI18nText;
import com.mineplex.studio.example.survivalgames.SurvivalGamesPlugin;
import com.mineplex.studio.example.survivalgames.game.kit.PlayerKit;
import com.mineplex.studio.example.survivalgames.game.listeners.SurvivalGamesListener;
import com.mineplex.studio.example.survivalgames.game.listeners.SurvivalGamesPreStartListener;
import com.mineplex.studio.example.survivalgames.game.listeners.SurvivalGamesStartedListener;
import com.mineplex.studio.example.survivalgames.game.loot.SurvivalGamesLootMechanic;
import com.mineplex.studio.example.survivalgames.game.mechanic.BorderMechanic;
import com.mineplex.studio.example.survivalgames.game.mechanic.DamageGlowMechanic;
import com.mineplex.studio.example.survivalgames.game.mechanic.TrackingCompassMechanic;
import com.mineplex.studio.example.survivalgames.game.stat.SurvivalGamesStats;
import com.mineplex.studio.sdk.i18n.I18nText;
import com.mineplex.studio.sdk.modules.MineplexModuleManager;
import com.mineplex.studio.sdk.modules.game.*;
import com.mineplex.studio.sdk.modules.game.event.PlayerStateChangeEvent;
import com.mineplex.studio.sdk.modules.game.event.PostMineplexGameStateChangeEvent;
import com.mineplex.studio.sdk.modules.game.event.PreMineplexGameStateChangeEvent;
import com.mineplex.studio.sdk.modules.game.mechanics.GameWorldSelectorMechanic;
import com.mineplex.studio.sdk.modules.game.mechanics.ability.AbilityMechanic;
import com.mineplex.studio.sdk.modules.game.mechanics.helper.GameStateListenerHelperMechanic;
import com.mineplex.studio.sdk.modules.game.mechanics.kit.KitMechanic;
import com.mineplex.studio.sdk.modules.game.mechanics.legacy.*;
import com.mineplex.studio.sdk.modules.game.mechanics.team.TeamMechanic;
import com.mineplex.studio.sdk.modules.game.mechanics.team.assigners.SingleTeamAssigner;
import com.mineplex.studio.sdk.modules.leaderboard.LeaderboardModule;
import com.mineplex.studio.sdk.modules.stats.StatsModule;
import com.mineplex.studio.sdk.modules.world.MineplexWorld;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

/**
 * The SurvivalGames game implementation.
 * Games are required to setup its own {@link com.mineplex.studio.sdk.modules.game.mechanics.GameMechanic} and call specific events:
 * - {@link PlayerStateChangeEvent}
 * - {@link PreMineplexGameStateChangeEvent}
 * - {@link PostMineplexGameStateChangeEvent}
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class SurvivalGames implements SingleWorldMineplexGame {
    // Messages
    /**
     * The {@link I18nText} for the player team name.
     */
    public static final I18nText PLAYERS_TEAM_NAME = new SurvivalGamesI18nText("GAME_TEAM_PLAYER", "Players");
    /**
     * The {@link I18nText} for the game win message without winners.
     * This message follows the {@link MiniMessage} format.
     */
    public static final I18nText NO_WINNER = new SurvivalGamesI18nText(
            "GAME_END_NO_WINNER", "<red>The game has ended with no winner. Better luck next time!</red>");
    /**
     * The {@link I18nText} for the game win message with winners.
     * This message follows the {@link MiniMessage} format.
     */
    public static final I18nText WINNER_WAS = new SurvivalGamesI18nText(
            "GAME_END_WINNER", "<b><yellow><winner></yellow></b> <green>has won the game!</green>");

    /**
     * The {@link JavaPlugin} the {@link MineplexGame} is created from.
     */
    private final JavaPlugin plugin;

    // Modules
    /**
     * The {@link MineplexGameModule} is responsible for managing the {@link GameCycle} and to construct new {@link GameCycle} for {@link MineplexGame}.
     */
    private final MineplexGameModule gameModule = MineplexModuleManager.getRegisteredModule(MineplexGameModule.class);
    /**
     * The {@link StatsModule} is responsible for saving and accessing {@link Player} stats.
     */
    private final StatsModule statsModule = MineplexModuleManager.getRegisteredModule(StatsModule.class);

    /**
     * The {@link LeaderboardModule} is responsible for saving and accessing leaderboards and {@link Player} leaderboard entries.
     */
    private final LeaderboardModule leaderboardModule =
            MineplexModuleManager.getRegisteredModule(LeaderboardModule.class);

    // Game mechanics
    /**
     * The {@link GameStateListenerHelperMechanic} is a helper class to register {@link GameState} based {@link org.bukkit.event.Listener} and {@link org.bukkit.scheduler.BukkitTask}.
     */
    private GameStateListenerHelperMechanic<SurvivalGames> stateHelperMechanic;
    /**
     * The {@link AbilityMechanic} is responsible for managing the game {@link com.mineplex.studio.sdk.modules.game.mechanics.ability.Ability}.
     */
    private AbilityMechanic abilityMechanic;
    /**
     * The {@link KitMechanic} is responsible for managing the game {@link com.mineplex.studio.sdk.modules.game.mechanics.kit.Kit}.
     */
    private KitMechanic kitMechanic;
    /**
     * The {@link GameWorldSelectorMechanic} is responsible selecting and loading {@link MineplexWorld} for our {@link MineplexGame}.
     */
    private GameWorldSelectorMechanic gameWorldSelectorMechanic;
    /**
     * The {@link SurvivalGamesLootMechanic} is responsible for filling our {@link org.bukkit.block.Container} with the pre-defined loot.
     */
    private SurvivalGamesLootMechanic lootContainerMechanic;
    /**
     * The {@link TeamMechanic} is responsible for managing {@link com.mineplex.studio.sdk.modules.game.mechanics.team.GameTeam} and assigning {@link Player} to those.
     */
    private TeamMechanic teamMechanic;

    /**
     * The {@link DamageGlowMechanic} applies the {@link PotionEffectType#GLOWING} effect when the {@link Player} receives damages.
     */
    private DamageGlowMechanic damageGlowMechanic;
    /**
     * The {@link TrackingCompassMechanic} adds a new {@link org.bukkit.inventory.ItemStack} that can be used by the {@link Player} to locate other game participants.
     */
    private TrackingCompassMechanic trackingCompassMechanic;
    /**
     * The {@link BorderMechanic} dynamically manages the {@link org.bukkit.WorldBorder} of the {@link MineplexWorld} depending on the selected world and {@link Player} count.
     */
    private BorderMechanic borderMechanic;

    private LegacyArmorMechanic legacyArmorMechanic;
    private LegacyBrewingStandMechanic legacyBrewingStandMechanic;
    private LegacyCriticalHitMechanic legacyCriticalHitMechanic;
    private LegacyDamageValuesMechanic legacyDamageValuesMechanic;
    private LegacyEnchantmentsMechanic legacyEnchantmentsMechanic;
    private LegacyEnchantmentTableMechanic legacyEnchantmentTableMechanic;
    private LegacyEnderPearlMechanic legacyEnderPearlMechanic;
    private LegacyKnockbackMechanic legacyKnockbackMechanic;
    private LegacyPlayerAttackSpeedMechanic legacyPlayerAttackSpeedMechanic;
    private LegacyProjectileMechanic legacyProjectileMechanic;
    private LegacyRegenerationMechanic legacyRegenerationMechanic;
    private LegacySweepingAttackMechanic legacySweepingAttackMechanic;

    /**
     * This {@link Map} maintains a mapping between {@link Player} objects and {@link PlayerState} objects.
     */
    private final Map<Player, PlayerState> players = new HashMap<>();
    /**
     * A {@link Set} containing all {@link Player} with the {@link PlayerState#isAlive}.
     */
    private final Set<Player> alivePlayers = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * Sets the minimum number of players required for this {@link MineplexGame} to start.
     *
     * @param minPlayers the minimum number of players to set
     */
    @Setter
    private int minPlayers = 2;

    /**
     * Represents the current {@link GameState} of this {@link MineplexGame} instance.
     */
    private GameState gameState = GameState.PREPARING;

    /**
     * Method triggered when the {@link GameState} is set to {@link GameState#PRE_START}.
     * <p>
     * This method adds all online players to the game and checks the game start condition.
     */
    private void onPreStart() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            this.addPlayer(player, false);
        }
        this.checkGameStartCondition();
    }

    /**
     * Method triggered when the {@link GameState} is set to {@link GameState#STARTED}.
     * <p>
     * This method performs the necessary actions to initialize the game after it has started.
     * It cleans up players, teleports them to their spawn points, sets their game mode to adventure,
     * grants them a kit, and assigns teams to players.
     *
     */
    private void onStart() {
        // Move all players into the game and do the necessary modifications.
        final List<Location> spawns = this.getSpawnLocations();
        this.players.keySet().forEach(player -> {
            // Reset hp, reset inventory, effects, etc...
            this.cleanupPlayer(player);

            // Teleport the player to a random spawn point
            // TODO: FIX DUPLICATING SELECTION
            final Location spawn = spawns.get(ThreadLocalRandom.current().nextInt(spawns.size()));
            player.teleport(spawn, PlayerTeleportEvent.TeleportCause.PLUGIN);

            // Adjust player gamemode
            player.setGameMode(GameMode.ADVENTURE);

            // Assign kit to player
            this.getKitMechanic().grantKit(player, PlayerKit.class);
        });

        // Assign all alive players to the player team
        this.getTeamMechanic()
                .assignTeams(
                        new ArrayList<>(this.players.keySet()),
                        this.getTeamMechanic()
                                .constructTeamAssigner(SingleTeamAssigner.class)
                                .orElseThrow());
    }

    /**
     * Method triggered when the {@link GameState} is set to {@link GameState#ENDED}.
     * <p>
     * This method is responsible for handling the actions that occur when the game ends.
     * If there are no alive players, it sends a message to all online players indicating that there was no winner
     * and sets their game mode to spectator. If there is a winner, it sends a message to all online players
     * indicating who the winner was, sets their game mode to spectator, and awards them with win stats and leaderboard score.
     * Finally, it sets the {@link GameState} to {@link GameState#CLEANING_UP}.
     *
     */
    private void onEnded() {
        // Check if a player is still alive or if the game ended without a winner
        if (this.alivePlayers.isEmpty()) {
            // Inform all players that the game has no winner
            for (final Player online : Bukkit.getOnlinePlayers()) {
                online.sendMessage(
                        MiniMessage.miniMessage().deserialize(SurvivalGames.NO_WINNER.getText(online.locale())));
                online.setGameMode(GameMode.SPECTATOR);
            }
        } else {
            final Player winner = this.alivePlayers.iterator().next();

            // Inform all players of the winner
            for (final Player online : Bukkit.getOnlinePlayers()) {
                online.sendMessage(MiniMessage.miniMessage()
                        .deserialize(
                                SurvivalGames.WINNER_WAS.getText(online.locale()),
                                Placeholder.parsed("winner", winner.getName())));

                online.setGameMode(GameMode.SPECTATOR);
            }

            if (!SurvivalGamesPlugin.LOCAL_TESTING) {
                // Award stats to the winning player
                this.statsModule.awardPlayerStatsAsync(winner, Map.of(SurvivalGamesStats.WINS.getStatName(), 1L));
                this.leaderboardModule.incrementLeaderboardScoreAsync("Wins", winner, 1);
            }
        }

        // Set the game to the next GameState to trigger additional game logics
        this.setGameState(GameState.CLEANING_UP);
    }

    /**
     * Retrieves the {@link MineplexWorld} for the current {@link MineplexGame} instance.
     *
     * @return The game world for the current game instance.
     */
    @Override
    public MineplexWorld getGameWorld() {
        return this.gameWorldSelectorMechanic.getSelectedGameWorld();
    }

    /**
     * Returns the name of the {@link MineplexGame}.
     *
     * @return the name of the game
     */
    @Override
    public @NotNull String getName() {
        return "Survival Games";
    }

    /**
     * Sets the current {@link GameState} of the {@link MineplexGame}.
     *
     * @param gameState the new state of the game
     */
    @Override
    public void setGameState(@NonNull final GameState gameState) {
        // The state did not change, nothing to do.
        if (this.gameState == gameState) {
            return;
        }

        // Call pre state event
        new PreMineplexGameStateChangeEvent(this, this.gameState, gameState).callEvent();
        // Create post state event
        final PostMineplexGameStateChangeEvent postEvent =
                new PostMineplexGameStateChangeEvent(this, this.gameState, gameState);
        // Change state
        this.gameState = gameState;
        // Call post state event
        postEvent.callEvent();
    }

    /**
     * Retrieves the {@link PlayerState} of the specified player in the game.
     *
     * @param player the player for whom to retrieve the state
     * @return the player's current state in the game, or {@link BuiltInPlayerState#SPECTATOR} if the player's state is not found
     */
    @Override
    public @NotNull PlayerState getPlayerState(final @NotNull Player player) {
        return this.players.getOrDefault(player, BuiltInPlayerState.SPECTATOR);
    }

    /**
     * Setting up all {@link com.mineplex.studio.sdk.modules.game.mechanics.GameMechanic} for this game.
     */
    @Override
    public void setup() {
        //noinspection unchecked
        this.stateHelperMechanic = (GameStateListenerHelperMechanic<SurvivalGames>) this.gameModule
                .constructGameMechanic(GameStateListenerHelperMechanic.class, this)
                .orElseThrow();
        this.stateHelperMechanic
                // Function run once when the GameState changes to PRE_START
                .registerRunnable(this::onPreStart, GameState.PRE_START)
                // Function run once when the GameState changes to STARTED
                .registerRunnable(this::onStart, GameState.STARTED)
                // Function run once when the GameState changes to ENDED
                .registerRunnable(this::onEnded, GameState.ENDED)
                // Event listener that is listening during all GameStates
                .registerEventListener(new SurvivalGamesListener(this), GameState.values())
                // Event listener that is listening during the PRE_START GameState
                .registerEventListener(new SurvivalGamesPreStartListener(this), GameState.PRE_START)
                // Event listener that is listening during the STARTED GameState
                .registerEventListener(new SurvivalGamesStartedListener(this), GameState.STARTED);

        this.legacyKnockbackMechanic = this.gameModule
                .constructGameMechanic(LegacyKnockbackMechanic.class, this)
                .orElseThrow();

        this.legacyBrewingStandMechanic = this.gameModule
                .constructGameMechanic(LegacyBrewingStandMechanic.class, this)
                .orElseThrow();

        this.legacyCriticalHitMechanic = this.gameModule
                .constructGameMechanic(LegacyCriticalHitMechanic.class, this)
                .orElseThrow();

        this.legacyDamageValuesMechanic = this.gameModule
                .constructGameMechanic(LegacyDamageValuesMechanic.class, this)
                .orElseThrow();

        this.legacyEnchantmentsMechanic = this.gameModule
                .constructGameMechanic(LegacyEnchantmentsMechanic.class, this)
                .orElseThrow();

        this.legacyEnchantmentTableMechanic = this.gameModule
                .constructGameMechanic(LegacyEnchantmentTableMechanic.class, this)
                .orElseThrow();

        this.legacyArmorMechanic = this.gameModule
                .constructGameMechanic(LegacyArmorMechanic.class, this)
                .orElseThrow();

        this.legacyRegenerationMechanic = this.gameModule
                .constructGameMechanic(LegacyRegenerationMechanic.class, this)
                .orElseThrow();

        this.legacyPlayerAttackSpeedMechanic = this.gameModule
                .constructGameMechanic(LegacyPlayerAttackSpeedMechanic.class, this)
                .orElseThrow();

        this.legacyProjectileMechanic = this.gameModule
                .constructGameMechanic(LegacyProjectileMechanic.class, this)
                .orElseThrow();

        this.legacyDamageValuesMechanic = this.gameModule
                .constructGameMechanic(LegacyDamageValuesMechanic.class, this)
                .orElseThrow();

        this.legacySweepingAttackMechanic = this.gameModule
                .constructGameMechanic(LegacySweepingAttackMechanic.class, this)
                .orElseThrow();

        this.legacyEnderPearlMechanic = this.gameModule
                .constructGameMechanic(LegacyEnderPearlMechanic.class, this)
                .orElseThrow();

        this.gameWorldSelectorMechanic = this.gameModule
                .constructGameMechanic(GameWorldSelectorMechanic.class, this)
                .orElseThrow();
        this.kitMechanic =
                this.gameModule.constructGameMechanic(KitMechanic.class, this).orElseThrow();
        this.abilityMechanic = this.gameModule
                .constructGameMechanic(AbilityMechanic.class, this)
                .orElseThrow();
        this.teamMechanic =
                this.gameModule.constructGameMechanic(TeamMechanic.class, this).orElseThrow();
        this.damageGlowMechanic = new DamageGlowMechanic(this.plugin);
        this.trackingCompassMechanic = new TrackingCompassMechanic(this.plugin);
        this.lootContainerMechanic = new SurvivalGamesLootMechanic(this.trackingCompassMechanic);
        this.borderMechanic = new BorderMechanic();

        // Blacklist the lobby from being selected as a game world.
        this.gameWorldSelectorMechanic.setFilter(name -> !"lobby".equalsIgnoreCase(name));
        // Determine the next map for ::getGameWorld
        this.gameWorldSelectorMechanic.setup(this);

        this.kitMechanic.setup(this);
        this.abilityMechanic.setup(this);
        this.trackingCompassMechanic.setup(this);
        this.borderMechanic.setup(this);
        this.lootContainerMechanic.setup(this);
        this.teamMechanic.setup(this);
        this.teamMechanic.registerTeam("Players", locale -> Component.text(PLAYERS_TEAM_NAME.getText(locale)));

        this.kitMechanic.registerKit(this, PlayerKit.class, new PlayerKit(this.abilityMechanic, this.kitMechanic));

        this.damageGlowMechanic.setup(this);

        // Legacy mechanics
        this.legacyArmorMechanic.setup(this);
        this.legacyBrewingStandMechanic.setup(this);
        this.legacyCriticalHitMechanic.setup(this);
        this.legacyDamageValuesMechanic.setup(this);
        this.legacyEnchantmentsMechanic.setup(this);
        this.legacyEnchantmentTableMechanic.setup(this);
        this.legacyEnderPearlMechanic.setup(this);
        this.legacyKnockbackMechanic.setup(this);
        this.legacyPlayerAttackSpeedMechanic.setup(this);
        this.legacyProjectileMechanic.setup(this);
        this.legacyRegenerationMechanic.setup(this);
        this.legacySweepingAttackMechanic.setup(this);

        this.stateHelperMechanic.setup(this);
    }

    /**
     * Perform the teardown process for this {@link MineplexGame}.
     * This method should be called when the game is ending or being reset.
     * It cleans up all the {@link com.mineplex.studio.sdk.modules.game.mechanics.GameMechanic} and clears all {@link Player}.
     */
    @Override
    public void teardown() {
        // Destroy game mechanics
        this.stateHelperMechanic.teardown();

        this.damageGlowMechanic.teardown();
        this.kitMechanic.teardown();
        this.abilityMechanic.teardown();
        this.teamMechanic.teardown();
        this.lootContainerMechanic.teardown();
        this.gameWorldSelectorMechanic.teardown();
        this.trackingCompassMechanic.teardown();
        this.borderMechanic.teardown();

        // Legacy mechanics
        this.legacyArmorMechanic.teardown();
        this.legacyBrewingStandMechanic.teardown();
        this.legacyCriticalHitMechanic.teardown();
        this.legacyDamageValuesMechanic.teardown();
        this.legacyEnchantmentsMechanic.teardown();
        this.legacyEnchantmentTableMechanic.teardown();
        this.legacyEnderPearlMechanic.teardown();
        this.legacyKnockbackMechanic.teardown();
        this.legacyPlayerAttackSpeedMechanic.teardown();
        this.legacyProjectileMechanic.teardown();
        this.legacyRegenerationMechanic.teardown();
        this.legacySweepingAttackMechanic.teardown();

        // Cleanup player data
        for (final Player player : this.players.keySet()) {
            this.cleanupPlayer(player);
        }

        this.players.clear();
        this.alivePlayers.clear();
    }

    /**
     * Set the {@link PlayerState} of a {@link Player} in the game to the specified new state.
     *
     * @param player    The player whose state is being set.
     * @param newState  The new state to set for the player.
     */
    public void setPlayerState(final Player player, @NonNull final PlayerState newState) {
        final PlayerState previousState = this.players.put(player, newState);

        if (newState.equals(previousState)) {
            return;
        }

        if (newState.isAlive()) {
            this.alivePlayers.add(player);
        } else {
            this.alivePlayers.remove(player);
        }

        new PlayerStateChangeEvent(player, this, previousState, newState).callEvent();
    }

    /**
     * Remove a {@link Player} from the game.
     *
     * @param player The player to be removed from the game.
     */
    public void removePlayer(final Player player) {
        this.players.remove(player);
    }

    /**
     * Clean up a {@link Player} after they have finished playing the game.
     * <p>
     * This method clears the inventory, health, effects, etc...
     *
     * @param player The player to be cleaned up.
     */
    public void cleanupPlayer(final Player player) {
        player.getInventory().clear();
        player.closeInventory(InventoryCloseEvent.Reason.DEATH);
        player.setExp(0);
        player.setFireTicks(0);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.clearActivePotionEffects();
        player.clearActiveItem();
        player.setFallDistance(0);
        player.setInvulnerable(false);
    }

    /**
     * Add a {@link Player} to the game.
     * <p>
     * This method sets the player's state to ALIVE and checks if the game start condition should be checked.
     *
     * @param player               The player to be added to the game.
     * @param checkStartCondition  Whether to check the game start condition or not.
     */
    public void addPlayer(final Player player, final boolean checkStartCondition) {
        this.setPlayerState(player, BuiltInPlayerState.ALIVE);

        if (checkStartCondition) {
            this.checkGameStartCondition();
        }
    }

    /**
     * Get the spawn locations in the game world.
     *
     * @return A list of spawn locations in the game world.
     */
    public List<Location> getSpawnLocations() {
        return this.getGameWorld().getDataPoints("SPAWN");
    }

    /**
     * This method checks if the number of alive {@link Player} in the game is equal to or greater than the minimum number of players
     * required to start the game. If the condition is met, it sets the game state to {@link GameState#STARTED}.
     */
    // TODO: The startup logic should not be here and more inside the lobby. Players should also only be added on
    // startup of the game.
    public void checkGameStartCondition() {
        log.debug("Game start check. Count: {} Required: {}", this.alivePlayers, this.minPlayers);
        if (this.alivePlayers.size() >= this.minPlayers) {
            this.setGameState(GameState.STARTED);
        }
    }

    /**
     * This method checks if the number of alive {@link Player} in the game is less than or equal to 1.
     * If the condition is met, it sets the game state to {@link GameState#ENDED}.
     */
    public void checkGameEndCondition() {
        log.debug("Game end check. Count: {}", this.alivePlayers);
        if (this.alivePlayers.size() <= 1) {
            this.setGameState(GameState.ENDED);
        }
    }
}
