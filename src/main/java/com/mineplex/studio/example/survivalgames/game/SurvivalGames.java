package com.mineplex.studio.example.survivalgames.game;

import com.mineplex.studio.example.survivalgames.SurvivalGamesPlugin;
import com.mineplex.studio.example.survivalgames.game.kit.PlayerKit;
import com.mineplex.studio.example.survivalgames.game.listeners.SurvivalGamesListener;
import com.mineplex.studio.example.survivalgames.game.listeners.SurvivalGamesPreStartListener;
import com.mineplex.studio.example.survivalgames.game.listeners.SurvivalGamesStartedListener;
import com.mineplex.studio.example.survivalgames.game.loot.SurvivalGamesLootMechanic;
import com.mineplex.studio.example.survivalgames.game.mechanic.BorderMechanic;
import com.mineplex.studio.example.survivalgames.game.mechanic.DamageGlowMechanic;
import com.mineplex.studio.example.survivalgames.game.mechanic.HealingSoupMechanic;
import com.mineplex.studio.example.survivalgames.game.mechanic.TrackingCompassMechanic;
import com.mineplex.studio.example.survivalgames.game.stat.SurvivalGamesStats;
import com.mineplex.studio.sdk.modules.MineplexModuleManager;
import com.mineplex.studio.sdk.modules.game.*;
import com.mineplex.studio.sdk.modules.game.event.PlayerStateChangeEvent;
import com.mineplex.studio.sdk.modules.game.event.PostMineplexGameStateChangeEvent;
import com.mineplex.studio.sdk.modules.game.event.PreMineplexGameStateChangeEvent;
import com.mineplex.studio.sdk.modules.game.helper.GameStateTracker;
import com.mineplex.studio.sdk.modules.game.helper.PlayerStateTracker;
import com.mineplex.studio.sdk.modules.game.mechanics.GameWorldSelectorMechanic;
import com.mineplex.studio.sdk.modules.game.mechanics.ability.AbilityMechanic;
import com.mineplex.studio.sdk.modules.game.mechanics.helper.GameStateListenerHelperMechanic;
import com.mineplex.studio.sdk.modules.game.mechanics.kit.KitMechanic;
import com.mineplex.studio.sdk.modules.game.mechanics.legacy.*;
import com.mineplex.studio.sdk.modules.game.mechanics.spectator.SpectatorLocationHandler;
import com.mineplex.studio.sdk.modules.game.mechanics.spectator.SpectatorMechanic;
import com.mineplex.studio.sdk.modules.game.mechanics.spectator.SpectatorStateHandler;
import com.mineplex.studio.sdk.modules.game.mechanics.team.TeamMechanic;
import com.mineplex.studio.sdk.modules.game.mechanics.team.assigners.SingleTeamAssigner;
import com.mineplex.studio.sdk.modules.leaderboard.LeaderboardModule;
import com.mineplex.studio.sdk.modules.stats.StatsModule;
import com.mineplex.studio.sdk.modules.world.MineplexWorld;
import com.mineplex.studio.sdk.util.selector.BuiltInGameStateSelector;
import java.util.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
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
    /**
     * The key for the {@link MineplexWorld} center data points.
     */
    private static final String WORLD_CENTER_KEY = "CENTER";

    /**
     * The fallback center position if no data point was found.
     */
    private static final Vector FALLBACK_CENTER = new Vector(0, 0, 0);

    /**
     * The {@link JavaPlugin} the {@link MineplexGame} is created from.
     */
    private final JavaPlugin plugin;

    // Modules
    /**
     * The {@link MineplexGameMechanicFactory} is responsible for constructing {@link com.mineplex.studio.sdk.modules.game.mechanics.GameMechanic}.
     */
    private final MineplexGameMechanicFactory gameMechanicFactory =
            MineplexModuleManager.getRegisteredModule(MineplexGameMechanicFactory.class);
    /**
     * The {@link MineplexGameModule} is responsible for managing the {@link GameCycle}.
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
     * The {@link SpectatorMechanic} is responsible for managing game spec and respawn logic.
     */
    private SpectatorMechanic spectatorMechanic;
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
     * The {@link LegacyMechanic} manages all legacy {@link com.mineplex.studio.sdk.modules.game.mechanics.GameMechanic}.
     */
    private LegacyMechanic legacyMechanic;

    /**
     * The {@link DamageGlowMechanic} applies the {@link PotionEffectType#GLOWING} effect when the {@link Player} receives damages.
     */
    private DamageGlowMechanic damageGlowMechanic;
    /**
     * The {@link HealingSoupMechanic} applies a regeneration effect to a {@link Player} when consuming {@link org.bukkit.Material#MUSHROOM_STEW}.
     */
    private HealingSoupMechanic healingSoupMechanic;
    /**
     * The {@link TrackingCompassMechanic} adds a new {@link org.bukkit.inventory.ItemStack} that can be used by the {@link Player} to locate other game participants.
     */
    private TrackingCompassMechanic trackingCompassMechanic;
    /**
     * The {@link BorderMechanic} dynamically manages the {@link org.bukkit.WorldBorder} of the {@link MineplexWorld} depending on the selected world and {@link Player} count.
     */
    private BorderMechanic borderMechanic;

    /**
     * Utility to simplify the player state tracking.
     */
    @Delegate
    private final PlayerStateTracker playerStateTracker = new PlayerStateTracker(this, BuiltInPlayerState.SPECTATOR);

    /**
     * Sets the minimum number of players required for this {@link MineplexGame} to start.
     *
     * @param minPlayers the minimum number of players to set
     */
    @Setter
    private int minPlayers = 2;

    /**
     * Utility to simplify the game state tracking.
     */
    @Delegate
    private final GameStateTracker gameStateTracker = new GameStateTracker(this, BuiltInGameState.PREPARING);

    /**
     * Method triggered when the {@link GameState} is set to {@link GameState#isReady}.
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
     * Method triggered when the {@link GameState} is set to {@link GameState#isInProgress}.
     * <p>
     * This method performs the necessary actions to initialize the game after it has started.
     * It cleans up players, teleports them to their spawn points, sets their game mode to adventure,
     * grants them a kit, and assigns teams to players.
     */
    private void onStart() {
        // Move all players into the game and do the necessary modifications.
        final List<Location> spawns = new ArrayList<>(this.getSpawnLocations());
        Collections.shuffle(spawns);

        final Location center = this.getWorldCenter();

        this.getPlayerStates().keySet().forEach(player -> {
            // Reset hp, reset inventory, effects, etc...
            this.cleanupPlayer(player);

            // Teleport the player to a random spawn point
            final Location spawn = this.getLocationAwayFromOtherLocations(
                    spawns, this.getPlayerStates().keySet());
            // Fix spawn look before teleport
            this.adjustSpawnLocation(spawn, center);
            player.teleport(spawn, PlayerTeleportEvent.TeleportCause.PLUGIN);

            // Adjust player gamemode
            player.setGameMode(GameMode.ADVENTURE);

            // Assign kit to player
            this.getKitMechanic().grantKit(player, PlayerKit.class);
        });

        // Assign all alive players to the player team
        this.getTeamMechanic()
                .assignTeams(
                        new ArrayList<>(this.getPlayerStates().keySet()),
                        this.getTeamMechanic()
                                .constructTeamAssigner(SingleTeamAssigner.class)
                                .orElseThrow());
    }

    /**
     * Retrieves the world center location for the current {@link SingleWorldMineplexGame}.
     * If the data point for the world center is not found it will fallback to {@link SurvivalGames#FALLBACK_CENTER}.
     *
     * @return The world center location for the current {@link SingleWorldMineplexGame}.
     */
    private Location getWorldCenter() {
        final MineplexWorld world = this.getGameWorld();
        final List<Location> locations = world.getDataPoints(WORLD_CENTER_KEY);
        if (locations.isEmpty()) {
            log.warn("Missing {} data point key, falling back to fallback value.", WORLD_CENTER_KEY);
            return FALLBACK_CENTER.toLocation(world.getMinecraftWorld());
        }
        return locations.getFirst();
    }

    /**
     * Rotates the location to look at the center location.
     *
     * @param location the location to rotate
     * @param center   the center to look at
     */
    private void adjustSpawnLocation(final Location location, final Location center) {
        // Center location on block
        location.add(0.5, 0, 0.5);

        // Look at center location
        location.setDirection(
                center.toVector().setY(0).subtract(location.toVector().setY(0)).normalize());
    }

    /**
     * Find the best spawn locations for each player.
     *
     * @param locations spawn locations
     * @param players   players who can spawn
     * @return the best spawn location
     */
    private Location getLocationAwayFromOtherLocations(
            final Iterable<Location> locations, final Iterable<Player> players) {
        Location bestLocation = null;
        double bestDist = Double.MIN_VALUE;

        for (final Location location : locations) {
            double closest = Double.MAX_VALUE;

            for (final Player player : players) {
                // Different Worlds
                if (!player.getWorld().equals(location.getWorld())) {
                    continue;
                }

                final double distanceSquared = player.getLocation().distanceSquared(location);
                if (closest >= distanceSquared) {
                    closest = distanceSquared;
                }
            }

            if (closest > bestDist) {
                bestLocation = location;
                bestDist = closest;
            }
        }

        return bestLocation;
    }

    private List<Player> getAlivePlayers() {
        return this.getPlayerStates().entrySet().stream()
                .filter(entry -> entry.getValue().isAlive())
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Method triggered when the {@link GameState} is set to {@link GameState#isEnded}.
     * <p>
     * This method is responsible for handling the actions that occur when the game ends.
     * If there are no alive players, it sends a message to all online players indicating that there was no winner
     * and sets their game mode to spectator. If there is a winner, it sends a message to all online players
     * indicating who the winner was, sets their game mode to spectator, and awards them with win stats and leaderboard score.
     */
    private void onEnded() {
        final List<Player> alivePlayers = this.getAlivePlayers();
        // Check if a player is still alive or if the game ended without a winner
        if (alivePlayers.isEmpty()) {
            // Inform all players that the game has no winner
            for (final Player online : Bukkit.getOnlinePlayers()) {
                SurvivalGamesMessageComponent.GAME_END_NO_WINNER.send(online);
            }
        } else {
            final Player winner = alivePlayers.getFirst();

            // Inform all players of the winner
            for (final Player online : Bukkit.getOnlinePlayers()) {
                SurvivalGamesMessageComponent.GAME_END_HAS_WINNER.send(online, winner.displayName());
            }

            if (!SurvivalGamesPlugin.LOCAL_TESTING) {
                // Award stats to the winning player
                this.statsModule.awardPlayerStatsAsync(winner, Map.of(SurvivalGamesStats.WINS.getStatName(), 1L));
                this.leaderboardModule.incrementLeaderboardScoreAsync("Wins", winner, 1);
            }
        }

        // Start the next game
        MineplexModuleManager.getRegisteredModule(MineplexGameModule.class).startNextGame();
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
     * Setting up all {@link com.mineplex.studio.sdk.modules.game.mechanics.GameMechanic} for this game.
     */
    @Override
    public void setup() {
        //noinspection unchecked
        this.stateHelperMechanic = this.gameMechanicFactory.construct(GameStateListenerHelperMechanic.class);
        this.stateHelperMechanic
                // Function run once when the GameState changes to PRE_START
                .registerRunnable(this::onPreStart, BuiltInGameStateSelector.ready())
                // Function run once when the GameState changes to STARTED
                .registerSingleRunnable(this::onStart, BuiltInGameStateSelector.inProgress())
                // Function run once when the GameState changes to ENDED
                .registerSingleRunnable(this::onEnded, BuiltInGameStateSelector.ended())
                // Event listener that is listening during all GameStates
                .registerEventListener(new SurvivalGamesListener(this), state -> true)
                // Event listener that is listening during the PRE_START GameState
                .registerEventListener(new SurvivalGamesPreStartListener(this), BuiltInGameStateSelector.ready())
                // Event listener that is listening during the STARTED GameState
                .registerEventListener(new SurvivalGamesStartedListener(this), BuiltInGameStateSelector.inProgress());

        this.spectatorMechanic = this.gameMechanicFactory.construct(SpectatorMechanic.class);
        this.spectatorMechanic
                .getSettings()
                .setDeathOut(true)
                .setDropItemsOnDeath(true)
                .setDropItemsOnDisconnect(true);
        this.spectatorMechanic.setStateHandler(new SpectatorStateHandler() {
            @Override
            public void onSpectatorAdd(@NonNull final Player player, final boolean teleport, final boolean out) {
                SurvivalGames.this.setPlayerState(
                        player, out ? BuiltInPlayerState.ELIMINATED : BuiltInPlayerState.RESPAWNING);
            }

            @Override
            public void onPlayerRespawn(@NonNull final Player player) {
                SurvivalGames.this.setPlayerState(player, BuiltInPlayerState.ALIVE);
            }
        });
        this.spectatorMechanic.setLocationHandler(new SpectatorLocationHandler() {
            @Override
            public Location getSpectatorLocation(@NonNull final Player player) {
                return new Location(SurvivalGames.this.getGameWorld().getMinecraftWorld(), 0, 100, 0);
            }

            @Override
            public boolean shouldTeleport(@NonNull final Player player) {
                return false;
            }
        });
        this.spectatorMechanic.setup(this);

        this.legacyMechanic = this.gameMechanicFactory.construct(LegacyMechanic.class);

        this.gameWorldSelectorMechanic = this.gameMechanicFactory.construct(GameWorldSelectorMechanic.class);
        this.kitMechanic = this.gameMechanicFactory.construct(KitMechanic.class);
        this.abilityMechanic = this.gameMechanicFactory.construct(AbilityMechanic.class);
        this.teamMechanic = this.gameMechanicFactory.construct(TeamMechanic.class);
        this.damageGlowMechanic = new DamageGlowMechanic();
        this.healingSoupMechanic = new HealingSoupMechanic();
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
        this.teamMechanic.registerTeam("Players", SurvivalGamesMessageComponent.PLAYERS_TEAM_NAME.apply());

        this.kitMechanic.registerKit(this, PlayerKit.class, new PlayerKit(this.abilityMechanic, this.kitMechanic));

        this.damageGlowMechanic.setup(this);
        this.healingSoupMechanic.setup(this);
        this.legacyMechanic.setup(this);

        this.stateHelperMechanic.setup(this);

        // We need to indicate that we are ready after setting up all mechanics.
        this.setGameState(BuiltInGameState.PRE_START);
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

        this.healingSoupMechanic.teardown();
        this.damageGlowMechanic.teardown();
        this.kitMechanic.teardown();
        this.abilityMechanic.teardown();
        this.teamMechanic.teardown();
        this.lootContainerMechanic.teardown();
        this.gameWorldSelectorMechanic.teardown();
        this.trackingCompassMechanic.teardown();
        this.borderMechanic.teardown();
        this.legacyMechanic.teardown();
        this.spectatorMechanic.teardown();

        // Cleanup player data
        for (final Player player : this.getPlayerStates().keySet()) {
            this.cleanupPlayer(player);
        }
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
     * @param player              The player to be added to the game.
     * @param checkStartCondition Whether to check the game start condition or not.
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
     * required to start the game. If the condition is met, it sets the game state to {@link BuiltInGameState#STARTED}.
     */
    // TODO: The startup logic should not be here and more inside the lobby. Players should also only be added on
    // startup of the game.
    public void checkGameStartCondition() {
        final List<Player> alivePlayers = this.getAlivePlayers();
        log.debug("Game start check. Count: {} Required: {}", alivePlayers, this.minPlayers);
        if (alivePlayers.size() >= this.minPlayers) {
            this.setGameState(BuiltInGameState.STARTED);
        }
    }

    /**
     * This method checks if the number of alive {@link Player} in the game is less than or equal to 1.
     * If the condition is met, it sets the game state to {@link BuiltInGameState#ENDED}.
     */
    public void checkGameEndCondition() {
        final List<Player> alivePlayers = this.getAlivePlayers();
        log.debug("Game end check. Count: {}", alivePlayers);
        if (alivePlayers.size() <= 1) {
            this.setGameState(BuiltInGameState.ENDED);
        }
    }
}
