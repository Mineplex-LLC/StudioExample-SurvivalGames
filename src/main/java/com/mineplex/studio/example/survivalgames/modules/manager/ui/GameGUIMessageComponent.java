package com.mineplex.studio.example.survivalgames.modules.manager.ui;

import com.mineplex.studio.sdk.modules.i18n.MineplexMessageComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * This class contains all the message components for the Survival Games game GUI.
 */
public class GameGUIMessageComponent extends MineplexMessageComponent {
    /**
     * The GUI title for the game GUI.
     */
    public static final Args0 GUI_TITLE = () -> Component.translatable("sg.module.manager.gui.title");
    /**
     * The GUI content for the game GUI.
     */
    public static final Args0 GUI_CONTENT = () -> Component.translatable("sg.module.manager.gui.content");
    /**
     * The button action message to stop the game.
     */
    public static final Args0 GUI_STOP =
            () -> Component.translatable("sg.module.manager.gui.stop").color(NamedTextColor.GREEN);
    /**
     * The button action message to cancel the GUI.
     */
    public static final Args0 GUI_CANCEL = () -> Component.translatable("sg.module.manager.gui.cancel");
}
