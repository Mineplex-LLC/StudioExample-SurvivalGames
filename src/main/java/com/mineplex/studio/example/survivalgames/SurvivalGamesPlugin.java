package com.mineplex.studio.example.survivalgames;

import com.mineplex.studio.example.survivalgames.game.SurvivalGames;
import com.mineplex.studio.example.survivalgames.modules.chat.SurvivalGamesChatModule;
import com.mineplex.studio.example.survivalgames.modules.manager.GameManagerModule;
import com.mineplex.studio.example.survivalgames.modules.prefix.ChatPrefixModule;
import com.mineplex.studio.example.survivalgames.modules.worlddemo.WorldDemoModule;
import com.mineplex.studio.sdk.modules.MineplexModuleManager;
import com.mineplex.studio.sdk.modules.game.GameCycle;
import com.mineplex.studio.sdk.modules.game.MineplexGame;
import com.mineplex.studio.sdk.modules.game.MineplexGameModule;
import com.mineplex.studio.sdk.modules.lobby.LobbyModule;
import com.mineplex.studio.sdk.modules.world.MineplexWorld;
import com.mineplex.studio.sdk.modules.world.MineplexWorldModule;
import com.mineplex.studio.sdk.modules.world.config.MineplexWorldConfig;
import com.mineplex.studio.sdk.modules.world.config.WorldCreationConfig;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This is the entry point of your plugin.
 */
@Slf4j
public class SurvivalGamesPlugin extends JavaPlugin {
    /**
     * These aren't the droids you're looking for.
     */
    public static final boolean LOCAL_TESTING = System.getenv("LOCAL_TEST") != null;

    /**
     * The {@link LobbyModule} manages the {@link org.bukkit.entity.Player} till the {@link MineplexGame} is started.
     */
    private LobbyModule lobbyModule;
    /**
     * The {@link MineplexGameModule} is responsible for managing the {@link GameCycle} and to construct new {@link GameCycle} for {@link MineplexGame}.
     */
    private MineplexGameModule gameModule;

    /**
     * Method called when the plugin is enabled.
     * Use this method to setup:
     * - {@link GameCycle}
     * - {@link com.mineplex.studio.sdk.modules.MineplexModule}
     */
    @Override
    public void onEnable() {
        log.info("Starting plugin!");

        // Setup modules
        final MineplexModuleManager moduleManager = MineplexModuleManager.getInstance();
        moduleManager
                .registerModule(new WorldDemoModule())
                .registerModule(new ChatPrefixModule(this))
                .registerModule(new SurvivalGamesChatModule())
                .registerModule(new GameManagerModule());

        // Create and load the lobby
        final MineplexWorldModule mineplexWorldModule =
                MineplexModuleManager.getRegisteredModule(MineplexWorldModule.class);

        // Load the lobby from /assets/world-templates/lobby.zip
        final MineplexWorld lobby = mineplexWorldModule.createMineplexWorld(
                MineplexWorldConfig.builder()
                        .worldCreationConfig(WorldCreationConfig.builder()
                                .worldTemplate("lobby")
                                .build())
                        .build(),
                null);

        this.lobbyModule = MineplexModuleManager.getRegisteredModule(LobbyModule.class);
        this.lobbyModule.setActiveLobby(this.lobbyModule.createBasicLobby(lobby));
        this.lobbyModule.setup();

        // Setup SurvivalGames game cycle
        this.gameModule = MineplexModuleManager.getRegisteredModule(MineplexGameModule.class);
        this.gameModule.setGameCycle(new GameCycle() {
            @Override
            public MineplexGame createNextGame() {
                return new SurvivalGames(SurvivalGamesPlugin.this);
            }

            @Override
            public boolean hasNextGame() {
                return true;
            }
        });

        // Trigger the next game
        this.gameModule.setCurrentGame(
                this.gameModule.getGameCycle().orElseThrow().createNextGame());
    }

    /**
     * Method called when the plugin is disabled.
     * Use this method to teardown:
     * - {@link com.mineplex.studio.sdk.modules.MineplexModule}
     */
    @Override
    public void onDisable() {
        this.gameModule.teardown();
        this.lobbyModule.teardown();
    }
}
