package com.likz.agtests;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TerminalAG implements ToolWindowFactory {

    private static ToolWindow toolWindow;
    private static ContentManager contentManager;
    private static final String TOOL_WINDOW_ID = "AG Terminal";
    private static Content mainContent;

    private JPanel leftPanel;
    private JPanel rightPanel;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        createMainTab(project);
        toolWindow.getContentManager().addContent(mainContent);

        toolWindow.setStripeTitle("AutoGenTests");
        toolWindow.setAnchor(ToolWindowAnchor.BOTTOM, null);
    }

    public TerminalAG() {
    }

    public TerminalAG(Project project, String name) {
        TerminalAG.toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
        TerminalAG.contentManager = toolWindow.getContentManager();
        contentManager.removeContent(mainContent, true);

        leftPanel = createLeftPanel();
        rightPanel = createRightPanel();
        JScrollPane scrollPane = new JBScrollPane(rightPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, scrollPane);
        splitPane.setDividerLocation(300);

        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(splitPane, "Creating test for " + name, true);
        content.setCloseable(true);

        contentManager.addContent(content);
        contentManager.addContentManagerListener(new ContentManagerAdapter() {
            @Override
            public void contentRemoved(ContentManagerEvent event) {
                if (contentManager.getContentCount() == 0) {
                    contentManager.addContent(mainContent);
                }
            }
        });

        openToolWindow(project);
        contentManager.setSelectedContent(content);
    }

    // Метод для программного открытия ToolWindow
    public static void openToolWindow(Project project) {
        if (toolWindow != null) {
            toolWindow.show();
        }
    }

    private static void createMainTab(Project project) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("There are no actions to show");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setBorder(new EmptyBorder(10, 0, 20, 0));

        JLabel interactiveLabel = new JLabel("Click to open IDE plugin settings");
        interactiveLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        interactiveLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        interactiveLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Tools/AutoGen Settings");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                interactiveLabel.setText("<html><u>" + interactiveLabel.getText() + "</u></html>");
                interactiveLabel.setHorizontalAlignment(SwingConstants.CENTER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                interactiveLabel.setText(interactiveLabel.getText()
                        .replaceAll("<html><u>", "")
                        .replaceAll("</u></html>", ""));
            }
        });

        panel.add(label);
        panel.add(interactiveLabel);

        ContentFactory contentFactory = ContentFactory.getInstance();
        mainContent = contentFactory.createContent(panel, "", false);
        mainContent.setCloseable(false);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel label1 = new JLabel("Project preparation");
        label1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        label1.setOpaque(true);
        label1.setBackground(JBColor.LIGHT_GRAY);
        JLabel label2 = new JLabel("Working with tests");
        label2.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        panel.add(label1);
        panel.add(label2);

        return panel;
    }

    public void finishLeftPanel_1() {
        JLabel leftComponent = (JLabel) leftPanel.getComponents()[0];
        leftComponent.setIcon(AllIcons.General.InspectionsOK);
        leftComponent.setBackground(null);
        JLabel nextComponent = (JLabel) leftPanel.getComponents()[1];
        nextComponent.setBackground(JBColor.YELLOW);
    }

    public void finishLeftPanel_2() {
        JLabel leftComponent = (JLabel) leftPanel.getComponents()[1];
        leftComponent.setIcon(AllIcons.General.InspectionsOK);
        leftComponent.setBackground(null);
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        return panel;
    }

    public void addToRightPanel(String text) {
        RightPanelComponent component = new RightPanelComponent(text, AllIcons.Process.Step_1);
        rightPanel.add(component);

        rightPanel.revalidate();
        rightPanel.repaint();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

    public void pauseComponentRightPanel(String name) {
        for (Component component : rightPanel.getComponents()) {
            if (component instanceof RightPanelComponent) {
                RightPanelComponent rightPanelComponent = (RightPanelComponent) component;
                if (rightPanelComponent.textLabel.getText().equals(name)) {
                    rightPanelComponent.statusLabel.setIcon(AllIcons.Actions.Pause);
                    break;
                }
            }
        }
    }

    public void cancelComponentRightPanel(String name) {
        for (Component component : rightPanel.getComponents()) {
            if (component instanceof RightPanelComponent) {
                RightPanelComponent rightPanelComponent = (RightPanelComponent) component;
                if (rightPanelComponent.textLabel.getText().equals(name)) {
                    rightPanelComponent.statusLabel.setIcon(AllIcons.Actions.Cancel);
                    break;
                }
            }
        }
    }

    public void finishComponentRightPanel(Integer position) {
        Component component = rightPanel.getComponent(position);
        if (component instanceof RightPanelComponent) {
            RightPanelComponent rightPanelComponent = (RightPanelComponent) component;
            rightPanelComponent.statusLabel.setIcon(AllIcons.General.InspectionsOK);
        }
    }

    public void finishComponentRightPanel(String name) {
        for (Component component : rightPanel.getComponents()) {
            if (component instanceof RightPanelComponent) {
                RightPanelComponent rightPanelComponent = (RightPanelComponent) component;
                if (rightPanelComponent.textLabel.getText().equals(name)) {
                    rightPanelComponent.statusLabel.setIcon(AllIcons.General.InspectionsOK);
                    break;
                }
            }
        }
    }

    public void errorComponentRightPanel(String name) {
        for (Component component : rightPanel.getComponents()) {
            if (component instanceof RightPanelComponent) {
                RightPanelComponent rightPanelComponent = (RightPanelComponent) component;
                if (rightPanelComponent.textLabel.getText().equals(name)) {
                    rightPanelComponent.statusLabel.setIcon(AllIcons.General.NotificationError);
                    break;
                }
            }
        }
    }

    class RightPanelComponent extends JPanel {
        private JLabel timeLabel;
        private JLabel statusLabel;
        private JLabel textLabel;

        public RightPanelComponent(String text, Icon statusIcon) {
            setLayout(new FlowLayout(FlowLayout.LEFT));

            this.timeLabel = new JLabel(new SimpleDateFormat("HH:mm:ss").format(new Date()));
            this.statusLabel = new JLabel();
            statusLabel.setIcon(statusIcon);
            this.textLabel = new JLabel(text);

            add(timeLabel);
            add(statusLabel);
            add(textLabel);
        }
    }

}
