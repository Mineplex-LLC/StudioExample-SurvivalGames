package com.mineplex.studio.example.survivalgames.game.mechanic;

import com.mineplex.studio.sdk.modules.game.GameState;
import com.mineplex.studio.sdk.modules.game.MineplexGame;
import com.mineplex.studio.sdk.modules.game.SingleWorldMineplexGame;
import com.mineplex.studio.sdk.modules.game.event.PlayerStateChangeEvent;
import com.mineplex.studio.sdk.modules.game.mechanics.GameMechanic;
import com.mineplex.studio.sdk.modules.game.mechanics.helper.GameStateListenerHelperMechanic;
import com.mineplex.studio.sdk.modules.world.MineplexWorld;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

/**
 * The {@link BorderMechanic} is responsible for managing the world {@link WorldBorder} of a {@link SingleWorldMineplexGame}.
 * It calculates the initial border size based on the distance between the world center and the border points and
 * dynamically adjusts the border size based on the number of alive players in the game.
 */
@RequiredArgsConstructor
@Slf4j
public class BorderMechanic implements GameMechanic<SingleWorldMineplexGame> {
    /**
     * The key for the {@link MineplexWorld} border data points.
     */
    private static final String WORLD_BORDER_KEY = "WORLD_BORDER";
    /**
     * The key for the {@link MineplexWorld} center data points.
     */
    private static final String WORLD_CENTER_KEY = "CENTER";

    /**
     * The fallback center position if no data point was found.
     */
    private static final Vector FALLBACK_CENTER = new Vector(0, 0, 0);

    /**
     * The fallback initial border size if no data point was found.
     */
    private static final int FALLBACK_INITIAL_BORDER = 256;

    /**
     * The {@link SingleWorldMineplexGame} the {@link BorderMechanic} is created from.
     * We need to use {@link SingleWorldMineplexGame} instead of {@link MineplexGame} because we require {@link SingleWorldMineplexGame#getGameWorld)}.
     */
    private SingleWorldMineplexGame game;
    /**
     * The {@link GameStateListenerHelperMechanic} is a helper class to register {@link GameState} based {@link org.bukkit.event.Listener} and {@link org.bukkit.scheduler.BukkitTask}.
     */
    private GameStateListenerHelperMechanic<SingleWorldMineplexGame> stateHelperMechanic;

    /**
     * The {@link WorldBorder} center.
     */
    @Getter
    private Location center;

    /**
     * The initial {@link WorldBorder} size during start of this mechanic.
     */
    private double initialBorder;
    /**
     * The initial {@link Player} count during start of this mechanic.
     */
    private double initialPlayers;

    /**
     * The rate at which the {@link WorldBorder} shrinks.
     */
    @Getter
    @Setter
    private int shrinkTimeRate = 60;

    /**
     * The alive {@link Player} rate at which the {@link WorldBorder} shrinks.
     */
    @Getter
    @Setter
    private int shrinkTimePlayerRate = 24;

    /**
     * The minimum radius of the {@link WorldBorder}.
     */
    @Getter
    @Setter
    private int minRadius = 10;

    /**
     * Method to be called when this mechanic is set up for a {@link MineplexGame}
     * @param game The {@link MineplexGame} setting up this mechanic
     */
    @Override
    public void setup(@NonNull final SingleWorldMineplexGame game) {
        this.game = game;

        this.center = this.getWorldCenter();
        this.initialBorder = this.calculateInitialBorder();

        //noinspection unchecked
        this.stateHelperMechanic = game.getGameMechanicFactory().construct(GameStateListenerHelperMechanic.class);

        this.stateHelperMechanic
                // Function run once when the GameState changes to STARTED
                .registerRunnable(this::setupBorder, GameState.STARTED)
                // Event listener that is listening during the STARTED GameState
                .registerEventListener(this, GameState.STARTED);

        this.stateHelperMechanic.setup(game);
    }

    /**
     * Method to be called when this mechanic is no longer needed by the host {@link MineplexGame}
     */
    @Override
    public void teardown() {
        this.stateHelperMechanic.teardown();

        // Reset border size
        if (this.game != null) {
            this.getWorldBorder().setSize(Integer.MAX_VALUE);
        }
    }

    /**
     * Retrieves the {@link WorldBorder} for the current {@link SingleWorldMineplexGame}.
     *
     * @return The world border for the current {@link SingleWorldMineplexGame}.
     */
    private WorldBorder getWorldBorder() {
        return this.game.getGameWorld().getMinecraftWorld().getWorldBorder();
    }

    /**
     * Retrieves the world center location for the current {@link SingleWorldMineplexGame}.
     * If the data point for the world center is not found it will fallback to {@link BorderMechanic#FALLBACK_CENTER}.
     *
     * @return The world center location for the current {@link SingleWorldMineplexGame}.
     */
    private Location getWorldCenter() {
        final MineplexWorld world = this.game.getGameWorld();
        final List<Location> locations = world.getDataPoints(WORLD_CENTER_KEY);
        if (locations.isEmpty()) {
            log.warn("Missing {} data point key, falling back to fallback value.", WORLD_CENTER_KEY);
            return FALLBACK_CENTER.toLocation(world.getMinecraftWorld());
        }
        return locations.getFirst();
    }

    /**
     * Retrieves the count of currently alive players in the game.
     *
     * @return The count of alive players.
     */
    private int getAlivePlayerCount() {
        int alive = 0;

        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (this.game.getPlayerState(player).isAlive()) {
                alive++;
            }
        }

        return alive;
    }

    /**
     * Calculates the initial border size for {@link SingleWorldMineplexGame}.
     * <p>
     * This method retrieves border data points with the key {@link BorderMechanic#WORLD_BORDER_KEY} of the {@link MineplexWorld}
     * and calculates the longest distance between those borders and the {@link this#center} of the game.
     * <p>
     * If there are no data points representing the borders {@link BorderMechanic#FALLBACK_INITIAL_BORDER} is returned.
     *
     * @return The initial border size.
     */
    private double calculateInitialBorder() {
        final MineplexWorld world = this.game.getGameWorld();

        final List<Location> borders = world.getDataPoints(WORLD_BORDER_KEY);
        if (borders.isEmpty()) {
            log.warn("Missing {} data point key, falling back to fallback value.", WORLD_BORDER_KEY);
            return FALLBACK_INITIAL_BORDER;
        }

        double longestDistance = Long.MIN_VALUE;
        for (final Location location : borders) {
            longestDistance = Math.max(longestDistance, Math.abs(location.getX() - this.center.getX()));
            longestDistance = Math.max(longestDistance, Math.abs(location.getZ() - this.center.getZ()));
        }
        return longestDistance * 2;
    }

    /**
     * Sets up the border for the game.
     * <p>
     * This method configures the border properties such as center, size, damage amount, damage buffer,
     * and warning distance
     */
    private void setupBorder() {
        final WorldBorder border = this.getWorldBorder();
        border.setCenter(this.center.getX(), this.center.getZ());
        border.setSize(this.initialBorder);
        border.setDamageAmount(0.1);
        border.setDamageBuffer(0);
        border.setWarningDistance(10);

        final int players = this.getAlivePlayerCount();
        this.initialPlayers = players;
        this.updateBorderSize(players);
    }

    /**
     * Updates the size of the {@link WorldBorder} based on the number of alivePlayers.
     *
     * @param alivePlayers the number of {@link Player} currently alive in the game
     */
    private void updateBorderSize(final int alivePlayers) {
        final WorldBorder border = this.getWorldBorder();

        border.setSize(border.getSize());
        border.setSize(this.minRadius, this.getBorderShrinkTime(alivePlayers));
    }

    /**
     * Calculates the shrink time for the {@link WorldBorder} based on the number of alivePlayers.
     *
     * @param alivePlayers the number of alivePlayers currently alive in the game
     * @return the shrink time of the WorldBorder in milliseconds
     */
    private long getBorderShrinkTime(final int alivePlayers) {
        final WorldBorder border = this.getWorldBorder();
        return (long) ((border.getSize() / this.initialBorder)
                * (alivePlayers * (this.shrinkTimePlayerRate / this.initialPlayers))
                * this.shrinkTimeRate);
    }

    /**
     * Updates the {@link WorldBorder} if a {@link Player} drops out of the {@link MineplexGame}.
     *
     * @param event the PlayerStateChangeEvent that triggered the method
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerDeathOut(final PlayerStateChangeEvent event) {
        if (!event.getToState().isAlive() && event.getToState().isGameParticipant()) {
            this.updateBorderSize(this.getAlivePlayerCount());
        }
    }
}
