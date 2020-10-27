package com.rarchives.ripme.ui.config;

import com.rarchives.ripme.utils.Utils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Lukas Kirschner
 * @since 2020-10-27
 **/
public class DirectoryConfigUIItem extends ConfigUIItem<Path, HBox> {
    private final Window stage;
    private TextField pathField = null;
    private Button pathChooserButton = null;
    private DirectoryChooser fileChooser = null;

    public DirectoryConfigUIItem(String textKey, String configKey, Path defaultValue, Window stage) {
        super(textKey, configKey, defaultValue);
        this.stage = stage;
    }

    @Override
    public void loadConfiguration() {
        this.pathField.setText(Utils.getConfigString(super.configKey, super.defaultValue.toString()));
        this.fileChooser.setInitialDirectory(new File(Utils.getConfigString(super.configKey, super.defaultValue.toString())));
    }

    @Override
    HBox generateConfigNode() {
        this.pathField = new TextField();
        this.pathChooserButton = new Button("...");
        this.fileChooser = new DirectoryChooser();
        HBox.setHgrow(this.pathField, Priority.ALWAYS);
        return new HBox(this.pathField, this.pathChooserButton);
    }

    @Override
    void setupConfigListener() {
        fileChooser.setTitle(Utils.getLocalizedString("filechooser.pathselect"));
        pathChooserButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                File dir = fileChooser.showDialog(stage);
                if (dir != null) {
                    try {
                        Utils.setConfigString(DirectoryConfigUIItem.super.configKey, dir.getCanonicalPath().toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                    loadConfiguration();
                }
            }
        });
    }
}
