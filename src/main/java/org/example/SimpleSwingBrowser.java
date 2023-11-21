package org.example;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

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

    public SimpleSwingBrowser() {
        super();
        initComponents();
    }

    private void initComponents() {
        createScene();

        ActionListener al = e -> loadURL(txtURL.getText());

        btnGo.addActionListener(al);
        txtURL.addActionListener(al);

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

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnBack);
        buttonPanel.add(btnForward);

        topBar.add(buttonPanel, BorderLayout.WEST);
        topBar.add(txtURL, BorderLayout.CENTER);
        topBar.add(btnGo, BorderLayout.EAST);

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimpleSwingBrowser browser = new SimpleSwingBrowser();
            browser.setExtendedState(JFrame.MAXIMIZED_BOTH);
            browser.setVisible(true);
            browser.loadURL("https://google.com");
        });
    }
}