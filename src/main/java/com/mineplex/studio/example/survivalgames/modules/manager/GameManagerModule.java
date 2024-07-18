package com.mineplex.studio.example.survivalgames.modules.manager;

import com.mineplex.studio.example.survivalgames.modules.manager.commands.GameCommand;
import com.mineplex.studio.example.survivalgames.modules.manager.ui.GameGUI;
import com.mineplex.studio.example.survivalgames.modules.worlddemo.WorldDemoModule;
import com.mineplex.studio.sdk.gui.MineplexGUI;
import com.mineplex.studio.sdk.modules.MineplexModule;
import com.mineplex.studio.sdk.modules.MineplexModuleImplementation;
import com.mineplex.studio.sdk.modules.MineplexModuleManager;
import com.mineplex.studio.sdk.modules.command.CommandModule;
import com.mineplex.studio.sdk.modules.game.GameCycle;
import com.mineplex.studio.sdk.modules.game.GameState;
import com.mineplex.studio.sdk.modules.game.MineplexGame;
import com.mineplex.studio.sdk.modules.game.MineplexGameModule;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;

/**
 * A demo {@link MineplexModule} showing a use case of {@link MineplexGUI} to stop the running game.
 */
@RequiredArgsConstructor
@MineplexModuleImplementation(GameManagerModule.class)
public class GameManagerModule implements MineplexModule {
    // Modules
    /**
     * The {@link MineplexGameModule} is responsible for managing the {@link GameCycle} and to construct new {@link GameCycle} for {@link MineplexGame}.
     */
    private MineplexGameModule gameModule;

    /**
     * The {@link CommandModule} is responsible for registering and unregistering commands dynamically.
     */
    private CommandModule commandModule;

    /**
     * Command to control {@link WorldDemoModule}.
     */
    private Command command;

    /**
     * Method called to allocate any additional resources this module uses
     */
    @Override
    public void setup() {
        this.gameModule = MineplexModuleManager.getRegisteredModule(MineplexGameModule.class);
        this.commandModule = MineplexModuleManager.getRegisteredModule(CommandModule.class);

        // Setup command
        this.command = new GameCommand(new GameGUI(this));
        this.commandModule.register("sg", this.command);
    }

    /**
     * Method called to release and cleanup any additional resources this module uses
     */
    @Override
    public void teardown() {
        this.commandModule.unregister(this.command);
        this.command = null;
        this.commandModule = null;

        this.gameModule = null;
    }

    /**
     * Stops the currently running game.
     * <p>
     * This method is used to stop the currently running game, if there is one.
     * If the game's state is {@link GameState#isInProgress}, it will forcefully start the next game.
     */
    public void stopGame() {
        this.gameModule
                .getCurrentGame()
                .filter(game -> game.getGameState().isInProgress())
                .ifPresent(game -> this.gameModule.startNextGame());
    }
}
