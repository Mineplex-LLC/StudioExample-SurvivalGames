package com.mineplex.studio.example.survivalgames.game.mechanic;

import com.mineplex.studio.sdk.modules.i18n.MineplexMessageComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * This class contains all the message components for the Survival Games tracking compass mechanic.
 */
public class TrackingCompassMessageComponent extends MineplexMessageComponent {
    /**
     * The name of the tracking compass item.
     */
    public static final Args0 ITEM_NAME =
            () -> itemName(Component.translatable("sg.mechanic.tracking_compass.item.name"))
                    .color(NamedTextColor.YELLOW);

    /**
     * The lore of the tracking compass item.
     */
    public static final Args1<Integer> ITEM_LORE_0 = uses ->
            itemLore(Component.translatable("sg.mechanic.tracking_compass.item.lore.0", count(Component.text(uses))));
    /**
     * The first line of the lore of the tracking compass item.
     */
    public static final Args0 ITEM_LORE_1 =
            () -> itemLore(Component.translatable("sg.mechanic.tracking_compass.item.lore.1"));
    /**
     * The second line of the lore of the tracking compass item.
     */
    public static final Args0 ITEM_LORE_2 =
            () -> itemLore(Component.translatable("sg.mechanic.tracking_compass.item.lore.2"));
    /**
     * The third line of the lore of the tracking compass item.
     */
    public static final Args0 ITEM_LORE_3 =
            () -> itemLore(Component.translatable("sg.mechanic.tracking_compass.item.lore.3"));
    /**
     * The fourth line of the lore of the tracking compass item.
     */
    public static final Args0 ITEM_LORE_4 =
            () -> itemLore(Component.translatable("sg.mechanic.tracking_compass.item.lore.4"));

    /**
     * The message when the tracking compass has no target.
     */
    public static final Args0 NO_TARGET =
            () -> withGamePrefix(Component.translatable("sg.mechanic.tracking_compass.no_target"));
    /**
     * The message when the tracking compass has found a target and the distance to the target.
     */
    public static final Args3<Component, Integer, Integer> FOUND_TARGET = (target, distance, newUses) -> {
        Component uses = newUses > 1
                ? Component.translatable("sg.mechanic.tracking_compass.uses.plural")
                : Component.translatable("sg.mechanic.tracking_compass.uses.singular");
        return withGamePrefix(Component.translatable(
                "sg.mechanic.tracking_compass.target",
                element(target),
                element(Component.text(distance)),
                element(Component.text(newUses)),
                uses));
    };
    /**
     * The message when an item was combined with another item.
     */
    public static final Args0 COMBINE =
            () -> withGamePrefix(Component.translatable("sg.mechanic.tracking_compass.combine"));
}
