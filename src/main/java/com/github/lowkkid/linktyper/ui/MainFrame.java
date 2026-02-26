package com.github.lowkkid.linktyper.ui;

import com.github.lowkkid.linktyper.core.KeybindingConfig;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign2.*;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;

import static com.github.lowkkid.linktyper.ui.Theme.*;

public class MainFrame extends JFrame {

    private JTextField filePathField;
    private JButton    browseButton;
    private JButton    generateButton;
    private JTextArea  messageArea;
    private JLabel     startBindLabel;
    private JLabel     pauseBindLabel;
    private JLabel     stopBindLabel;

    private final KeybindingConfig    config;

    public MainFrame(KeybindingConfig config) {
        this.config  = config;
        setTitle("LinkTyper");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        root.add(buildTitleBar(),  BorderLayout.NORTH);
        root.add(buildCenter(),    BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        enableDrag(root);
        setVisible(true);
    }

    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_SURFACE);
        bar.setBorder(new EmptyBorder(11, 16, 11, 12));

        JLabel title = new JLabel("LinkTyper");
        title.setForeground(TEXT_PRI);
        title.setFont(FONT_UI.deriveFont(Font.BOLD, 14f));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        controls.setOpaque(false);

        JButton settingsBtn = iconButton(FontIcon.of(MaterialDesignC.COG_OUTLINE, ICON_MD, TEXT_SEC));
        JButton closeBtn    = iconButton(FontIcon.of(MaterialDesignW.WINDOW_CLOSE, ICON_MD, TEXT_SEC));

        settingsBtn.addActionListener(e -> openSettings());
        closeBtn.addActionListener(e -> System.exit(0));
        hoverIconColor(closeBtn, DANGER);
        hoverIconColor(settingsBtn, TEXT_PRI);

        controls.add(settingsBtn);
        controls.add(closeBtn);

        bar.add(title,    BorderLayout.WEST);
        bar.add(controls, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(BG);
        center.setBorder(new EmptyBorder(14, 14, 10, 14));

        JPanel fileRow = new JPanel(new BorderLayout(7, 0));
        fileRow.setOpaque(false);
        fileRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        filePathField = new JTextField();
        filePathField.setEditable(false);
        filePathField.setText("No file selected");
        styleTextField(filePathField);
        filePathField.setForeground(TEXT_MUTED);

        browseButton = new JButton(FontIcon.of(MaterialDesignF.FOLDER_OPEN_OUTLINE, ICON_SM, TEXT_SEC));
        styleBrowseButton(browseButton);
        browseButton.addActionListener(e -> browseFile());

        fileRow.add(filePathField, BorderLayout.CENTER);
        fileRow.add(browseButton,  BorderLayout.EAST);

        generateButton = new JButton("Generate",
                FontIcon.of(MaterialDesignS.SHUFFLE_VARIANT, ICON_SM, Color.WHITE));
        styleAccentButton(generateButton);
        generateButton.setEnabled(false);

        JPanel genRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        genRow.setOpaque(false);
        genRow.add(generateButton);

        messageArea = new JTextArea(8, 36);
        messageArea.setFont(FONT_MONO);
        messageArea.setBackground(BG_INPUT);
        messageArea.setForeground(TEXT_PRI);
        messageArea.setCaretColor(ACCENT);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        messageArea.setSelectedTextColor(Color.WHITE);
        messageArea.setSelectionColor(SELECTION);

        JScrollPane scroll = new JScrollPane(messageArea);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scroll.getViewport().setBackground(BG_INPUT);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        center.add(fileRow);
        center.add(Box.createVerticalStrut(7));
        center.add(genRow);
        center.add(Box.createVerticalStrut(7));
        center.add(scroll);
        center.add(Box.createVerticalStrut(10));
        center.add(buildHint());
        return center;
    }

    private JPanel buildHint() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(28, 28, 36));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        panel.add(hintStep("1", MaterialDesignT.TEXT_BOX_OUTLINE,
                "Write your message or generate one from a file"));
        panel.add(Box.createVerticalStrut(5));
        panel.add(hintStep("2", MaterialDesignC.CURSOR_DEFAULT_OUTLINE,
                "Click the input field where you want to type"));
        panel.add(Box.createVerticalStrut(5));
        panel.add(hintStep("3", MaterialDesignK.KEYBOARD_OUTLINE,
                "Press the configured keybinding to start typing"));

        return panel;
    }

    private JPanel hintStep(String num, Ikon ikon, String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        row.setOpaque(false);

        JLabel numLabel = new JLabel(num);
        numLabel.setFont(FONT_SMALL.deriveFont(Font.BOLD, 10f));
        numLabel.setForeground(ACCENT);
        numLabel.setPreferredSize(new Dimension(12, 16));

        JLabel icon = new JLabel(FontIcon.of(ikon, ICON_SM - 1, TEXT_MUTED));

        JLabel label = new JLabel(text);
        label.setFont(FONT_SMALL.deriveFont(11f));
        label.setForeground(TEXT_SEC);

        row.add(numLabel);
        row.add(icon);
        row.add(label);
        return row;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        bar.setBackground(BG_SURFACE);
        bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));

        startBindLabel = keybindLabel(MaterialDesignP.PLAY_CIRCLE_OUTLINE,  config.getStartLabel());
        pauseBindLabel = keybindLabel(MaterialDesignP.PAUSE_CIRCLE_OUTLINE, config.getPauseLabel());
        stopBindLabel  = keybindLabel(MaterialDesignS.STOP_CIRCLE_OUTLINE,  config.getStopLabel());

        bar.add(startBindLabel);
        bar.add(separator());
        bar.add(pauseBindLabel);
        bar.add(separator());
        bar.add(stopBindLabel);
        return bar;
    }

    private JLabel keybindLabel(Ikon ikon, String keys) {
        JLabel label = new JLabel(keys, FontIcon.of(ikon, ICON_SM, TEXT_SEC), JLabel.LEFT);
        label.setFont(FONT_SMALL);
        label.setForeground(TEXT_SEC);
        label.setIconTextGap(6);
        return label;
    }

    private JLabel separator() {
        JLabel sep = new JLabel("|");
        sep.setForeground(TEXT_MUTED);
        sep.setFont(FONT_UI);
        return sep;
    }

    private JButton iconButton(Icon icon) {
        JButton btn = new JButton(icon);
        btn.setBackground(null);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(28, 24));
        return btn;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRI);
        field.setCaretColor(ACCENT);
        field.setFont(FONT_SMALL);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(4, 10, 4, 10)));
    }

    private void styleBrowseButton(JButton btn) {
        btn.setBackground(BG_INPUT);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(4, 10, 4, 10)));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        hoverBg(btn, BG_SURFACE);
    }

    private void styleAccentButton(JButton btn) {
        btn.setFont(FONT_SMALL.deriveFont(Font.BOLD));
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT);
        btn.setBorder(new EmptyBorder(6, 16, 6, 16));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setIconTextGap(7);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        hoverBg(btn, ACCENT_HOV);
    }

    private void hoverBg(JButton btn, Color hover) {
        Color normal = btn.getBackground();
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(normal); }
        });
    }

    private void hoverIconColor(JButton btn, Color hover) {
        Icon original = btn.getIcon();
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (original instanceof FontIcon fi)
                    btn.setIcon(FontIcon.of(fi.getIkon(), fi.getIconSize(), hover));
            }
            public void mouseExited(MouseEvent e) { btn.setIcon(original); }
        });
    }

    private void enableDrag(JPanel root) {
        int[] origin = new int[2];
        root.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                origin[0] = e.getX(); origin[1] = e.getY();
            }
        });
        root.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - origin[0],
                        loc.y + e.getY() - origin[1]);
            }
        });
    }

    private void browseFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            filePathField.setForeground(TEXT_PRI);
            filePathField.setText(fc.getSelectedFile().getAbsolutePath());
            generateButton.setEnabled(true);
        }
    }

    private void openSettings() {
        new SettingsDialog(this, config).setVisible(true);
    }

    public void updateKeybindLabels(String start, String pause, String stop) {
        startBindLabel.setText(start);
        pauseBindLabel.setText(pause);
        stopBindLabel.setText(stop);
    }

    public String getMessage() {
        return messageArea.getText();
    }
}