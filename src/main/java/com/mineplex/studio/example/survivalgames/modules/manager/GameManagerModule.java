package com.mineplex.studio.example.survivalgames.modules.manager;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import com.mineplex.studio.example.survivalgames.modules.manager.commands.GameCommand;
import com.mineplex.studio.example.survivalgames.modules.manager.ui.GameGUI;
import com.mineplex.studio.example.survivalgames.modules.worlddemo.WorldDemoModule;
import com.mineplex.studio.sdk.gui.MineplexGUI;
import com.mineplex.studio.sdk.modules.MineplexModule;
import com.mineplex.studio.sdk.modules.MineplexModuleManager;
import com.mineplex.studio.sdk.modules.game.GameCycle;
import com.mineplex.studio.sdk.modules.game.GameState;
import com.mineplex.studio.sdk.modules.game.MineplexGame;
import com.mineplex.studio.sdk.modules.game.MineplexGameModule;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A demo {@link MineplexModule} showing a use case of {@link MineplexGUI} to stop the running game.
 */
@RequiredArgsConstructor
public class GameManagerModule implements MineplexModule {
    /**
     * The {@link JavaPlugin} the {@link MineplexModule} is created from.
     */
    private final JavaPlugin plugin;

    // Modules
    /**
     * The {@link MineplexGameModule} is responsible for managing the {@link GameCycle} and to construct new {@link GameCycle} for {@link MineplexGame}.
     */
    private MineplexGameModule gameModule;

    /**
     * Manages register and unregister of {@link Command}.
     */
    private PaperCommandManager commandManager;
    /**
     * Command to control {@link WorldDemoModule}.
     */
    private BaseCommand command;

    /**
     * Method called to allocate any additional resources this module uses
     */
    @Override
    public void setup() {
        this.gameModule = MineplexModuleManager.getRegisteredModule(MineplexGameModule.class);

        // Setup command
        this.commandManager = new PaperCommandManager(this.plugin);
        this.command = new GameCommand(new GameGUI(this));
        this.commandManager.registerCommand(this.command);
    }

    /**
     * Method called to release and cleanup any additional resources this module uses
     */
    @Override
    public void teardown() {
        // Teardown command
        this.commandManager.unregisterCommand(this.command);
        this.commandManager = null;
        this.command = null;
    }

    /**
     * Stops the currently running game.
     * <p>
     * This method is used to stop the currently running game, if there is one.
     * If the game's state is {@link GameState#STARTED}, it will change the state
     * to {@link GameState#ENDED}.
     */
    public void stopGame() {
        this.gameModule.getCurrentGame().ifPresent(game -> {
            if (game.getGameState() == GameState.STARTED) {
                game.setGameState(GameState.ENDED);
            }
        });
    }
}
