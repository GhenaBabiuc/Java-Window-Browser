package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TabbedSwingBrowser extends JFrame {

    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final List<SimpleSwingBrowser> tabs = new ArrayList<>();

    public TabbedSwingBrowser() {
        super("Tabbed Swing Browser");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1024, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem newTabItem = new JMenuItem("New Tab");
        JMenuItem closeTabItem = new JMenuItem("Close Tab");

        newTabItem.addActionListener(e -> addTab("https://google.com"));
        closeTabItem.addActionListener(e -> closeTab());

        fileMenu.add(newTabItem);
        fileMenu.add(closeTabItem);
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        SwingUtilities.invokeLater(() -> {
            addTab("https://google.com");
            add(tabbedPane);
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        });
    }

    private void addTab(String initialUrl) {
        SimpleSwingBrowser browserTab = new SimpleSwingBrowser();
        tabs.add(browserTab);
        tabbedPane.addTab("New Tab", browserTab.getPanel());
        tabbedPane.setSelectedIndex(tabs.size() - 1);
        browserTab.loadURL(initialUrl);
    }

    private void closeTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < tabs.size()) {
            tabs.remove(selectedIndex);
            tabbedPane.remove(selectedIndex);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TabbedSwingBrowser::new);
    }
}