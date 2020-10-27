package com.rarchives.ripme.ui.config;

import com.rarchives.ripme.utils.Utils;
import javafx.scene.control.CheckBox;

/**
 * @author Lukas Kirschner
 * @since 2020-10-27
 **/
public class BooleanConfigUIItem extends ConfigUIItem<Boolean, CheckBox> {
    public BooleanConfigUIItem(String textKey, String configKey, boolean defaultValue) {
        super(textKey, configKey, defaultValue);
    }

    @Override
    public void loadConfiguration() {
        super.configurationItem.setSelected(Utils.getConfigBoolean(super.configKey, super.defaultValue));
    }

    @Override
    CheckBox generateConfigNode() {
        return new CheckBox();
    }

    @Override
    void setupConfigListener() {
        super.configurationItem.selectedProperty().addListener((observable,oldValue,newValue) -> {
            Utils.setConfigBoolean(super.configKey, newValue);
//            Utils.configureLogger(); // TODO why?
        });
    }
}
