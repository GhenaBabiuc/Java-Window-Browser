package org.example;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static javafx.concurrent.Worker.State.FAILED;

public class SimpleSwingBrowser extends JFrame {

    private final JFXPanel jfxPanel = new JFXPanel();
    private WebEngine engine;
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JLabel lblStatus = new JLabel();
    private final JButton btnGo = new JButton("Go");
    private final JTextField txtURL = new JTextField();
    private final JProgressBar progressBar = new JProgressBar();
    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnBack = new JButton("<-");
    private final JButton btnForward = new JButton("->");
    private final JButton btnHistory = new JButton("History");
    private final JList<String> historyList = new JList<>();
    private final DefaultListModel<String> historyListModel = new DefaultListModel<>();
    private final JButton btnAddBookmark = new JButton("Add Bookmark");
    private final JButton btnManageBookmarks = new JButton("Manage Bookmarks");
    private final DefaultListModel<String> bookmarksListModel = new DefaultListModel<>();
    private final JList<String> bookmarksList = new JList<>(bookmarksListModel);

    public SimpleSwingBrowser() {
        super();
        initComponents();
    }

    private void initComponents() {
        createScene();

        ActionListener al = e -> loadURL(txtURL.getText());

        btnGo.addActionListener(al);
        txtURL.addActionListener(al);
        btnHistory.addActionListener(e -> showHistoryDialog());
        btnAddBookmark.addActionListener(e -> addBookmark());
        btnManageBookmarks.addActionListener(e -> showBookmarksDialog());

        btnRefresh.addActionListener(e -> Platform.runLater(() -> engine.reload()));
        btnBack.addActionListener(e -> Platform.runLater(() -> {
            if (engine.getHistory().getCurrentIndex() > 0) {
                engine.getHistory().go(-1);
            }
        }));
        btnForward.addActionListener(e -> Platform.runLater(() -> {
            if (engine.getHistory().getCurrentIndex() + 1 < engine.getHistory().getEntries().size()) {
                engine.getHistory().go(1);
            }
        }));

        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);

        JPanel addressBar = new JPanel(new BorderLayout());
        addressBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnBack);
        buttonPanel.add(btnForward);

        addressBar.add(buttonPanel, BorderLayout.WEST);
        addressBar.add(txtURL, BorderLayout.CENTER);
        addressBar.add(btnGo, BorderLayout.EAST);

        JPanel historyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        historyPanel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        historyPanel.add(btnHistory, BorderLayout.WEST);
        historyPanel.add(btnAddBookmark, BorderLayout.WEST);
        historyPanel.add(btnManageBookmarks, BorderLayout.WEST);

        JPanel topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.Y_AXIS));
        topBar.add(addressBar);
        topBar.add(historyPanel);

        JPanel statusBar = new JPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.add(lblStatus, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(jfxPanel, BorderLayout.CENTER);
        panel.add(statusBar, BorderLayout.SOUTH);

        getContentPane().add(panel);

        setLocationRelativeTo(null);
        setPreferredSize(new Dimension(1024, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    private void addBookmark() {
        String currentUrl = txtURL.getText();
        if (!bookmarksListModel.contains(currentUrl)) {
            bookmarksListModel.addElement(currentUrl);
            JOptionPane.showMessageDialog(panel, "Bookmark added successfully!");
        } else {
            JOptionPane.showMessageDialog(panel, "Bookmark already exists!");
        }
    }

    private void showBookmarksDialog() {
        JDialog bookmarksDialog = new JDialog(this, "Bookmarks", true);
        bookmarksDialog.setLayout(new BorderLayout());

        JScrollPane bookmarksScrollPane = new JScrollPane(bookmarksList);
        bookmarksDialog.add(bookmarksScrollPane, BorderLayout.CENTER);

        bookmarksList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedBookmark();
                }
            }
        });

        bookmarksList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyCode.ENTER.getCode()) {
                    openSelectedPageFromHistory();
                }
            }
        });

        bookmarksDialog.setSize(400, 300);
        bookmarksDialog.setLocationRelativeTo(this);
        bookmarksDialog.setVisible(true);
    }

    private void openSelectedBookmark() {
        int selectedIndex = bookmarksList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < bookmarksListModel.getSize()) {
            String selectedUrl = bookmarksListModel.getElementAt(selectedIndex);
            loadURL(selectedUrl);
        }
    }


    private void showHistoryDialog() {
        List<WebHistory.Entry> entries = engine.getHistory().getEntries();
        historyListModel.clear();

        for (WebHistory.Entry entry : entries) {
            historyListModel.addElement(entry.getUrl());
        }

        historyList.setModel(historyListModel);

        JDialog historyDialog = new JDialog(this, "History", true);
        historyDialog.setLayout(new BorderLayout());

        JScrollPane historyScrollPane = new JScrollPane(historyList);
        historyDialog.add(historyScrollPane, BorderLayout.CENTER);

        historyList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyCode.ENTER.getCode()) {
                    openSelectedPageFromHistory();
                }
            }
        });

        historyList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedPageFromHistory();
                }
            }
        });

        historyDialog.setSize(400, 300);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setVisible(true);
    }

    private void openSelectedPageFromHistory() {
        int selectedIndex = historyList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < historyListModel.getSize()) {
            String selectedUrl = historyListModel.getElementAt(selectedIndex);
            loadURL(selectedUrl);
        }
    }

    private void createScene() {

        Platform.runLater(() -> {

            WebView view = new WebView();
            engine = view.getEngine();

            engine.titleProperty().addListener((observable, oldValue, newValue) -> SwingUtilities.invokeLater(() -> SimpleSwingBrowser.this.setTitle(newValue)));

            engine.setOnStatusChanged(event -> SwingUtilities.invokeLater(() -> lblStatus.setText(event.getData())));

            engine.locationProperty().addListener((ov, oldValue, newValue) -> SwingUtilities.invokeLater(() -> txtURL.setText(newValue)));

            engine.getLoadWorker().workDoneProperty().addListener((observableValue, oldValue, newValue) -> SwingUtilities.invokeLater(() -> progressBar.setValue(newValue.intValue())));

            engine.getLoadWorker()
                    .exceptionProperty()
                    .addListener((o, old, value) -> {
                        if (engine.getLoadWorker().getState() == FAILED) {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                                    panel,
                                    (value != null)
                                            ? engine.getLocation() + "\n" + value.getMessage()
                                            : engine.getLocation() + "\nUnexpected error.",
                                    "Loading error...",
                                    JOptionPane.ERROR_MESSAGE));
                        }
                    });

            jfxPanel.setScene(new Scene(view));
        });
    }

    public void loadURL(final String url) {
        Platform.runLater(() -> {
            String tmp = toURL(url);

            if (tmp == null) {
                tmp = toURL("https://" + url);
            }

            engine.load(tmp);
        });
    }

    private static String toURL(String str) {
        try {
            return new URL(str).toExternalForm();
        } catch (MalformedURLException exception) {
            return null;
        }
    }

    public JPanel getPanel() {
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimpleSwingBrowser browser = new SimpleSwingBrowser();
            browser.setExtendedState(JFrame.MAXIMIZED_BOTH);
            browser.setVisible(true);
            browser.loadURL("https://google.com");
        });
    }
}