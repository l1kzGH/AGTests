package com.likz.agtests.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

public class ConfigAG implements Configurable {
    static public final String URL = "http://localhost:8000/gnrt/v1";
    static public final String pluginDataDir = PathManager.getPluginsPath() + "/AGTests/data/user_settings.json";
    static public PluginSettings pluginSettings = new PluginSettings();

    static private boolean canBeSaved = true;
    private JPanel panel;

    static private JCheckBox directoryCheckBox;
    static private JTextField newDirectField;
    static private ComboBox<String> frameworkBox;
    static private JTextField nameField;
    static private ComboBox<String> patternBox;

    @Override
    public String getDisplayName() {
        return "AutoGen Settings";
    }

    @Override
    public @Nullable JComponent createComponent() {
        panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));

        // Directory changing
        JPanel directoryPanel = createDirectoryPanel();
        // New Class Name
        JPanel classPanel = createClassNamePanel();
        // New Method's name
        JPanel methodsPanel = createMethodNamePanel();
        // Extract
        JPanel border1 = borderPanel("Project setup", directoryPanel, classPanel, methodsPanel);
        panel.add(border1);

        // The framework selection point
        JPanel frameworkPanel = createFrameworkPanel();
        // Using pattern
        JPanel patternPanel = createPatternPanel();
        // Extract
        JPanel border2 = borderPanel("AI setup", frameworkPanel, patternPanel, new JPanel());
        panel.add(border2);

        SwingUtilities.invokeLater(this::readAndApplySettings);

        return panel;
    }

    @Override
    public boolean isModified() {
        if (directoryCheckBox.isSelected()) {
            return true;
        }
        if (frameworkBox.getSelectedIndex() != 0) {
            return true;
        }
        if (!nameField.getText().equals("[class_name]Test")) {
            return true;
        }
        if (patternBox.getSelectedIndex() != 0) {
            return true;
        }
        return false;
    }

    @Override
    public void apply() {
        if (canBeSaved) {
            PluginSettings settings = new PluginSettings();

            if (!newDirectField.getText().equals("")) {
                settings.setCustomerDirectory(newDirectField.getText());
            }
            if (frameworkBox.getSelectedIndex() != 0) {
                settings.setCustomerFramework(frameworkBox.getSelectedIndex());
            }
            if (!nameField.getText().equals("[class_name]Test")) {
                settings.setCustomerTestName(nameField.getText());
            }
            if (patternBox.getSelectedIndex() != 0) {
                settings.setGenerationPattern(patternBox.getSelectedIndex());
            }

            writeSettings(settings);
            pluginSettings = settings;
        }
    }

    @Override
    public void reset() {
        directoryCheckBox.setSelected(false);
        newDirectField.setEnabled(false);
        newDirectField.setText("");
        frameworkBox.setSelectedIndex(0);
        nameField.setText("[class_name]Test");
        patternBox.setSelectedIndex(0);
    }

    private void writeSettings(PluginSettings settings) {
        ObjectMapper objectMapper = new ObjectMapper();
        File directory = new File(PathManager.getPluginsPath() + "/AGTests/data");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, "user_settings.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            String json = objectMapper.writeValueAsString(settings);
            System.out.println(json);
            objectMapper.writeValue(file, json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JPanel borderPanel(String title, JPanel ... panels){
        Border lineBorder = BorderFactory.createLineBorder(JBColor.LIGHT_GRAY);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(lineBorder, title);
        titledBorder.setTitleFont(new Font("SansSerif", Font.BOLD, 12));

        JPanel borderPanel = new JPanel();
        borderPanel.setBorder(titledBorder);
        borderPanel.setLayout(new GridLayout(panels.length, 1));
        for (JPanel panel : panels) {
            borderPanel.add(panel);
        }

        return borderPanel;
    }

    private JPanel createDirectoryPanel() {
        JPanel directoryPanel = new JPanel();
        directoryPanel.setLayout(new GridLayout(3, 1));

        JLabel directoryLabel = new JLabel("Change test directory location");
        // directoryLabel.setFont(new Font("Arial", Font.BOLD, 12));

        directoryCheckBox = new JCheckBox("Enter new directory");

        newDirectField = new JTextField();
        newDirectField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = newDirectField.getText();
                if (!isValidPath(text)) {
                    canBeSaved = false;
                    newDirectField.setForeground(JBColor.RED);
                } else {
                    canBeSaved = true;
                    newDirectField.setForeground(JBColor.BLACK);
                }
            }
        });
        newDirectField.setEnabled(false);

        directoryPanel.add(directoryLabel);
        directoryPanel.add(directoryCheckBox);
        directoryPanel.add(newDirectField);

        directoryCheckBox.addActionListener(x -> {
            boolean selected = directoryCheckBox.isSelected();
            newDirectField.setEnabled(selected);
            if (selected) {
                newDirectField.setText("src/test/java");
                newDirectField.requestFocusInWindow(); // Делаем фокус на текстовом поле
            } else {
                newDirectField.setText("");
            }
        });

        directoryPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        return directoryPanel;
    }

    private boolean isValidPath(String path) {
        // Паттерн для разрешенных символов в пути
        String pattern = "^[a-zA-Z/\\-]*$";

        // Проверка с помощью регулярного выражения
        return path.matches(pattern);
    }

    private JPanel createFrameworkPanel() {
        JPanel frameworkPanel = new JPanel();
        frameworkPanel.setLayout(new GridLayout(3, 1));

        // Header adding
        JLabel frameworkLabel = new JLabel("Java Test Framework");
        // frameworkLabel.setFont(new Font("Arial", Font.BOLD, 12));

        // Options select
        String[] frameworkOptions = {"Auto", "JUnit"/*, "TestNG"*/};
        frameworkBox = new ComboBox<>(frameworkOptions);
        frameworkBox.setMaximumSize(new Dimension(100, 100));
        frameworkBox.setEnabled(true);

        // Label
        JLabel label = new JLabel("Java framework responsible for generating unit tests");
        label.setFont(new Font("Yu Gothic UI", Font.PLAIN, 13));

        frameworkPanel.add(frameworkLabel);
        frameworkPanel.add(frameworkBox);
        frameworkPanel.add(label);

        frameworkPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        return frameworkPanel;
    }

    private JPanel createClassNamePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));

        JLabel nameLabel = new JLabel("Class name");
        //nameLabel.setFont(new Font("Arial", Font.BOLD, 12));

        nameField = new JTextField("[class_name]Test");
        nameField.setSize(200, 100);
        nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = nameField.getText();
                if (!isValidText(text, 1)) {
                    canBeSaved = false;
                    nameField.setForeground(JBColor.RED);
                } else {
                    canBeSaved = true;
                    nameField.setForeground(JBColor.BLACK);
                }
            }
        });

        // Label
        JLabel label = new JLabel("The name can only be changed inside [ ]");
        label.setFont(new Font("Yu Gothic UI", Font.PLAIN, 13));

        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(label);

        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        return panel;
    }

    private JPanel createPatternPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));

        // Header adding
        JLabel patternLabel = new JLabel("Generation pattern");
        //patternLabel.setFont(new Font("Arial", Font.BOLD, 12));

        // Options select
        String[] frameworkOptions = {"No pattern", "Arrange-Act-Assert", "Given-When-Then"};
        patternBox = new ComboBox<>(frameworkOptions);
        patternBox.setMaximumSize(new Dimension(200, 100));
        patternBox.setEnabled(true);

        // Label
        JLabel label = new JLabel("Pattern choosing for building the tests structure");
        label.setFont(new Font("Yu Gothic UI", Font.PLAIN, 13));

        panel.add(patternLabel);
        panel.add(patternBox);
        panel.add(label);

        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        return panel;
    }

    private JPanel createMethodNamePanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));

        JLabel methodLabel = new JLabel("Method's name");
        //nameLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JTextField methodField = new JTextField("test[method_name]");
        methodField.setSize(200, 100);
        methodField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = methodField.getText();
                if (!isValidText(text, 2)) {
                    canBeSaved = false;
                    methodField.setForeground(JBColor.RED);
                } else {
                    canBeSaved = true;
                    methodField.setForeground(JBColor.BLACK);
                }
            }
        });

        // Label
        JLabel label = new JLabel("The name can only be changed inside [ ]");
        label.setFont(new Font("Yu Gothic UI", Font.PLAIN, 13));

        panel.add(methodLabel);
        panel.add(methodField);
        panel.add(label);

        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        return panel;
    }

    private boolean isValidText(String text, int idPattern) {
        int openBracketIndex = text.indexOf('[');
        int closeBracketIndex = text.indexOf(']');

        if (openBracketIndex == -1 || closeBracketIndex == -1 || openBracketIndex >= closeBracketIndex) {
            return false;
        }

        String insideBrackets = text.substring(openBracketIndex + 1, closeBracketIndex);

        if (idPattern == 1) {
            return !insideBrackets.isEmpty() && text.equals("[" + insideBrackets + "]Test");
        } else {
            return !insideBrackets.isEmpty() && text.equals("test[" + insideBrackets + "]");
        }
    }

    private JLabel createResetLabel() {
        JLabel label = new JLabel("Reset");
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.setForeground(JBColor.BLUE);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Обработка клика
                System.out.println("too");
            }
        });

        label.setVisible(false);
        return label;
    }

    private void readAndApplySettings() {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(ConfigAG.pluginDataDir);
        if (file.exists())
            try {
                String json = objectMapper.readValue(file, String.class);
                pluginSettings = objectMapper.readValue(json, PluginSettings.class);
                if (!pluginSettings.equals(new PluginSettings())) {
                    if (!pluginSettings.getCustomerDirectory().isEmpty()) {
                        directoryCheckBox.setSelected(true);
                        newDirectField.setEnabled(true);
                        newDirectField.setText(pluginSettings.getCustomerDirectory());
                    }
                    frameworkBox.setSelectedIndex(pluginSettings.getCustomerFramework());
                    nameField.setText(pluginSettings.getCustomerTestName());
                    patternBox.setSelectedIndex(pluginSettings.getGenerationPattern());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }

}
