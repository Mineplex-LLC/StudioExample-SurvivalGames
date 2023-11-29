package com.mineplex.studio.example.survivalgames.game.listeners;

import com.mineplex.studio.example.survivalgames.game.SurvivalGames;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * The event {@link Listener} for {@link SurvivalGames} that is only active during {@link com.mineplex.studio.sdk.modules.game.GameState#PRE_START}.
 */
@RequiredArgsConstructor
public class SurvivalGamesPreStartListener implements Listener {
    /**
     * The game instance of {@link SurvivalGames}.
     */
    private final SurvivalGames game;

    /**
     * Event handler for the {@link PlayerJoinEvent} event.
     * Adds the joined {@link org.bukkit.entity.Player} to the game and checks if the game can be started.
     * @param event The {@link PlayerJoinEvent} event
     */
    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        this.game.addPlayer(event.getPlayer(), true);
    }
}
