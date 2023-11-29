package com.mineplex.studio.example.survivalgames.game.stat;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * This enum represents the different types of {@link com.mineplex.studio.example.survivalgames.game.SurvivalGames} stats.
 */
@Getter
@AllArgsConstructor
public enum SurvivalGamesStats {
    WINS("Wins"),
    DEATHS("Deaths"),
    KILLS("Kills");

    /**
     * Represents the stat name.
     */
    private final String statName;
}
