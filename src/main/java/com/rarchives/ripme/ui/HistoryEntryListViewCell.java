package com.rarchives.ripme.ui;

import com.rarchives.ripme.utils.Utils;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Lukas Kirschner
 * @since 2020-10-31
 **/
public class HistoryEntryListViewCell extends ListCell<HistoryEntry> {
    private static final Insets captionRightMargin = new Insets(0, 10, 0, 0);
    private static double startDateNeededWidth = 0.0;
    private static double modifiedDateNeededWidth = 0.0;
    private static Date dummyDate = Date.from(LocalDateTime.of(2022, 12, 28, 12, 28, 28).atZone(ZoneId.systemDefault()).toInstant());
    private final Label urlLabel;
    private final Label startDateLabel;
    private final Label modifiedDateLabel;
    private final Label titleLabel;
    private final Label startDateCaptionLabel;
    private final Label modifiedDateCaptionLabel;
    private final GridPane mainPane;

    public HistoryEntryListViewCell() {
        super();
        this.urlLabel = new Label();
        this.startDateLabel = new Label();
        this.modifiedDateLabel = new Label();
        this.titleLabel = new Label();
        this.mainPane = new GridPane();
        this.startDateCaptionLabel = new Label(Utils.getLocalizedString("start.date.caption"));
        this.modifiedDateCaptionLabel = new Label(Utils.getLocalizedString("modified.date.caption"));
        GridPane.setHalignment(startDateCaptionLabel, HPos.RIGHT);
        GridPane.setHalignment(modifiedDateCaptionLabel, HPos.RIGHT);
        GridPane.setMargin(startDateCaptionLabel, captionRightMargin);
        GridPane.setMargin(modifiedDateCaptionLabel, captionRightMargin);
        ColumnConstraints startDateColumnConstraints = new ColumnConstraints();
        startDateColumnConstraints.setPrefWidth(getStartDateNeededWidth());
        startDateColumnConstraints.setMinWidth(getStartDateNeededWidth());
        startDateColumnConstraints.setHgrow(Priority.NEVER);
        ColumnConstraints modifiedDateColumnConstraints = new ColumnConstraints();
        modifiedDateColumnConstraints.setPrefWidth(getModifiedDateNeededWidth());
        modifiedDateColumnConstraints.setMinWidth(getModifiedDateNeededWidth());
        modifiedDateColumnConstraints.setHgrow(Priority.NEVER);
        ColumnConstraints urlColumnConstraints = new ColumnConstraints();
        urlColumnConstraints.setHgrow(Priority.ALWAYS);
        mainPane.add(startDateCaptionLabel, 0, 0, 1, 1);
        mainPane.add(modifiedDateCaptionLabel, 1, 0, 1, 1);
        mainPane.add(titleLabel, 2, 0, 1, 1);
        mainPane.add(startDateLabel, 0, 1, 1, 1);
        mainPane.add(modifiedDateLabel, 1, 1, 1, 1);
        mainPane.add(urlLabel, 2, 1, 1, 1);
        mainPane.getColumnConstraints().addAll(startDateColumnConstraints, modifiedDateColumnConstraints, urlColumnConstraints);
        mainPane.setHgap(10);
        mainPane.setVgap(5);

    }

    private double getStartDateNeededWidth() {
        if (startDateNeededWidth == 0.0) {
            startDateNeededWidth = calculateWidthOfTextLabel(HistoryEntry.prettyDate(dummyDate));
        }
        return startDateNeededWidth;
    }

    private double getModifiedDateNeededWidth() {
        if (modifiedDateNeededWidth == 0.0) {
            modifiedDateNeededWidth = calculateWidthOfTextLabel(HistoryEntry.prettyDate(dummyDate));
        }
        return modifiedDateNeededWidth;
    }

    private double calculateWidthOfTextLabel(String content) {
        final Text dummyText = new Text(content);
        new Scene(new Group(dummyText));
        dummyText.applyCss();
        return dummyText.getLayoutBounds().getWidth();
    }

    @Override
    protected void updateItem(HistoryEntry item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setText(null);
            setGraphic(null);
        } else {
            this.startDateLabel.setText(item.getStartDatePretty());
            this.modifiedDateLabel.setText(item.getModifiedDatePretty());
            this.urlLabel.setText(item.getUrl());
            this.titleLabel.setText(item.getTitle());
            setText(null);
            setGraphic(this.mainPane);
        }
    }
}
