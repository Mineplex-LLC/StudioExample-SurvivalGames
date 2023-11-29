package com.mineplex.studio.example.survivalgames.modules.prefix.data;

import com.mineplex.studio.sdk.modules.data.DataStorageModule;
import com.mineplex.studio.sdk.modules.data.StorableStructuredData;
import com.mineplex.studio.sdk.modules.data.annotation.DataCollection;
import com.mineplex.studio.sdk.modules.data.annotation.DataKey;
import lombok.Builder;
import lombok.Data;

/**
 * This class is used for storing and retrieving user prefixes from {@link DataStorageModule}.
 */
@DataCollection(name = "UserPrefixes")
@Data
@Builder
public class UserPrefix implements StorableStructuredData {
    /**
     * Represents a unique identifier for a player.
     * This variable is annotated with {@link DataKey}, indicating that it is a key used for data retrieval.
     */
    @DataKey
    String playerId;

    /**
     * Prefix of a {@link org.bukkit.entity.Player}.
     */
    String prefix;
}
