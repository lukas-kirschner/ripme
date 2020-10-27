package com.rarchives.ripme.ui;

import java.awt.TrayIcon;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import com.rarchives.ripme.ui.config.BooleanConfigUIItem;
import com.rarchives.ripme.ui.config.ConfigUIItem;
import com.rarchives.ripme.ui.config.DirectoryConfigUIItem;
import com.rarchives.ripme.ui.config.IntegerConfigUIItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.utils.RipUtils;
import com.rarchives.ripme.utils.Utils;

import javafx.application.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

/**
 * Everything UI-related starts and ends here.
 */
public final class MainWindow implements Runnable, RipStatusHandler {

    private static final Logger LOGGER = Logger.getLogger(MainWindow.class);

    private boolean isRipping = false; // Flag to indicate if we're ripping something

    private static Stage stage;
    private static VBox mainFrame;
    private static TextField ripTextfield;
    private static Button ripButton, stopButton;

    private static Label statusLabel;
    private static Button openButton;
    private static ProgressBar statusProgress;

    // Put an empty JPanel on the bottom of the window to keep components
    // anchored to the top when there is no open lower panel
    private static Pane emptyPanel;
    private static TabPane optionsPanel;

    // Log
    private static Tab optionLog;
    private static VBox logPanel;
    private static ListView<String> logText;
    private static ObservableList<String> logList = FXCollections.observableArrayList();

    // History
    private static Tab optionHistory;
    private static final History HISTORY = new History();
    private static VBox historyPanel;
    private static ListView<String> historyTable;
    private static ObservableList<String> historyTableModel = FXCollections.observableArrayList();
    private static Button historyButtonRemove, historyButtonClear, historyButtonRerip;

    // Queue
    public static Tab optionQueue;
    private static VBox queuePanel;
    private static ListView<String> queueView;
    private static ObservableList<String> queueListModel = FXCollections.observableArrayList();

    // Configuration
    private static Tab optionConfiguration;
    private static ScrollPane configurationPanel;
    // This doesn't really belong here but I have no idea where else to put it
    private static Button configUrlFileChooserButton;

    private static TrayIcon trayIcon;
    private static MenuItem trayMenuMain;
    private static CheckMenuItem trayMenuAutorip;

    private static Image mainIcon;

    private static AbstractRipper ripper;

    private void updateQueue(ObservableList<String> model) {
        if (model != null)
            queueListModel = model;
        if (queueListModel.size() > 0) {
            Utils.setConfigList("queue", queueListModel.stream());
            Utils.saveConfig();
        }
        queueView.setItems(queueListModel);
    }

    private void updateQueue() {
        updateQueue(null);
    }

    public static void addUrlToQueue(String url) {
        queueListModel.add(url);
    }

    public MainWindow(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle(Utils.IMPLEMENTATION_TITLE + " v" + Utils.IMPLEMENTATION_VERSION);
        stage.setOnCloseRequest(t -> {
            Platform.exit();
        });
        mainFrame = new VBox();

        createUI(mainFrame);
        pack();

        loadHistory();
        setupHandlers();

        Thread shutdownThread = new Thread(this::shutdownCleanup);
        Runtime.getRuntime().addShutdownHook(shutdownThread);

        boolean autoripEnabled = Utils.getConfigBoolean("clipboard.autorip", false);
        ClipboardUtils.setClipboardAutoRip(autoripEnabled);
//        trayMenuAutorip.setSelected(autoripEnabled);
    }

    public void run() {
        pack();
        restoreWindowPositionAndSetupScene();
        stage.show();
    }

    private void shutdownCleanup() {
//        Utils.setConfigBoolean("file.overwrite", configOverwriteCheckbox.isSelected());
//        Utils.setConfigInteger("threads.size", Integer.parseInt(configThreadsText.getText()));
//        Utils.setConfigInteger("download.retries", Integer.parseInt(configRetriesText.getText()));
//        Utils.setConfigInteger("download.timeout", Integer.parseInt(configTimeoutText.getText()));
//        Utils.setConfigBoolean("clipboard.autorip", ClipboardUtils.getClipboardAutoRip());
//        Utils.setConfigString("log.level", configLogLevelCombobox.getValue());
//        Utils.setConfigBoolean("play.sound", configPlaySound.isSelected());
//        Utils.setConfigBoolean("download.save_order", configSaveOrderCheckbox.isSelected());
//        Utils.setConfigBoolean("download.show_popup", configShowPopup.isSelected());
//        Utils.setConfigBoolean("log.save", configSaveLogs.isSelected());
//        Utils.setConfigBoolean("urls_only.save", configSaveURLsOnly.isSelected());
//        Utils.setConfigBoolean("album_titles.save", configSaveAlbumTitles.isSelected());
//        Utils.setConfigBoolean("clipboard.autorip", configClipboardAutorip.isSelected());
//        Utils.setConfigBoolean("descriptions.save", configSaveDescriptions.isSelected());
//        Utils.setConfigBoolean("prefer.mp4", configPreferMp4.isSelected());
//        Utils.setConfigBoolean("photos.only", configDownloadPhotosOnly.isSelected());
//        Utils.setConfigBoolean("remember.url_history", configURLHistoryCheckbox.isSelected());
//        Utils.setConfigString("lang", configSelectLangComboBox.getValue());
        saveWindowPosition();
        saveHistory();
        Utils.saveConfig();
    }

    private void status(String text) {
        statusWithColor(text, Color.BLACK);
    }

    private void error(String text) {
        statusWithColor(text, Color.RED);
    }

    private void statusWithColor(String text, Paint color) {
        statusLabel.setTextFill(color);
        statusLabel.setText(text);
        pack();
    }

    private void pack() {
        Platform.runLater(() -> {
//            Dimension preferredSize = mainFrame.getPreferredSize();
//            mainFrame.setMinimumSize(preferredSize);
//            if (isCollapsed()) {
//                mainFrame.setSize(preferredSize);
//            }//TODO
        });
    }

    private void createLogPanel(){
        logText = new ListView<>(logList);
        logText.setEditable(false);
        VBox.setVgrow(logText,Priority.ALWAYS);
        logPanel = new VBox(logText);
    }
    private void createHistoryPanel(){
        historyButtonRemove = new Button(Utils.getLocalizedString("remove"));
        historyButtonClear = new Button(Utils.getLocalizedString("clear"));
        historyButtonRerip = new Button(Utils.getLocalizedString("re-rip.checked"));
        Pane dummyPane = new Pane();
        HBox.setHgrow(dummyPane,Priority.ALWAYS);
        HBox historyMenuPane = new HBox(historyButtonRemove,historyButtonRerip,dummyPane,historyButtonClear);
        historyTable = new ListView<>(historyTableModel);
        VBox.setVgrow(historyTable,Priority.ALWAYS);
        historyPanel = new VBox(historyMenuPane,historyTable);
        //TODO Button functionality
    }
    private void createQueuePanel(){
        queueView = new ListView<>();

        VBox.setVgrow(queueView,Priority.ALWAYS);
        queuePanel = new VBox();
        queueListModel.addAll(Utils.getConfigList("queue"));
        updateQueue();
    }

    private static List<ConfigUIItem<?,? extends Node>> configUIItems = Arrays.asList(
            new IntegerConfigUIItem("max.download.threads","threads.size",3),
            new IntegerConfigUIItem("timeout.mill","download.timeout",60000),
            new IntegerConfigUIItem("retry.download.count","download.retries",3),
            new BooleanConfigUIItem("overwrite.existing.files","file.overwrite",false),
            new BooleanConfigUIItem("sound.when.rip.completes","play.sound",false),
            new BooleanConfigUIItem("notification.when.rip.starts","download.show_popup",false),
            new BooleanConfigUIItem("preserve.order","download.save_order",true),
            new BooleanConfigUIItem("save.logs","log.save",false),
            new BooleanConfigUIItem("save.urls.only","urls_only.save",false),
            new BooleanConfigUIItem("save.album.titles","album_titles.save",true),
            new BooleanConfigUIItem("autorip.from.clipboard","clipboard.autorip",false),
            new BooleanConfigUIItem("save.descriptions","descriptions.save",true),
            new BooleanConfigUIItem("prefer.mp4.over.gif","prefer.mp4",true),
            new BooleanConfigUIItem("download.photos.only","photos.only",false),
            new BooleanConfigUIItem("restore.window.position","window.position",true),
            new BooleanConfigUIItem("remember.url.history","remember.url_history",true),
            new DirectoryConfigUIItem("select.save.dir","rips.directory",Paths.get(Utils.getJarDirectory().getPath()).resolve(Utils.RIP_DIRECTORY),stage)
            //TODO languages
//        configSelectLangComboBox = new JComboBox<>(Utils.getSupportedLanguages());
//        configSelectLangComboBox.setSelectedItem(Utils.getSelectedLanguage());
            //TODO Log level? Maybe not
    );

    private void createConfigurationPanel(){
        GridPane mainConfigurationPane = new GridPane();
        configurationPanel = new ScrollPane(mainConfigurationPane);
        configurationPanel.setFitToHeight(true);
        configurationPanel.setFitToWidth(true);
        mainConfigurationPane.setHgap(5);
        mainConfigurationPane.setVgap(2);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setPercentWidth(25);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        c2.setPercentWidth(75);
        mainConfigurationPane.getColumnConstraints().addAll(c1,c2);
        AtomicInteger currentRow = new AtomicInteger(0);

        for (final ConfigUIItem<?,? extends Node> item : configUIItems){
            final int rowIndex = currentRow.getAndIncrement();
            final Label lbl = item.instantiateDescriptionLabel();
            GridPane.setHalignment(lbl, HPos.RIGHT);
            GridPane.setValignment(lbl, VPos.CENTER);
            mainConfigurationPane.add(lbl,0,rowIndex,1,1);
            mainConfigurationPane.add(item.instantiateConfigNode(),1,rowIndex,1,1);
            item.loadConfiguration();
            //TODO style, listeners...? Change from GridPane to ListView?
        }

//        configUrlFileChooserButton = new JButton(Utils.getLocalizedString("download.url.list"));//TODO add this button somewhere else!

//        configLogLevelCombobox = new JComboBox<>(
//                new String[] { "Log level: Error", "Log level: Warn", "Log level: Info", "Log level: Debug" });
//        configLogLevelCombobox.setSelectedItem(Utils.getConfigString("log.level", "Log level: Debug"));
//        setLogLevel(configLogLevelCombobox.getSelectedItem().toString());
//        configSaveDirLabel = new JLabel();
//        try {
//            String workingDir = (Utils.shortenPath(Utils.getWorkingDirectory()));
//            configSaveDirLabel.setText(workingDir);
//            configSaveDirLabel.setForeground(Color.BLUE);
//            configSaveDirLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
//        } catch (Exception e) {
//        }
//        configSaveDirLabel.setToolTipText(configSaveDirLabel.getText());
//        configSaveDirLabel.setHorizontalAlignment(JLabel.RIGHT);
//        configSaveDirButton = new JButton(Utils.getLocalizedString("select.save.dir") + "...");

    }

    private void createUI(VBox pane) {
        // If creating the tray icon fails, ignore it.
        try {
            setupTrayIcon();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        EmptyBorder emptyBorder = new EmptyBorder(5, 5, 5, 5);
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//        gbc.weightx = 1;
//        gbc.ipadx = 2;
//        gbc.gridx = 0;
//        gbc.weighty = 0;
//        gbc.ipady = 2;
//        gbc.gridy = 0;
//        gbc.anchor = GridBagConstraints.PAGE_START;
//TODO set custom CSS here...? or native look and feel if possible

//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (ClassNotFoundException | InstantiationException
//                | IllegalAccessException e) {
//            LOGGER.error("[!] Exception setting system theme:", e);
//        }

        ripTextfield = new TextField("");
//        ripTextfield.addMouseListener(new ContextMenuMouseListener());//TODO
//        ImageIcon ripIcon = new ImageIcon(mainIcon);
        ripButton = new Button("Rip");
        stopButton = new Button("Stop");
        stopButton.setDisable(true);
//        try {
//            Image stopIcon = new Image(getClass().getClassLoader().getResource("stop.png").toString());
//            stopButton.setIcon(new ImageIcon(stopIcon));
//        } catch (Exception ignored) {
//        }
        //todo
        HBox ripPanel = new HBox(new Label("URL:"),ripTextfield,ripButton,stopButton);
        ripPanel.setAlignment(Pos.BASELINE_CENTER);
        HBox.setHgrow(ripTextfield,Priority.ALWAYS);

        statusLabel = new Label(Utils.getLocalizedString("inactive"));
//        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        openButton = new Button();
        openButton.setVisible(false);
        HBox.setHgrow(statusLabel,Priority.ALWAYS);
        HBox statusPanel = new HBox(statusLabel,openButton);
        statusPanel.setAlignment(Pos.BASELINE_CENTER);

        statusProgress = new ProgressBar();
        statusProgress.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(statusProgress,Priority.ALWAYS);
        HBox progressPanel = new HBox(statusProgress);

        optionsPanel = new TabPane();
        optionsPanel.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(optionsPanel,Priority.ALWAYS);

        createLogPanel();
        createHistoryPanel();
        createQueuePanel();
        createConfigurationPanel();

        optionLog = new Tab(Utils.getLocalizedString("Log"),logPanel);
        optionHistory = new Tab(Utils.getLocalizedString("History"),historyPanel);
        optionQueue = new Tab(Utils.getLocalizedString("queue"),queuePanel);
        optionConfiguration = new Tab(Utils.getLocalizedString("Configuration"),configurationPanel);
        optionsPanel.getTabs().addAll(optionLog,optionHistory,optionQueue,optionConfiguration);
        pane.getChildren().addAll(ripPanel,statusPanel,progressPanel,optionsPanel);
    }

    private void changeLocale() {
        statusLabel.setText(Utils.getLocalizedString("inactive"));
        for (final ConfigUIItem<?,? extends Node> item : configUIItems){
            item.updateLocale();
        }
    }

    private void setupHandlers() {
        ripButton.setOnAction(new RipButtonHandler());
//        ripTextfield.addActionListener(new RipButtonHandler()); //We do not want to spam into the console when a user enters text!
//        ripTextfield.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                update();
//            }
//
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                update();
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                update();
//            }
//
//            private void update() {
//                try {
//                    String urlText = ripTextfield.getText().trim();
//                    if (urlText.equals("")) {
//                        return;
//                    }
//                    if (!urlText.startsWith("http")) {
//                        urlText = "http://" + urlText;
//                    }
//                    URL url = new URL(urlText);
//                    AbstractRipper ripper = AbstractRipper.getRipper(url);
//                    statusWithColor(ripper.getHost() + " album detected", Color.GREEN);
//                } catch (Exception e) {
//                    statusWithColor("Can't rip this URL: " + e.getMessage(), Color.RED);
//                }
//            }
//        });
        stopButton.setOnAction(event -> {
            if (ripper != null) {
                ripper.stop();
                isRipping = false;
                stopButton.setDisable(true);
                statusProgress.setProgress(0.0);
                statusProgress.setVisible(false);
                pack();
                statusProgress.setProgress(0.0);
                status(Utils.getLocalizedString("ripping.interrupted"));
                appendLog("Ripper interrupted", Color.RED);
            }
        });
//        optionLog.addActionListener(event -> {
//            logPanel.setVisible(!logPanel.isVisible());
//            emptyPanel.setVisible(!logPanel.isVisible());
//            historyPanel.setVisible(false);
//            queuePanel.setVisible(false);
//            configurationPanel.setVisible(false);
//            if (logPanel.isVisible()) {
//                optionLog.setFont(optionLog.getFont().deriveFont(Font.BOLD));
//            } else {
//                optionLog.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
//            }
//            optionHistory.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
//            optionQueue.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
//            optionConfiguration.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
//            pack();
//        });
//        optionHistory.addActionListener(event -> {
//            logPanel.setVisible(false);
//            historyPanel.setVisible(!historyPanel.isVisible());
//            emptyPanel.setVisible(!historyPanel.isVisible());
//            queuePanel.setVisible(false);
//            configurationPanel.setVisible(false);
//            optionLog.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
//            if (historyPanel.isVisible()) {
//                optionHistory.setFont(optionLog.getFont().deriveFont(Font.BOLD));
//            } else {
//                optionHistory.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
//            }
//            optionQueue.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
//            optionConfiguration.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
//            pack();
//        });
//        optionQueue.addActionListener(event -> {
//            logPanel.setVisible(false);
//            historyPanel.setVisible(false);
//            queuePanel.setVisible(!queuePanel.isVisible());
//            emptyPanel.setVisible(!queuePanel.isVisible());
//            configurationPanel.setVisible(false);
//            optionLog.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
//            optionHistory.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
//            if (queuePanel.isVisible()) {
//                optionQueue.setFont(optionLog.getFont().deriveFont(Font.BOLD));
//            } else {
//                optionQueue.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
//            }
//            optionConfiguration.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
//            pack();
//        });
//        optionConfiguration.addActionListener(event -> {
//            logPanel.setVisible(false);
//            historyPanel.setVisible(false);
//            queuePanel.setVisible(false);
//            configurationPanel.setVisible(!configurationPanel.isVisible());
//            emptyPanel.setVisible(!configurationPanel.isVisible());
//            optionLog.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
//            optionHistory.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
//            optionQueue.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
//            if (configurationPanel.isVisible()) {
//                optionConfiguration.setFont(optionLog.getFont().deriveFont(Font.BOLD));
//            } else {
//                optionConfiguration.setFont(optionLog.getFont().deriveFont(Font.PLAIN));
//            }
//            pack();
//        });
        historyButtonRemove.setOnAction(event -> {
            for (String item : historyTable.getSelectionModel().getSelectedItems()){
                HISTORY.remove(item);
                historyTableModel.remove(item);
            }
            saveHistory();
        });
        historyButtonClear.setOnAction(event -> {//TODO grey out if history is empty
            if (Utils.getConfigBoolean("history.warn_before_delete", true)) {
                final String OK = Utils.getLocalizedString("dialog.ok");
                final String CANCEL = Utils.getLocalizedString("dialog.cancel");
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.initStyle(StageStyle.UTILITY);
                alert.setTitle(Utils.getLocalizedString("history.delete.confirm.title"));
                alert.setHeaderText(Utils.getLocalizedString("history.delete.confirm.title"));
                alert.setContentText(Utils.getLocalizedString("history.delete.confirm.body"));
                String[] options = new String[]{OK, CANCEL};
                List<ButtonType> buttons = new ArrayList<>();
                for (String option : options) {
                    buttons.add(new ButtonType(option));
                }
                alert.getButtonTypes().setAll(buttons);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get().getText().equals(OK)) {
                    //Fall through
                } else {
                    return;
                }
//                //To make enter key press the actual focused button, not the first one. Just like pressing "space". // TODO Stackoverflow suggests this
//                alert.getDialogPane().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
//                    if (event.getCode().equals(KeyCode.ENTER)) {
//                        event.consume();
//                        try {
//                            Robot r = new Robot();
//                            r.keyPress(java.awt.event.KeyEvent.VK_SPACE);
//                            r.keyRelease(java.awt.event.KeyEvent.VK_SPACE);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
            } else {
                Utils.clearURLHistory();
                HISTORY.clear();
                historyTableModel.clear();
                saveHistory();
            }
        });

        // Re-rip all selected history elements
        historyButtonRerip.setOnAction(event -> {
            if (HISTORY.isEmpty()) {//TODO Rewrite such that button greys out instead
//                JOptionPane.showMessageDialog(null, Utils.getLocalizedString("history.load.none"), "RipMe Error",
//                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            int added = 0;
            queueListModel.addAll(historyTable.getSelectionModel().getSelectedItems());
//            if (added == 0) {
//                JOptionPane.showMessageDialog(null, Utils.getLocalizedString("history.load.none.checked"),
//
//                        "RipMe Error", JOptionPane.ERROR_MESSAGE);
//            }
        });
//        configLogLevelCombobox.addActionListener(arg0 -> {
//            String level = ((JComboBox) arg0.getSource()).getSelectedItem().toString();
//            setLogLevel(level);
//        });
//        configSelectLangComboBox.addActionListener(arg0 -> {
//            String level = ((JComboBox) arg0.getSource()).getSelectedItem().toString();
//            Utils.setLanguage(level);
//            changeLocale();
//        });
// Great TODO-------------------------------------- Refactor to use ONE SINGLE method to parse URL files!!!!!!
//        configUrlFileChooserButton.addActionListener(arg0 -> {
//            UIManager.put("FileChooser.useSystemExtensionHiding", false);
//            JFileChooser jfc = new JFileChooser(Utils.getWorkingDirectory());
//            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
//            int returnVal = jfc.showDialog(null, "Open");
//            if (returnVal != JFileChooser.APPROVE_OPTION) {
//                return;
//            }
//            File chosenFile = jfc.getSelectedFile();
//            String chosenPath = null;
//            try {
//                chosenPath = chosenFile.getCanonicalPath();
//            } catch (Exception e) {
//                LOGGER.error("Error while getting selected path: ", e);
//                return;
//            }
//            try (BufferedReader br = new BufferedReader(new FileReader(chosenPath))) {
//                for (String line = br.readLine(); line != null; line = br.readLine()) {
//                    line = line.trim();
//                    if (line.startsWith("http")) {
//                        MainWindow.addUrlToQueue(line);
//                    } else {
//                        LOGGER.error("Skipping url " + line + " because it looks malformed (doesn't start with http)");
//                    }
//                }
//
//            } catch (IOException e) {
//                LOGGER.error("Error reading file " + e.getMessage());
//            }
//        });


//TODO
//        configClipboardAutorip.addActionListener(arg0 -> {
//            Utils.setConfigBoolean("clipboard.autorip", configClipboardAutorip.isSelected());
//            ClipboardUtils.setClipboardAutoRip(configClipboardAutorip.isSelected());
//            trayMenuAutorip.setState(configClipboardAutorip.isSelected());
//            Utils.configureLogger();
//        });

//        queueListModel.addListDataListener(new ListDataListener() {
//            @Override
//            public void intervalAdded(ListDataEvent arg0) {
//                updateQueue();
//
//                if (!isRipping) {
//                    ripNextAlbum();
//                }
//            }
//
//            @Override
//            public void contentsChanged(ListDataEvent arg0) {
//            }
//
//            @Override
//            public void intervalRemoved(ListDataEvent arg0) {
//            }
//        });
    }

    private void setLogLevel(String level) {
        Level newLevel = Level.ERROR;
        level = level.substring(level.lastIndexOf(' ') + 1);
        switch (level) {
        case "Debug":
            newLevel = Level.DEBUG;
            break;
        case "Info":
            newLevel = Level.INFO;
            break;
        case "Warn":
            newLevel = Level.WARN;
            break;
        case "Error":
            newLevel = Level.ERROR;
            break;
        }
        Logger.getRootLogger().setLevel(newLevel);
        LOGGER.setLevel(newLevel);
        ConsoleAppender ca = (ConsoleAppender) Logger.getRootLogger().getAppender("stdout");
        if (ca != null) {
            ca.setThreshold(newLevel);
        }
        FileAppender fa = (FileAppender) Logger.getRootLogger().getAppender("FILE");
        if (fa != null) {
            fa.setThreshold(newLevel);
        }
    }

    private void setupTrayIcon() {
//        mainFrame.addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowActivated(WindowEvent e) {
//                trayMenuMain.setLabel(Utils.getLocalizedString("tray.hide"));
//            }
//
//            @Override
//            public void windowDeactivated(WindowEvent e) {
//                trayMenuMain.setLabel(Utils.getLocalizedString("tray.show"));
//            }
//
//            @Override
//            public void windowDeiconified(WindowEvent e) {
//                trayMenuMain.setLabel(Utils.getLocalizedString("tray.hide"));
//            }
//
//            @Override
//            public void windowIconified(WindowEvent e) {
//                trayMenuMain.setLabel(Utils.getLocalizedString("tray.show"));
//            }
//        });
//        PopupMenu trayMenu = new PopupMenu();
//        trayMenuMain = new MenuItem(Utils.getLocalizedString("tray.hide"));
//        trayMenuMain.addActionListener(arg0 -> toggleTrayClick());
//        MenuItem trayMenuAbout = new MenuItem("About " + mainFrame.getTitle());
//        trayMenuAbout.addActionListener(arg0 -> {
//            StringBuilder about = new StringBuilder();
//
//            about.append("<html><h1>").append(mainFrame.getTitle()).append("</h1>");
//            about.append("Download albums from various websites:");
//            try {
//                List<String> rippers = Utils.getListOfAlbumRippers();
//                about.append("<ul>");
//                for (String ripper : rippers) {
//                    about.append("<li>");
//                    ripper = ripper.substring(ripper.lastIndexOf('.') + 1);
//                    if (ripper.contains("Ripper")) {
//                        ripper = ripper.substring(0, ripper.indexOf("Ripper"));
//                    }
//                    about.append(ripper);
//                    about.append("</li>");
//                }
//                about.append("</ul>");
//            } catch (Exception e) {
//            }
//            about.append("<br>And download videos from video sites:");
//            try {
//                List<String> rippers = Utils.getListOfVideoRippers();
//                about.append("<ul>");
//                for (String ripper : rippers) {
//                    about.append("<li>");
//                    ripper = ripper.substring(ripper.lastIndexOf('.') + 1);
//                    if (ripper.contains("Ripper")) {
//                        ripper = ripper.substring(0, ripper.indexOf("Ripper"));
//                    }
//                    about.append(ripper);
//                    about.append("</li>");
//                }
//                about.append("</ul>");
//            } catch (Exception e) {
//            }
//
//            about.append("Do you want to visit the project homepage on Github?");
//            about.append("</html>");
//            int response = JOptionPane.showConfirmDialog(null, about.toString(), mainFrame.getTitle(),
//                    JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, new ImageIcon(mainIcon));
//            if (response == JOptionPane.YES_OPTION) {
//                try {
//                    Desktop.getDesktop().browse(URI.create("http://github.com/ripmeapp/ripme"));
//                } catch (IOException e) {
//                    LOGGER.error("Exception while opening project home page", e);
//                }
//            }
//        });
//        MenuItem trayMenuExit = new MenuItem(Utils.getLocalizedString("tray.exit"));
//        trayMenuExit.addActionListener(arg0 -> System.exit(0));
//        trayMenuAutorip = new CheckboxMenuItem(Utils.getLocalizedString("tray.autorip"));
//        trayMenuAutorip.addItemListener(arg0 -> {
//            ClipboardUtils.setClipboardAutoRip(trayMenuAutorip.getState());
//            configClipboardAutorip.setSelected(trayMenuAutorip.getState());
//        });
//        trayMenu.add(trayMenuMain);
//        trayMenu.add(trayMenuAbout);
//        trayMenu.addSeparator();
//        trayMenu.add(trayMenuAutorip);
//        trayMenu.addSeparator();
//        trayMenu.add(trayMenuExit);
//        try {
//            mainIcon = ImageIO.read(getClass().getClassLoader().getResource("icon.png"));
//            trayIcon = new TrayIcon(mainIcon);
//            trayIcon.setToolTip(mainFrame.getTitle());
//            trayIcon.setImageAutoSize(true);
//            trayIcon.setPopupMenu(trayMenu);
//            SystemTray.getSystemTray().add(trayIcon);
//            trayIcon.addMouseListener(new MouseAdapter() {
//                @Override
//                public void mouseClicked(MouseEvent e) {
//                    toggleTrayClick();
//                    if (mainFrame.getExtendedState() != JFrame.NORMAL) {
//                        mainFrame.setExtendedState(JFrame.NORMAL);
//                    }
//                    mainFrame.setAlwaysOnTop(true);
//                    mainFrame.setAlwaysOnTop(false);
//                }
//            });
//        } catch (IOException | AWTException e) {
//            // TODO implement proper stack trace handling this is really just intented as a
//            // placeholder until you implement proper error handling
//            e.printStackTrace();
//        }
    }

    private void toggleTrayClick() {
//        if (mainFrame.getExtendedState() == JFrame.ICONIFIED || !mainFrame.isActive() || !mainFrame.isVisible()) {
//            mainFrame.setVisible(true);
//            mainFrame.setAlwaysOnTop(true);
//            mainFrame.setAlwaysOnTop(false);
//            trayMenuMain.setLabel(Utils.getLocalizedString("tray.hide"));
//        } else {
//            mainFrame.setVisible(false);
//            trayMenuMain.setLabel(Utils.getLocalizedString("tray.show"));
//        }
    }

    /**
     * Write a line to the Log section of the GUI
     *
     * @param text  the string to log
     * @param color the color of the line
     */
    private void appendLog(final String text, final Color color) {
        logList.add(text);
    }

    /**
     * Write a line to the GUI log and the CLI log
     *
     * @param line  the string to log
     * @param color the color of the line for the GUI log
     */
    public void displayAndLogError(String line, Color color) {
        appendLog(line, color);
        LOGGER.error(line);
    }

    private void loadHistory() {
        File historyFile = new File(Utils.getConfigDir() + File.separator + "history.json");
        HISTORY.clear();
        if (historyFile.exists()) {
            try {
                LOGGER.info(Utils.getLocalizedString("loading.history.from") + " " + historyFile.getCanonicalPath());
                HISTORY.fromFile(historyFile.getCanonicalPath());
            } catch (IOException e) {
                LOGGER.error("Failed to load history from file " + historyFile, e);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initStyle(StageStyle.UTILITY);
                alert.setTitle(Utils.getLocalizedString("error"));
                alert.setHeaderText(Utils.getLocalizedString("error"));
                alert.setContentText(String.format(Utils.getLocalizedString("history.load.failed.warning"), e.getMessage()));
                alert.showAndWait();
            }
        } else {
            LOGGER.info(Utils.getLocalizedString("loading.history.from.configuration"));
            HISTORY.fromList(Utils.getConfigList("download.history"));
            if (HISTORY.toList().isEmpty()) {
                // Loaded from config, still no entries.
                // Guess rip history based on rip folder
                String[] dirs = Utils.getWorkingDirectory()
                        .list((dir, file) -> new File(dir.getAbsolutePath() + File.separator + file).isDirectory());
                for (String dir : dirs) {
                    String url = RipUtils.urlFromDirectoryName(dir);
                    if (url != null) {
                        // We found one, add it to history
                        HistoryEntry entry = new HistoryEntry();
                        entry.url = url;
                        HISTORY.add(entry);
                    }
                }
            }
        }
    }

    private void saveHistory() {
        Path historyFile = Paths.get(Utils.getConfigDir() + File.separator + "history.json");
        try {
            if (!Files.exists(historyFile)) {
                Files.createDirectories(historyFile.getParent());
                Files.createFile(historyFile);
            }

            HISTORY.toFile(historyFile.toString());
            Utils.setConfigList("download.history", Collections.emptyList());
        } catch (IOException e) {
            LOGGER.error("Failed to save history to file " + historyFile, e);
        }
    }

    private void ripNextAlbum() {
        isRipping = true;
        // Save current state of queue to configuration.
        Utils.setConfigList("queue", queueListModel);

        if (queueListModel.isEmpty()) {
            // End of queue
            isRipping = false;
            return;
        }
        String nextAlbum = queueListModel.remove(0);//TODO replace with actual queue

        updateQueue();

        Thread t = ripAlbum(nextAlbum);
        if (t == null) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                LOGGER.error(Utils.getLocalizedString("interrupted.while.waiting.to.rip.next.album"), ie);
            }
            ripNextAlbum();
        } else {
            t.start();
        }
    }

    private Thread ripAlbum(String urlString) {
        // shutdownCleanup();
        optionsPanel.getSelectionModel().select(optionLog);//Select the log tab
        urlString = urlString.trim();
        if (urlString.toLowerCase().startsWith("gonewild:")) { //TODO generalize? Add Subreddits?
            urlString = "http://gonewild.com/user/" + urlString.substring(urlString.indexOf(':') + 1);
        }
        if (!urlString.startsWith("http")) {
            urlString = "http://" + urlString;
        }
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            LOGGER.error("[!] Could not generate URL for '" + urlString + "'", e);
            error("Given URL is not valid, expecting http://website.com/page/...");
            return null;
        }
        stopButton.setDisable(false);
        statusProgress.setProgress(0.0);
        openButton.setVisible(false);
        statusLabel.setVisible(true);
        pack();
        boolean failed = false;
        try {
            ripper = AbstractRipper.getRipper(url);
            ripper.setup();
        } catch (Exception e) {
            failed = true;
            LOGGER.error("Could not find ripper for URL " + url, e);
            error(e.getMessage());
        }
        if (!failed) {
            try {
                stage.setTitle("Ripping - "+Utils.IMPLEMENTATION_TITLE+" v" + Utils.IMPLEMENTATION_VERSION);
                status("Starting rip...");
                ripper.setObserver(this);
                Thread t = new Thread(ripper);
//                if (configShowPopup.isSelected() && (!mainFrame.isVisible() || !mainFrame.isActive())) {//TODO
//                    mainFrame.toFront();
//                    mainFrame.setAlwaysOnTop(true);
//                    trayIcon.displayMessage(mainFrame.getTitle(), "Started ripping " + ripper.getURL().toExternalForm(),
//                            MessageType.INFO);
//                    mainFrame.setAlwaysOnTop(false);
//                }
                return t;
            } catch (Exception e) {
                LOGGER.error("[!] Error while ripping: " + e.getMessage(), e);
                error("Unable to rip this URL: " + e.getMessage());
            }
        }
        stopButton.setDisable(true);
        statusProgress.setProgress(0.0);
        pack();
        return null;
    }

    private boolean canRip(String urlString) {
        try {
            String urlText = urlString.trim();
            if (urlText.equals("")) {
                return false;
            }
            if (!urlText.startsWith("http")) {
                urlText = "http://" + urlText;
            }
            URL url = new URL(urlText);
            // Ripper is needed here to throw/not throw an Exception
            AbstractRipper ripper = AbstractRipper.getRipper(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    class RipButtonHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent arg0) {
            String url = ripTextfield.getText();
            if (!queueListModel.contains(url) && !url.equals("")) {
                // Check if we're ripping a range of urls//TODO Better parsing
                if (url.contains("{")) {
                    // Make sure the user hasn't forgotten the closing }
                    if (url.contains("}")) {
                        String rangeToParse = url.substring(url.indexOf("{") + 1, url.indexOf("}"));
                        int rangeStart = Integer.parseInt(rangeToParse.split("-")[0]);
                        int rangeEnd = Integer.parseInt(rangeToParse.split("-")[1]);
                        for (int i = rangeStart; i < rangeEnd + 1; i++) {
                            String realURL = url.replaceAll("\\{\\S*\\}", Integer.toString(i));
                            if (canRip(realURL)) {
                                queueListModel.add(queueListModel.size(), realURL);
                                ripTextfield.clear();
                            } else {
                                displayAndLogError("Can't find ripper for " + realURL, Color.RED);
                            }
                        }
                    }
                } else {
                    queueListModel.add(queueListModel.size(), ripTextfield.getText());
                    ripTextfield.clear();
                }
            } else {
                if (!isRipping) {
                    ripNextAlbum();
                }
            }
        }
    }

    private class StatusEvent implements Runnable {
        private final AbstractRipper ripper;
        private final RipStatusMessage msg;

        StatusEvent(AbstractRipper ripper, RipStatusMessage msg) {
            this.ripper = ripper;
            this.msg = msg;
        }

        public void run() {
            handleEvent(this);
        }
    }

    private synchronized void handleEvent(StatusEvent evt) {
        if (ripper.isStopped()) {
            return;
        }
        RipStatusMessage msg = evt.msg;

        int completedPercent = evt.ripper.getCompletionPercentage();
        statusProgress.setProgress((double)completedPercent / 100.0);
        statusProgress.setVisible(true);
        status(evt.ripper.getStatusText());

        switch (msg.getStatus()) {
        case LOADING_RESOURCE:
        case DOWNLOAD_STARTED:
            if (LOGGER.isEnabledFor(Level.INFO)) {
                appendLog("Downloading " + msg.getObject(), Color.BLACK);
            }
            break;
        case DOWNLOAD_COMPLETE:
            if (LOGGER.isEnabledFor(Level.INFO)) {
                appendLog("Downloaded " + msg.getObject(), Color.GREEN);
            }
            break;
        case DOWNLOAD_COMPLETE_HISTORY:
            if (LOGGER.isEnabledFor(Level.INFO)) {
                appendLog("" + msg.getObject(), Color.GREEN);
            }
            break;

        case DOWNLOAD_ERRORED:
            if (LOGGER.isEnabledFor(Level.ERROR)) {
                appendLog((String) msg.getObject(), Color.RED);
            }
            break;
        case DOWNLOAD_WARN:
            if (LOGGER.isEnabledFor(Level.WARN)) {
                appendLog((String) msg.getObject(), Color.ORANGE);
            }
            break;

        case RIP_ERRORED:
            if (LOGGER.isEnabledFor(Level.ERROR)) {
                appendLog((String) msg.getObject(), Color.RED);
            }
            stopButton.setDisable(true);
            statusProgress.setProgress(0.0);
            statusProgress.setVisible(false);
            openButton.setVisible(false);
            pack();
            statusWithColor("Error: " + msg.getObject(), Color.RED);
            break;

        case RIP_COMPLETE:
            RipStatusComplete rsc = (RipStatusComplete) msg.getObject();
            String url = ripper.getURL().toExternalForm();
            if (HISTORY.containsURL(url)) {
                // TODO update "modifiedDate" of entry in HISTORY
                HistoryEntry entry = HISTORY.getEntryByURL(url);
                entry.count = rsc.count;
                entry.modifiedDate = new Date();
            } else {
                HistoryEntry entry = new HistoryEntry();
                entry.url = url;
                entry.dir = rsc.getDir();
                entry.count = rsc.count;
                try {
                    entry.title = ripper.getAlbumTitle(ripper.getURL());
                } catch (MalformedURLException e) {
                }
                HISTORY.add(entry);
//                historyTableModel.fireTableDataChanged();
            }
            if (Utils.getConfigBoolean("play.sound",false)) {
                Utils.playSound("camera.wav");
            }
            saveHistory();
            stopButton.setDisable(true);
            statusProgress.setProgress(0.0);
            statusProgress.setVisible(false);
            openButton.setVisible(true);
            File f = rsc.dir;
            String prettyFile = Utils.shortenPath(f);
            openButton.setText(Utils.getLocalizedString("open") + prettyFile);
            stage.setTitle(Utils.IMPLEMENTATION_TITLE + " v" + Utils.IMPLEMENTATION_VERSION);
//            try {//TODO
//                Image folderIcon = ImageIO.read(getClass().getClassLoader().getResource("folder.png"));
//                openButton.setIcon(new ImageIcon(folderIcon));
//            } catch (Exception e) {
//            }
            /*
             * content key %path% the path to the album folder %url% is the album url
             *
             *
             */
            if (Utils.getConfigBoolean("enable.finish.command", false)) {//TODO add timeout option
                try {
                    String commandToRun = Utils.getConfigString("finish.command", "ls");
                    commandToRun = commandToRun.replaceAll("%url%", url);
                    commandToRun = commandToRun.replaceAll("%path%", f.getAbsolutePath());
                    LOGGER.info("Running command " + commandToRun);
                    // code from:
                    // https://stackoverflow.com/questions/5711084/java-runtime-getruntime-getting-output-from-executing-a-command-line-program
                    Process proc = Runtime.getRuntime().exec(commandToRun);
                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

                    BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

                    // read the output from the command
                    LOGGER.info("Command output:\n");
                    String s = null;
                    while ((s = stdInput.readLine()) != null) {
                        LOGGER.info(s);
                    }

                    // read any errors from the attempted command
                    LOGGER.error("Command error:\n");
                    while ((s = stdError.readLine()) != null) {
                        System.out.println(s);
                    }
                } catch (IOException e) {
                    LOGGER.error("Was unable to run command \"" + Utils.getConfigString("finish.command", "ls"));
                    LOGGER.error(e.getStackTrace());
                }
            }
            appendLog("Rip complete, saved to " + f.getAbsolutePath(), Color.GREEN);
//            openButton.setActionCommand(f.toString());//TODO Open Button
//            openButton.addActionListener(event -> {
//                try {
//                    Desktop.getDesktop().open(new File(event.getActionCommand()));
//                } catch (Exception e) {
//                    LOGGER.error(e);
//                }
//            });
            pack();
            ripNextAlbum();
            break;
        case COMPLETED_BYTES:
            // Update completed bytes
            break;
        case TOTAL_BYTES:
            // Update total bytes
            break;
        case NO_ALBUM_OR_USER:
            if (LOGGER.isEnabledFor(Level.ERROR)) {
                appendLog((String) msg.getObject(), Color.RED);
            }
            stopButton.setDisable(true);
            statusProgress.setProgress(0.0);
            statusProgress.setVisible(false);
            openButton.setVisible(false);
            pack();
            statusWithColor("Error: " + msg.getObject(), Color.RED);
            break;
        }
    }

    public void update(AbstractRipper ripper, RipStatusMessage message) {
        StatusEvent event = new StatusEvent(ripper, message);
        Platform.runLater(event);
    }

    public static void ripAlbumStatic(String url) {
        ripTextfield.setText(url.trim());
        ripButton.fire();
    }

    public static void enableWindowPositioning() {
        Utils.setConfigBoolean("window.position", true);
    }

    public static void disableWindowPositioning() {
        Utils.setConfigBoolean("window.position", false);
    }

    private static boolean hasWindowPositionBug() {
        String osName = System.getProperty("os.name");
        // Java on Windows has a bug where if we try to manually set the position of the
        // Window,
        // javaw.exe will not close itself down when the application is closed.
        // Therefore, even if isWindowPositioningEnabled, if we are on Windows, we
        // ignore it.
        return osName == null || osName.startsWith("Windows");
    }

    private static boolean isWindowPositioningEnabled() {
        boolean isEnabled = Utils.getConfigBoolean("window.position", true);
        return isEnabled && !hasWindowPositionBug();
    }

    private static void saveWindowPosition() {
        if (!isWindowPositioningEnabled()) {
            return;
        }

        int x = (int) stage.getX();
        int y = (int) stage.getY();
        int w = (int) stage.getWidth();
        int h = (int) stage.getHeight();
        Utils.setConfigInteger("window.x", x);
        Utils.setConfigInteger("window.y", y);
        Utils.setConfigInteger("window.w", w);
        Utils.setConfigInteger("window.h", h);
        LOGGER.debug("Saved window position (x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + ")");
    }

    private static void restoreWindowPositionAndSetupScene() {
        if (!isWindowPositioningEnabled()) {
            stage.setScene(new Scene(mainFrame));
            return;
        }

        try {
            int x = Utils.getConfigInteger("window.x", -1);
            int y = Utils.getConfigInteger("window.y", -1);
            int w = Utils.getConfigInteger("window.w", -1);
            int h = Utils.getConfigInteger("window.h", -1);
            if (x < 0 || y < 0 || w <= 0 || h <= 0) {
                LOGGER.debug("UNUSUAL: One or more of: x, y, w, or h was still less than 0 after reading config");
                stage.setScene(new Scene(mainFrame));
                return;
            }
            stage.setScene(new Scene(mainFrame,w,h));
            stage.setX(x);
            stage.setY(y);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
