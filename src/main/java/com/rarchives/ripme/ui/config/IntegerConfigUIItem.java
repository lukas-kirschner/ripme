package com.rarchives.ripme.ui.config;

import com.rarchives.ripme.utils.Utils;
import javafx.scene.control.TextField;

/**
 * @author Lukas Kirschner
 * @since 2020-10-27
 **/
public class IntegerConfigUIItem extends ConfigUIItem<Integer, TextField> {
    public IntegerConfigUIItem(String textKey, String configKey, int defaultValue) {
        super(textKey, configKey, defaultValue);
    }

    @Override
    public void loadConfiguration() {
        super.configurationItem.setText(Integer.toString(Utils.getConfigInteger(super.configKey, super.defaultValue)));
    }

    @Override
    TextField generateConfigNode() {
        return new TextField();
    }

    @Override
    void setupConfigListener() {
        super.configurationItem.focusedProperty().addListener((arg0,oldVal,newVal)->{
            if (!newVal){//Fire on lost focus
                try {
                    Utils.setConfigInteger(super.configKey, Integer.parseInt(super.configurationItem.getText()));
                } catch (NumberFormatException e){
                    super.configurationItem.clear();
                }
            }
        });
    }
}
