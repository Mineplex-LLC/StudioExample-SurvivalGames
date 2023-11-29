package com.mineplex.studio.example.survivalgames.modules.prefix;

import com.mineplex.studio.example.survivalgames.modules.prefix.data.UserPrefix;
import java.util.concurrent.locks.Lock;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * The {@link ChatPrefixListener} class is responsible for handling events related to chat prefixes.
 */
@RequiredArgsConstructor
public class ChatPrefixListener implements Listener {
    /**
     * This module is responsible for managing the prefix of a {@link org.bukkit.entity.Player}.
     */
    private final ChatPrefixModule module;

    /**
     * Handles the {@link PlayerJoinEvent}.
     * Retrieves the user's prefix from {@link com.mineplex.studio.sdk.modules.data.DataStorageModule} and stores it in the user's prefixes map.
     *
     * @param event The {@link PlayerJoinEvent} to handle
     */
    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final String key = event.getPlayer().getUniqueId().toString();

        this.module
                .getDataStorageModule()
                .loadStructuredDataAsync(UserPrefix.class, key)
                .thenAccept(prefix -> {
                    if (prefix.isPresent()) {
                        final Lock lock = this.module.getLock(event.getPlayer()).writeLock();
                        lock.lock();
                        try {
                            this.module.getUserPrefixes().put(event.getPlayer(), prefix.get());
                        } finally {
                            lock.unlock();
                        }
                    }
                });
    }
}
