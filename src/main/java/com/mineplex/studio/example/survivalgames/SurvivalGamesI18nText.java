package com.mineplex.studio.example.survivalgames;

import com.mineplex.studio.sdk.i18n.I18nText;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class is the standardized way for all {@link SurvivalGamesPlugin} components to create localized messages.
 */
public class SurvivalGamesI18nText extends I18nText {
    /**
     * The localization key used for retrieving localized strings specific to {@link SurvivalGamesPlugin}.
     */
    private static final String LOCALIZATION_KEY = "SurvivalGames";

    /**
     * Creates a new {@link I18nText} with the default {@link SurvivalGamesPlugin} localization source.
     *
     * @param i18nKey The key used to retrieve the localized text.
     * @param defaultText The default text to use if the localized text is not available.
     */
    public SurvivalGamesI18nText(@NonNull final String i18nKey, @Nullable final String defaultText) {
        super(LOCALIZATION_KEY, i18nKey, defaultText);
    }
}
