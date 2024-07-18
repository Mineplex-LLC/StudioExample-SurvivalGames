package com.mineplex.studio.example.survivalgames.modules.manager.ui;

import com.mineplex.studio.example.survivalgames.modules.manager.GameManagerModule;
import com.mineplex.studio.sdk.gui.MineplexGUI;
import com.mineplex.studio.sdk.modules.game.MineplexGame;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.geysermc.cumulus.form.Form;
import org.geysermc.cumulus.form.ModalForm;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

/**
 * {@link MineplexGUI} example that is opened through {@link com.mineplex.studio.example.survivalgames.modules.manager.commands.GameCommand}.
 * The {@link GameGUI} provides a way to stop an ongoing {@link com.mineplex.studio.sdk.modules.game.MineplexGame}.
 */
@RequiredArgsConstructor
public class GameGUI implements MineplexGUI {
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
        return ModalForm.builder()
                .title(GameGUIMessageComponent.GUI_TITLE.renderBedrock(player))
                .content(GameGUIMessageComponent.GUI_CONTENT.renderBedrock(player))
                .button1(GameGUIMessageComponent.GUI_STOP.renderBedrock(player))
                .button2(GameGUIMessageComponent.GUI_CANCEL.renderBedrock(player))
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
        final ItemStack display = ItemStack.of(Material.REDSTONE_BLOCK);
        display.editMeta(displayItemMeta -> displayItemMeta.displayName(GameGUIMessageComponent.GUI_CONTENT.apply()));

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
                .setTitle(new AdventureComponentWrapper(GameGUIMessageComponent.GUI_TITLE.apply()))
                .setGui(gui)
                .build();
    }
}
