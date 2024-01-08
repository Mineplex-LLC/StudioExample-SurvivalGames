package com.mineplex.studio.example.survivalgames.game.listeners;

import com.mineplex.studio.example.survivalgames.SurvivalGamesPlugin;
import com.mineplex.studio.example.survivalgames.game.SurvivalGames;
import com.mineplex.studio.example.survivalgames.game.kit.PlayerKit;
import com.mineplex.studio.example.survivalgames.game.stat.SurvivalGamesStats;
import com.mineplex.studio.sdk.modules.game.BuiltInPlayerState;
import com.mineplex.studio.sdk.modules.game.event.PlayerStateChangeEvent;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * The event {@link Listener} for {@link SurvivalGames} that is only active during {@link com.mineplex.studio.sdk.modules.game.GameState#STARTED}.
 */
@RequiredArgsConstructor
public class SurvivalGamesStartedListener implements Listener {
    /**
     * The game instance of {@link SurvivalGames}.
     */
    private final SurvivalGames game;

    /**
     * Event handler for the {@link PlayerDeathEvent} event.
     * Cancels the death of game participants and changes the player state to {@link BuiltInPlayerState#ELIMINATED}
     * @param event The {@link PlayerDeathEvent} event
     */
    @EventHandler
    public void onDeath(final PlayerDeathEvent event) {
        if (!this.game.getPlayerState(event.getPlayer()).isAlive()) {
            return;
        }

        event.setCancelled(true);
        // We need to delay this by a tick since we can't teleport a dead player
        Bukkit.getScheduler()
                .runTask(
                        this.game.getPlugin(),
                        () -> this.game.setPlayerState(event.getPlayer(), BuiltInPlayerState.ELIMINATED));
    }

    /**
     * Event handler for the {@link PlayerJoinEvent} event.
     * Adds the joined {@link org.bukkit.entity.Player} as a spectator to the game.
     * @param event The {@link PlayerJoinEvent} event
     */
    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        this.game.setPlayerState(event.getPlayer(), BuiltInPlayerState.SPECTATOR);
    }

    /**
     * Event handler for the {@link PlayerStateChangeEvent} event.
     * Handle {@link Player} logic when a player dies inside the game.
     * @param event The {@link PlayerStateChangeEvent} event
     */
    @EventHandler
    public void onPlayerStateChange(final PlayerStateChangeEvent event) {
        if (event.getToState().isAlive()) {
            return;
        }

        final Player player = event.getPlayer();

        this.game.cleanupPlayer(player);
        player.setGameMode(GameMode.SPECTATOR);

        // Eliminated Player
        if (event.getToState().isGameParticipant()) {
            this.game.getKitMechanic().removeKit(player, PlayerKit.class);

            if (!SurvivalGamesPlugin.LOCAL_TESTING) {
                this.game
                        .getStatsModule()
                        .awardPlayerStats(player, Map.of(SurvivalGamesStats.DEATHS.getStatName(), 1L));

                if (player.getKiller() != null) {
                    this.game
                            .getStatsModule()
                            .awardPlayerStatsAsync(
                                    player.getKiller(), Map.of(SurvivalGamesStats.KILLS.getStatName(), 1L));
                    this.game.getLeaderboardModule().incrementLeaderboardScoreAsync("Kills", player.getKiller(), 1);
                }
            }

            this.game.checkGameEndCondition();
        }
        // Spectator
        else {
            event.getPlayer().teleport(this.game.getSpawnLocations().getFirst());
        }
    }
}
