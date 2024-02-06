package com.mineplex.studio.example.survivalgames.modules.manager.commands;

import com.mineplex.studio.example.survivalgames.modules.manager.ui.GameGUI;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link org.bukkit.command.Command} to open the {@link GameGUI}.
 */
public class GameCommand extends Command {
    /**
     * The name of the command.
     */
    private static final String COMMAND_NAME = "game";

    /**
     * The {@link com.mineplex.studio.sdk.gui.MineplexGUI} to be opened on command.
     */
    private final GameGUI gameGUI;

    public GameCommand(final GameGUI gameGUI) {
        super(COMMAND_NAME, "", String.format("/%s", COMMAND_NAME), List.of());

        this.gameGUI = gameGUI;
    }

    /**
     * Opens the {@link GameGUI} for the specified {@link Player}.
     */
    @Override
    public boolean execute(
            @NotNull final CommandSender commandSender, @NotNull final String s, @NotNull final String[] strings) {
        if (!(commandSender instanceof final Player player)) {
            return false;
        }

        this.gameGUI.createAndOpen(player);
        return true;
    }
}
