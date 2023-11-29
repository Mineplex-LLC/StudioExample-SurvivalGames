package com.mineplex.studio.example.survivalgames.game.listeners;

import com.mineplex.studio.example.survivalgames.game.SurvivalGames;
import com.mineplex.studio.sdk.modules.game.BuiltInPlayerState;
import com.mineplex.studio.sdk.modules.game.PlayerState;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * The event {@link Listener} for {@link SurvivalGames} that is active during all {@link com.mineplex.studio.sdk.modules.game.GameState}.
 */
@RequiredArgsConstructor
public class SurvivalGamesListener implements Listener {
    /**
     * The game instance of {@link SurvivalGames}.
     */
    private final SurvivalGames game;

    /**
     * Event handler for the {@link PlayerQuitEvent} event.
     * Adds the joined {@link org.bukkit.entity.Player} to the game and checks if the game can be started.
     * @param event The {@link PlayerQuitEvent} event
     */
    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final PlayerState playerState = this.game.getPlayerState(player);

        // Trigger player state related logics
        if (!playerState.isGameParticipant()) {
            this.game.setPlayerState(player, BuiltInPlayerState.SPECTATOR);
        } else {
            this.game.setPlayerState(player, BuiltInPlayerState.ELIMINATED);
        }

        this.game.removePlayer(event.getPlayer());
    }
}
