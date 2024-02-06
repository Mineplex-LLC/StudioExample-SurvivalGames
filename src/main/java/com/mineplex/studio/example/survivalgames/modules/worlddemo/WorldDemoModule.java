package com.mineplex.studio.example.survivalgames.modules.worlddemo;

import com.mineplex.studio.example.survivalgames.modules.worlddemo.commands.DemoWorldCommand;
import com.mineplex.studio.example.survivalgames.util.CommandUtil;
import com.mineplex.studio.sdk.modules.MineplexModule;
import com.mineplex.studio.sdk.modules.MineplexModuleManager;
import com.mineplex.studio.sdk.modules.world.MineplexWorld;
import com.mineplex.studio.sdk.modules.world.MineplexWorldModule;
import com.mineplex.studio.sdk.modules.world.config.MineplexWorldConfig;
import com.mineplex.studio.sdk.modules.world.config.PersistentWorldConfig;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.Command;

/**
 * A demo {@link MineplexModule} using the persistence world feature of {@link MineplexWorldModule}.
 */
@Getter
@Setter
public class WorldDemoModule implements MineplexModule {
    /**
     * Command to control {@link WorldDemoModule}.
     */
    private Command command;

    /**
     * The {@link MineplexWorldModule} manges the creation and loading of temporary and persistence {@link MineplexWorld}.
     */
    private MineplexWorldModule worldModule;
    /**
     * The name of the bucket for storing demo worlds.
     * The default bucket name is "DemoWorlds".
     */
    private String bucketName = "DemoWorlds";

    /**
     * Initializes a new instance of the WorldDemoModule class, with the given JavaPlugin instance.
     */
    public WorldDemoModule() {
        this.command = new DemoWorldCommand(this);
    }

    /**
     * Method called to allocate any additional resources this module uses
     */
    @Override
    public void setup() {
        this.worldModule = MineplexModuleManager.getRegisteredModule(MineplexWorldModule.class);
        CommandUtil.register(this.command);
    }

    /**
     * Method called to release and cleanup any additional resources this module uses
     */
    @Override
    public void teardown() {
        CommandUtil.unRegister(this.command);
        this.command = null;
    }

    /**
     * Loads or creates a {@link MineplexWorld} with the specified id from the {@link this#bucketName}.
     *
     * @param id the id of the {@link MineplexWorld} to load or create
     * @return a CompletableFuture that completes with the loaded or newly created {@link MineplexWorld}
     */
    public CompletableFuture<MineplexWorld> loadDemoWorld(final String id) {
        return this.worldModule.loadOrCreateMineplexWorld(
                this.bucketName,
                id,
                MineplexWorldConfig.builder()
                        .persistentWorldConfig(PersistentWorldConfig.builder()
                                .worldBucket(this.bucketName)
                                .build())
                        .build());
    }

    /**
     * Unloads a {@link MineplexWorld} with the specified id.
     *
     * @param id the id of the {@link MineplexWorld} to unload
     * @return {@code true} if the {@link MineplexWorld} was successfully unloaded, {@code false} otherwise.
     */
    public boolean unloadDemoWorld(final String id) {
        return this.worldModule
                .getLoadedMineplexWorld(id)
                .map(world -> {
                    this.worldModule.releaseWorld(world);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Deletes a demo world with the specified id.
     *
     * @param id the id of the demo world to delete
     * @return a {@link CompletableFuture} that completes when the deletion is successful
     */
    public CompletableFuture<Void> deleteDemoWorld(final String id) {
        return this.worldModule.deleteWorld(this.bucketName, id);
    }
}
