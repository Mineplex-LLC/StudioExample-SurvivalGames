package com.mineplex.studio.example.survivalgames.modules.prefix;

import com.google.common.util.concurrent.Striped;
import com.mineplex.studio.example.survivalgames.modules.prefix.commands.PrefixCommand;
import com.mineplex.studio.example.survivalgames.modules.prefix.data.UserPrefix;
import com.mineplex.studio.example.survivalgames.modules.worlddemo.WorldDemoModule;
import com.mineplex.studio.example.survivalgames.util.CommandUtil;
import com.mineplex.studio.sdk.modules.MineplexModule;
import com.mineplex.studio.sdk.modules.MineplexModuleImplementation;
import com.mineplex.studio.sdk.modules.MineplexModuleManager;
import com.mineplex.studio.sdk.modules.data.DataStorageModule;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A demo {@link MineplexModule} showing a use case for {@link DataStorageModule}.
 */
@RequiredArgsConstructor
@MineplexModuleImplementation(ChatPrefixModule.class)
public class ChatPrefixModule implements MineplexModule {
    /**
     * {@link ReadWriteLock} instance for allowing multiple read and write operations.
     */
    private final Striped<ReadWriteLock> lock = Striped.readWriteLock(4);

    /**
     * A {@link Map} storing {@link Player} to {@link UserPrefix} reference.
     */
    @Getter(AccessLevel.PACKAGE)
    private final Map<Player, UserPrefix> userPrefixes = new WeakHashMap<>();

    /**
     * The {@link JavaPlugin} the {@link MineplexModule} is created from.
     */
    private final JavaPlugin plugin;

    /**
     * {@link MineplexModule} for handling persistent data.
     */
    @Getter(AccessLevel.PACKAGE)
    private DataStorageModule dataStorageModule;

    /**
     * Command to control {@link WorldDemoModule}.
     */
    private Command command;
    /**
     * Contains all {@link Listener} logics for this module.
     */
    private Listener listener;

    /**
     * Method called to allocate any additional resources this module uses
     */
    @Override
    public void setup() {
        // Setup command
        this.command = new PrefixCommand(this);
        CommandUtil.register(this.command);

        // Setup listener
        this.dataStorageModule = MineplexModuleManager.getRegisteredModule(DataStorageModule.class);
        this.listener = new ChatPrefixListener(this);
        Bukkit.getPluginManager().registerEvents(this.listener, this.plugin);
    }

    /**
     * Method called to release and cleanup any additional resources this module uses
     */
    @Override
    public void teardown() {
        // Teardown command
        CommandUtil.unRegister(this.command);
        this.command = null;

        // Teardown listener
        HandlerList.unregisterAll(this.listener);
        this.listener = null;
    }

    /**
     * Returns the {@link ReadWriteLock} corresponding to the specified {@link Player}.
     *
     * @param player the player for which to retrieve the lock
     * @return the ReadWriteLock object associated with the player
     */
    ReadWriteLock getLock(final Player player) {
        return this.lock.get(player.getUniqueId());
    }

    /**
     * Returns the prefix of the requested {@link Player}.
     *
     * @param player the player for which to retrieve the prefix
     * @return an optional string representing the player's prefix,
     *          or an empty optional if no prefix is found
     */
    public Optional<String> getPrefix(final Player player) {
        final Lock lock = this.getLock(player).readLock();
        lock.lock();
        try {
            return Optional.ofNullable(this.userPrefixes.get(player)).map(UserPrefix::getPrefix);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Updates the prefix of the specified {@link Player}.
     *
     * @param player the player for which to update the prefix
     * @param prefix the new prefix to set for the player
     */
    public void updatePrefix(final Player player, final String prefix) {
        final Lock lock = this.getLock(player).writeLock();
        lock.lock();
        try {
            final UserPrefix userPrefix = this.userPrefixes.computeIfAbsent(player, k -> UserPrefix.builder()
                    .playerId(player.getUniqueId().toString())
                    .prefix(prefix)
                    .build());
            userPrefix.setPrefix(prefix);
            this.dataStorageModule.storeStructuredDataAsync(userPrefix);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes the prefix of the specified {@link Player}.
     *
     * @param player the player for which to remove the prefix
     */
    public void removePrefix(final Player player) {
        final Lock lock = this.getLock(player).writeLock();
        lock.lock();
        try {
            this.userPrefixes.remove(player);
            this.dataStorageModule.deleteStructuredDataAsync(
                    UserPrefix.class, player.getUniqueId().toString());
        } finally {
            lock.unlock();
        }
    }
}
