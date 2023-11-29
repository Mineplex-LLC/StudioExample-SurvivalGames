package com.mineplex.studio.example.survivalgames.modules.manager.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.mineplex.studio.example.survivalgames.modules.manager.ui.GameGUI;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

/**
 * Represents a {@link org.bukkit.command.Command} to open the {@link GameGUI}.
 */
@RequiredArgsConstructor
@CommandAlias("game")
public class GameCommand extends BaseCommand {
    /**
     * The {@link com.mineplex.studio.sdk.gui.MineplexGUI} to be opened on command.
     */
    private final GameGUI gameGUI;

    /**
     * Opens the {@link GameGUI} for the specified {@link Player}.
     *
     * @param player the player who triggered the command
     */
    @Default
    @Description("Open the game menu.")
    public void onCommand(final Player player) {
        this.gameGUI.createAndOpen(player);
    }
}
