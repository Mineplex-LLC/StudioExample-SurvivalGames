package com.mineplex.studio.example.survivalgames.game.loot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mineplex.studio.example.survivalgames.game.SurvivalGames;
import com.mineplex.studio.example.survivalgames.game.mechanic.TrackingCompassMechanic;
import com.mineplex.studio.jackson.MineplexJacksonModule;
import com.mineplex.studio.sdk.modules.game.MineplexGame;
import com.mineplex.studio.sdk.modules.game.mechanics.GameMechanic;
import com.mineplex.studio.sdk.modules.game.mechanics.loot.LootContainerMechanic;
import com.mineplex.studio.sdk.modules.game.mechanics.loot.LootContainerPool;
import com.mineplex.studio.sdk.modules.game.mechanics.loot.LootContainerRefill;
import com.mineplex.studio.sdk.modules.game.mechanics.loot.LootContainerType;
import com.mineplex.studio.sdk.modules.game.mechanics.loot.items.LootContainerItem;
import com.mineplex.studio.sdk.modules.world.MineplexWorld;
import com.mineplex.studio.sdk.util.AmountRange;
import com.mineplex.studio.sdk.util.MinecraftTimeUnit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Wrapper class for {@link LootContainerMechanic} to handle the {@link SurvivalGames} specific setup.
 * This class handles the loading of the loot settings from the assets folder and the locations from the {@link MineplexWorld} config.
 */
@Slf4j
public class SurvivalGamesLootMechanic implements GameMechanic<SurvivalGames> {
    // Loot type files
    /**
     * Represents the file path for the Tier One configuration file.
     */
    private static final File TIER_ONE_FILE =
            Path.of("assets", "configs", "tier1.json").toFile();

    /**
     * Represents the file path for the tier two configuration file.
     */
    private static final File TIER_TWO_FILE =
            Path.of("assets", "configs", "tier2.json").toFile();

    /**
     * The {@link ObjectMapper} is used to convert the loot type file {@link File} into {@link LootContainerType}.
     */
    private final ObjectMapper objectMapper;

    // Game mechanics
    /**
     * The {@link TrackingCompassMechanic} adds a new {@link org.bukkit.inventory.ItemStack} that can be used by the {@link Player} to locate other game participants.
     */
    private final TrackingCompassMechanic trackingCompassMechanic;
    /**
     * The {@link LootContainerMechanic} is responsible for filling {@link org.bukkit.block.Container} in a {@link MineplexWorld}.
     */
    private LootContainerMechanic lootMechanic;

    /**
     * Constructs a new instance with the given {@link TrackingCompassMechanic}.
     *
     * @param trackingCompassMechanic The {@link TrackingCompassMechanic} to be used by this module
     */
    public SurvivalGamesLootMechanic(final TrackingCompassMechanic trackingCompassMechanic) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new MineplexJacksonModule());

        this.trackingCompassMechanic = trackingCompassMechanic;
    }

    /**
     * Method to be called when this mechanic is set up for a {@link MineplexGame}
     * @param game The {@link MineplexGame} setting up this mechanic
     */
    @Override
    public void setup(@NonNull final SurvivalGames game) {
        // Read LootContainerTypes from disk
        final LootContainerType tierOneType;
        final LootContainerType tierTwoType;
        try {
            tierOneType = this.readLootType(game.getGameWorld(), TIER_ONE_FILE, "TIER-1");
            tierTwoType = this.readLootType(game.getGameWorld(), TIER_TWO_FILE, "TIER-2");

        } catch (final IOException e) {
            log.error("Failed to read loot type from disk!", e);
            throw new RuntimeException(e);
        }

        // Setup LootContainerMechanic
        this.lootMechanic = game.getGameModule()
                .constructGameMechanic(LootContainerMechanic.class, game)
                .orElseThrow();
        this.lootMechanic.register(tierOneType);
        this.lootMechanic.register(tierTwoType);
        this.lootMechanic.setup(game);
    }

    /**
     * Method to be called when this mechanic is no longer needed by the host {@link MineplexGame}
     */
    @Override
    public void teardown() {
        this.lootMechanic.teardown();
    }

    /**
     * Reads the loot type from a file and associates it with locations in the specified {@link MineplexWorld}.
     *
     * @param world The MineplexWorld object representing the world in which the loot type should be associated.
     * @param file The file from which the loot type should be read.
     * @param locationDataPointKey The key to identify the locations in the world associated with the loot type.
     * @return The LootContainerType object read from the file with locations associated.
     * @throws IOException If an I/O error occurs while reading the loot type from the file.
     */
    private LootContainerType readLootType(
            final MineplexWorld world, final File file, final String locationDataPointKey) throws IOException {
        final LootContainerType lootType = this.objectMapper.readValue(file, LootContainerType.class);
        final List<Location> locations = world.getDataPoints(locationDataPointKey);
        return lootType.withLocations(Set.copyOf(locations));
    }

    /**
     * Generate the {@link LootContainerType} types and write them to disk.
     *
     * @throws IOException If an I/O error occurs while writing the loot tables to files.
     */
    private void writeLootTableToFile() throws IOException {
        this.objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValue(
                        TIER_ONE_FILE,
                        LootContainerType.builder()
                                .name("Tier-1")
                                .items(List.of(
                                        LootContainerPool.builder()
                                                .addItem(LootContainerItem.of(Material.WOODEN_AXE, true), 240)
                                                .addItem(LootContainerItem.of(Material.WOODEN_SWORD, true), 210)
                                                .addItem(LootContainerItem.of(Material.STONE_AXE, true), 180)
                                                .addItem(LootContainerItem.of(Material.STONE_SWORD, true), 100)
                                                .build(),
                                        LootContainerPool.builder()
                                                .addItems(
                                                        100,
                                                        LootContainerItem.of(Material.LEATHER_HELMET, true),
                                                        LootContainerItem.of(Material.LEATHER_CHESTPLATE, true),
                                                        LootContainerItem.of(Material.LEATHER_LEGGINGS),
                                                        LootContainerItem.of(Material.LEATHER_BOOTS, true))
                                                .addItems(
                                                        75,
                                                        LootContainerItem.of(Material.GOLDEN_HELMET, true),
                                                        LootContainerItem.of(Material.GOLDEN_CHESTPLATE, true),
                                                        LootContainerItem.of(Material.GOLDEN_LEGGINGS, true),
                                                        LootContainerItem.of(Material.GOLDEN_BOOTS, true))
                                                .addItems(
                                                        30,
                                                        LootContainerItem.of(Material.CHAINMAIL_HELMET, true),
                                                        LootContainerItem.of(Material.CHAINMAIL_CHESTPLATE, true),
                                                        LootContainerItem.of(Material.CHAINMAIL_LEGGINGS, true),
                                                        LootContainerItem.of(Material.CHAINMAIL_BOOTS, true))
                                                .itemsPerContainer(AmountRange.between(1, 2))
                                                .build(),
                                        LootContainerPool.builder()
                                                .addItems(
                                                        100,
                                                        LootContainerItem.of(Material.FISHING_ROD),
                                                        LootContainerItem.of(
                                                                Material.SNOWBALL, AmountRange.between(1, 2)),
                                                        LootContainerItem.of(Material.EGG, AmountRange.between(1, 2)))
                                                .addItems(
                                                        60,
                                                        LootContainerItem.of(Material.BOW),
                                                        LootContainerItem.of(Material.ARROW, AmountRange.between(1, 2)))
                                                .build(),
                                        LootContainerPool.builder()
                                                .addItems(
                                                        100,
                                                        LootContainerItem.of(
                                                                Material.BAKED_POTATO, AmountRange.between(1, 3)),
                                                        LootContainerItem.of(
                                                                Material.COOKED_BEEF, AmountRange.between(1, 2)),
                                                        LootContainerItem.of(
                                                                Material.COOKED_CHICKEN, AmountRange.between(1, 3)),
                                                        LootContainerItem.of(
                                                                Material.CARROTS, AmountRange.between(1, 3)),
                                                        LootContainerItem.of(Material.WHEAT, AmountRange.between(1, 3)),
                                                        LootContainerItem.of(Material.APPLE, AmountRange.between(1, 3)),
                                                        LootContainerItem.of(
                                                                Material.PORKCHOP, AmountRange.between(1, 3)))
                                                .addItem(LootContainerItem.of(Material.MUSHROOM_STEW), 80)
                                                .build(),
                                        LootContainerPool.builder()
                                                .addItems(
                                                        100,
                                                        LootContainerItem.of(
                                                                Material.EXPERIENCE_BOTTLE, AmountRange.between(1, 2)),
                                                        LootContainerItem.of(Material.STICK, AmountRange.between(1, 2)),
                                                        LootContainerItem.builder()
                                                                .itemStack(
                                                                        this.trackingCompassMechanic
                                                                                .createTrackingCompass(5))
                                                                .build())
                                                .addItem(LootContainerItem.of(Material.OAK_BOAT), 50)
                                                .addItems(
                                                        45,
                                                        LootContainerItem.of(
                                                                Material.IRON_INGOT, AmountRange.between(1, 2)),
                                                        LootContainerItem.of(
                                                                Material.GOLD_INGOT, AmountRange.between(1, 2)))
                                                .addItems(
                                                        35,
                                                        LootContainerItem.of(Material.FLINT, AmountRange.between(1, 2)),
                                                        LootContainerItem.of(
                                                                Material.FEATHER, AmountRange.between(1, 2)))
                                                .build()))
                                .refill(LootContainerRefill.builder()
                                        .cooldownMode(LootContainerRefill.CooldownMode.CONTAINER)
                                        .setRefillTime(25, MinecraftTimeUnit.SECONDS)
                                        .build())
                                .locations(Set.of())
                                .build());

        this.objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValue(
                        TIER_TWO_FILE,
                        LootContainerType.builder()
                                .name("Tier-2")
                                .items(List.of(
                                        LootContainerPool.builder()
                                                .addItem(LootContainerItem.of(Material.WOODEN_AXE, true), 240)
                                                .addItem(LootContainerItem.of(Material.WOODEN_SWORD, true), 210)
                                                .addItem(LootContainerItem.of(Material.STONE_AXE, true), 180)
                                                .addItem(LootContainerItem.of(Material.STONE_SWORD, true), 100)
                                                .addItem(LootContainerItem.of(Material.IRON_AXE, true), 100)
                                                .build(),
                                        LootContainerPool.builder()
                                                .addItems(
                                                        100,
                                                        LootContainerItem.of(Material.LEATHER_HELMET, true),
                                                        LootContainerItem.of(Material.LEATHER_CHESTPLATE, true),
                                                        LootContainerItem.of(Material.LEATHER_LEGGINGS),
                                                        LootContainerItem.of(Material.LEATHER_BOOTS, true))
                                                .addItems(
                                                        75,
                                                        LootContainerItem.of(Material.GOLDEN_HELMET, true),
                                                        LootContainerItem.of(Material.GOLDEN_CHESTPLATE, true),
                                                        LootContainerItem.of(Material.GOLDEN_LEGGINGS, true),
                                                        LootContainerItem.of(Material.GOLDEN_BOOTS, true),
                                                        LootContainerItem.of(Material.CHAINMAIL_HELMET, true),
                                                        LootContainerItem.of(Material.CHAINMAIL_CHESTPLATE, true),
                                                        LootContainerItem.of(Material.CHAINMAIL_LEGGINGS, true),
                                                        LootContainerItem.of(Material.CHAINMAIL_BOOTS, true))
                                                .addItems(
                                                        25,
                                                        LootContainerItem.of(Material.IRON_HELMET, true),
                                                        LootContainerItem.of(Material.IRON_CHESTPLATE, true),
                                                        LootContainerItem.of(Material.IRON_LEGGINGS, true),
                                                        LootContainerItem.of(Material.IRON_BOOTS, true))
                                                .itemsPerContainer(AmountRange.between(1, 2))
                                                .build(),
                                        LootContainerPool.builder()
                                                .addItems(
                                                        100,
                                                        LootContainerItem.of(Material.FISHING_ROD),
                                                        LootContainerItem.of(
                                                                Material.SNOWBALL, AmountRange.between(1, 2)),
                                                        LootContainerItem.of(Material.EGG, AmountRange.between(1, 2)))
                                                .addItems(
                                                        50,
                                                        LootContainerItem.of(Material.BOW),
                                                        LootContainerItem.of(Material.ARROW, AmountRange.between(1, 2)))
                                                .build(),
                                        LootContainerPool.builder()
                                                .addItems(
                                                        100,
                                                        LootContainerItem.of(
                                                                Material.BAKED_POTATO, AmountRange.between(1, 3)),
                                                        LootContainerItem.of(
                                                                Material.COOKED_BEEF, AmountRange.between(1, 2)),
                                                        LootContainerItem.of(
                                                                Material.COOKED_CHICKEN, AmountRange.between(1, 3)),
                                                        LootContainerItem.of(
                                                                Material.CARROTS, AmountRange.between(1, 3)),
                                                        LootContainerItem.of(Material.WHEAT, AmountRange.between(1, 3)),
                                                        LootContainerItem.of(Material.APPLE, AmountRange.between(1, 3)),
                                                        LootContainerItem.of(
                                                                Material.PORKCHOP, AmountRange.between(1, 3)))
                                                .addItem(LootContainerItem.of(Material.MUSHROOM_STEW), 80)
                                                .build(),
                                        LootContainerPool.builder()
                                                .addItems(
                                                        100,
                                                        LootContainerItem.of(
                                                                Material.EXPERIENCE_BOTTLE, AmountRange.between(1, 2)),
                                                        LootContainerItem.of(Material.STICK, AmountRange.between(1, 2)),
                                                        LootContainerItem.builder()
                                                                .itemStack(
                                                                        this.trackingCompassMechanic
                                                                                .createTrackingCompass(5))
                                                                .build())
                                                .addItem(LootContainerItem.of(Material.OAK_BOAT), 50)
                                                .addItems(
                                                        50,
                                                        LootContainerItem.of(
                                                                Material.IRON_INGOT, AmountRange.between(1, 2)),
                                                        LootContainerItem.of(Material.DIAMOND))
                                                .addItem(LootContainerItem.of(Material.GOLD_INGOT), 80)
                                                .addItems(
                                                        70,
                                                        LootContainerItem.of(Material.FLINT, AmountRange.between(1, 2)),
                                                        LootContainerItem.of(
                                                                Material.FEATHER, AmountRange.between(1, 2)))
                                                .build()))
                                .refill(LootContainerRefill.builder()
                                        .cooldownMode(LootContainerRefill.CooldownMode.TYPE)
                                        .setRefillTime(15, MinecraftTimeUnit.SECONDS)
                                        .build())
                                .locations(Set.of())
                                .build());
    }
}
