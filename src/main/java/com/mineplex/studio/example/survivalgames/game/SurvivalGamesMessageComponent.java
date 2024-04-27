package com.mineplex.studio.example.survivalgames.game;

import com.mineplex.studio.sdk.modules.i18n.MineplexMessageComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * This class contains all the message components for the Survival Games game.
 */
public class SurvivalGamesMessageComponent extends MineplexMessageComponent {
    /**
     * The team name for {@link com.mineplex.studio.sdk.modules.game.mechanics.team.GameTeam}.
     */
    public static final Args0 PLAYERS_TEAM_NAME = () -> Component.translatable("sg.team_name");

    /**
     * The win message with no winners.
     */
    public static final Args0 GAME_END_NO_WINNER =
            () -> withGamePrefix(Component.translatable("sg.game_end.no_winner").color(NamedTextColor.RED));
    /**
     * The win message with a winner.
     */
    public static final Args1<Component> GAME_END_HAS_WINNER = winnerName -> withGamePrefix(
            Component.translatable("sg.game_end.has_winner", element(winnerName).decorate(TextDecoration.BOLD))
                    .color(NamedTextColor.GREEN));
}
