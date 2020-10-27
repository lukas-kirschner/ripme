package com.rarchives.ripme.ui.config;

import com.rarchives.ripme.utils.Utils;
import javafx.scene.control.Label;

/**
 * @param <T> Type of the configuration variable
 * @param <U> Type of the Node
 * @author Lukas Kirschner
 * @since 2020-10-27
 **/
public abstract class ConfigUIItem<T, U> {
    public final String textKey;
    public final String configKey;
    public final T defaultValue;
    private Label descriptionLabel = null;
    U configurationItem = null;

    public ConfigUIItem(String textKey, String configKey, T defaultValue) {
        this.textKey = textKey;
        this.configKey = configKey;
        this.defaultValue = defaultValue;
    }

    /**
     * Instantiate a new Description label for the config UI item or return the existing one if called multiple times.
     *
     * @return the description label
     */
    public final Label instantiateDescriptionLabel() {
        if (descriptionLabel == null) {
            descriptionLabel = new Label();
            updateLocale();
        }
        return descriptionLabel;
    }

    /**
     * Update the locale, i.e. the text displayed on the description label
     */
    public final void updateLocale() {
        if (descriptionLabel != null) {
            descriptionLabel.setText(Utils.getLocalizedString(textKey));
        }

    }

    /**
     * Load the default configuration of this ConfigUIItem from the global configuration
     */
    public abstract void loadConfiguration();

    /**
     * Instantiate the configuration node or return an existing node if called before
     *
     * @return the configuration node
     */
    public final U instantiateConfigNode() {
        if (configurationItem == null) {
            configurationItem = this.generateConfigNode();
            this.loadConfiguration();
            this.setupConfigListener();
        }
        return configurationItem;
    }

    /**
     * Only instantiate a new {@literal U} without doing anything else
     * @return
     */
    abstract U generateConfigNode();

    /**
     * Sets up the config listener that updates the config as soon as the action is fired
     */
    abstract void setupConfigListener();
}
