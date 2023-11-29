package com.mineplex.studio.example.survivalgames.modules.manager.ui;

import com.mineplex.studio.example.survivalgames.SurvivalGamesI18nText;
import com.mineplex.studio.example.survivalgames.modules.manager.GameManagerModule;
import com.mineplex.studio.sdk.gui.MineplexGUI;
import com.mineplex.studio.sdk.i18n.I18nText;
import com.mineplex.studio.sdk.modules.game.MineplexGame;
import java.util.Locale;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.geysermc.cumulus.form.Form;
import org.geysermc.cumulus.form.ModalForm;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

/**
 * {@link MineplexGUI} example that is opened through {@link com.mineplex.studio.example.survivalgames.modules.manager.commands.GameCommand}.
 * The {@link GameGUI} provides a way to stop an ongoing {@link com.mineplex.studio.sdk.modules.game.MineplexGame}.
 */
@RequiredArgsConstructor
public class GameGUI implements MineplexGUI {
    // Messages
    /**
     * The {@link I18nText} for the GUI title for both java and bedrock.
     */
    private static final I18nText GUI_TITLE = new SurvivalGamesI18nText("GAME_GUI_TITLE", "Stop Game");
    /**
     * The {@link I18nText} for the button action to stop the game for both java and bedrock.
     */
    private static final I18nText GUI_STOP_TITLE =
            new SurvivalGamesI18nText("GAME_GUI_STOP_TITLE", "Stop the currently running game.");
    /**
     * The {@link I18nText} for the bedrock GUI to close the menu.
     */
    private static final I18nText GUI_CANCEL = new SurvivalGamesI18nText("GAME_GUI_CANCEL", "Cancel");
    /**
     * The {@link I18nText} for the bedrock GUI to stop the game.
     */
    private static final I18nText GUI_STOP = new SurvivalGamesI18nText("GAME_GUI_STOP", "Stop");

    // Modules
    /**
     * The {@link GameManagerModule} is responsible for stopping the game.
     */
    private final GameManagerModule module;

    /**
     * Create a Bedrock {@link Form} for stopping a {@link MineplexGame}.
     *
     * @param player The player for whom the form is being created.
     * @return The Bedrock form for stopping the game.
     */
    @Override
    public Form createBedrockForm(@NonNull final Player player) {
        final Locale locale = player.locale();
        return ModalForm.builder()
                .title(GUI_TITLE.getText(locale))
                .content(GUI_STOP_TITLE.getText(locale))
                .button1(GUI_STOP.getText(locale))
                .button2(GUI_CANCEL.getText(locale))
                .validResultHandler(response -> {
                    if (response.clickedFirst()) {
                        this.module.stopGame();
                    }
                })
                .build();
    }

    /**
     * Create a Java GUI for stopping a {@link MineplexGame}.
     *
     * @param player The player for whom the menu is being created.
     * @return The Java GUI for stopping the game.
     */
    @Override
    public Window createJavaInventoryMenu(@NonNull final Player player) {
        final Locale locale = player.locale();

        final ItemStack display = new ItemStack(Material.REDSTONE_BLOCK);
        display.editMeta(displayItemMeta -> displayItemMeta.displayName(
                Component.text(GUI_STOP_TITLE.getText(locale)).color(NamedTextColor.GREEN)));

        final Gui gui = Gui.normal()
                .setStructure("# # # # C # # # #")
                .addIngredient('C', new SimpleItem(display, click -> {
                    final Player pl = click.getPlayer();
                    pl.closeInventory();
                    this.module.stopGame();
                }))
                .build();

        return Window.single()
                .setViewer(player)
                .setTitle(GUI_TITLE.getText(locale))
                .setGui(gui)
                .build();
    }
}
