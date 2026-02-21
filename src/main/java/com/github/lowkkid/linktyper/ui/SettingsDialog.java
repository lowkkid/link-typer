package com.github.lowkkid.linktyper.ui;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign2.*;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import static com.github.lowkkid.linktyper.ui.Theme.*;

public class SettingsDialog extends JDialog {

    private KeybindField startField;
    private KeybindField pauseField;
    private KeybindField stopField;

    private final MainFrame owner;

    public SettingsDialog(MainFrame owner) {
        super(owner, "Settings", true);
        this.owner = owner;
        setUndecorated(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        root.add(buildTitleBar(), BorderLayout.NORTH);
        root.add(buildContent(),  BorderLayout.CENTER);
        root.add(buildButtons(),  BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_SURFACE);
        bar.setBorder(new EmptyBorder(11, 16, 11, 12));

        JLabel title = new JLabel(
                "Keybindings",
                FontIcon.of(MaterialDesignK.KEYBOARD_OUTLINE, ICON_MD, TEXT_SEC),
                JLabel.LEFT);
        title.setForeground(TEXT_PRI);
        title.setFont(FONT_UI.deriveFont(Font.BOLD, 14f));
        title.setIconTextGap(8);

        JButton closeBtn = iconButton(FontIcon.of(MaterialDesignW.WINDOW_CLOSE, ICON_MD, TEXT_SEC));
        closeBtn.addActionListener(e -> dispose());
        hoverIconColor(closeBtn, DANGER);

        bar.add(title,    BorderLayout.WEST);
        bar.add(closeBtn, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(18, 18, 10, 18));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(6, 0, 6, 16);

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill    = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets  = new Insets(6, 0, 6, 0);

        startField = new KeybindField("Ctrl+Shift+S");
        pauseField = new KeybindField("Ctrl+Shift+P");
        stopField  = new KeybindField("Ctrl+Shift+X");

        addRow(panel, MaterialDesignP.PLAY_CIRCLE_OUTLINE,  "Start", startField, lc, fc, 0);
        addRow(panel, MaterialDesignP.PAUSE_CIRCLE_OUTLINE, "Pause", pauseField, lc, fc, 1);
        addRow(panel, MaterialDesignS.STOP_CIRCLE_OUTLINE,  "Stop",  stopField,  lc, fc, 2);

        JLabel hint = new JLabel("Click a field, then press your desired key combo");
        hint.setFont(FONT_SMALL.deriveFont(11f));
        hint.setForeground(TEXT_SEC);
        GridBagConstraints hc = new GridBagConstraints();
        hc.gridx = 0; hc.gridy = 3; hc.gridwidth = 2;
        hc.insets = new Insets(10, 0, 0, 0);
        panel.add(hint, hc);

        return panel;
    }

    private void addRow(JPanel panel, Ikon ikon, String text,
                        KeybindField field,
                        GridBagConstraints lc, GridBagConstraints fc, int row) {
        JLabel lbl = new JLabel(text, FontIcon.of(ikon, ICON_SM, TEXT_SEC), JLabel.LEFT);
        lbl.setFont(FONT_UI);
        lbl.setForeground(TEXT_PRI);
        lbl.setIconTextGap(7);

        lc.gridx = 0; lc.gridy = row;
        fc.gridx = 1; fc.gridy = row;
        panel.add(lbl,   lc);
        panel.add(field, fc);
    }

    private JPanel buildButtons() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        bar.setBackground(BG);
        bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));

        JButton cancel = new JButton("Cancel");
        styleSecondary(cancel);
        cancel.addActionListener(e -> dispose());

        JButton save = new JButton("Save", FontIcon.of(MaterialDesignC.CHECK, ICON_SM, Color.WHITE));
        styleAccent(save);
        save.addActionListener(e -> onSave());

        bar.add(cancel);
        bar.add(save);
        return bar;
    }

    private void onSave() {
        owner.updateKeybindLabels(
                startField.getText(),
                pauseField.getText(),
                stopField.getText());
        dispose();
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

    private void styleAccent(JButton btn) {
        btn.setFont(FONT_UI.deriveFont(Font.BOLD));
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT);
        btn.setBorder(new EmptyBorder(6, 16, 6, 16));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setIconTextGap(7);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color normal = ACCENT;
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(ACCENT_HOV); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(normal); }
        });
    }

    private void styleSecondary(JButton btn) {
        btn.setFont(FONT_UI);
        btn.setForeground(TEXT_SEC);
        btn.setBackground(BG_INPUT);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(5, 14, 5, 14)));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

    static class KeybindField extends JTextField {

        KeybindField(String initial) {
            super(initial, 16);
            setEditable(false);
            setBackground(BG_INPUT);
            setForeground(TEXT_PRI);
            setFont(FONT_MONO.deriveFont(12f));
            setHorizontalAlignment(CENTER);
            applyBorder(BORDER);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { startListening(); }
            });
            addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) { captureCombo(e); }
            });
            addFocusListener(new FocusAdapter() {
                public void focusLost(FocusEvent e) { stopListening(); }
            });
        }

        private void startListening() {
            setBackground(new Color(28, 28, 48));
            applyBorder(ACCENT);
            setText("Press keys…");
            setForeground(TEXT_MUTED);
            requestFocusInWindow();
        }

        private void stopListening() {
            setBackground(BG_INPUT);
            setForeground(TEXT_PRI);
            applyBorder(BORDER);
        }

        private void applyBorder(Color color) {
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color, 1),
                    new EmptyBorder(5, 10, 5, 10)));
        }

        private void captureCombo(KeyEvent e) {
            int mod = e.getModifiersEx();
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_CONTROL || key == KeyEvent.VK_SHIFT
                    || key == KeyEvent.VK_ALT || key == KeyEvent.VK_META) return;

            StringBuilder sb = new StringBuilder();
            if ((mod & KeyEvent.CTRL_DOWN_MASK)  != 0) sb.append("Ctrl+");
            if ((mod & KeyEvent.SHIFT_DOWN_MASK) != 0) sb.append("Shift+");
            if ((mod & KeyEvent.ALT_DOWN_MASK)   != 0) sb.append("Alt+");
            sb.append(KeyEvent.getKeyText(key));

            setText(sb.toString());
            stopListening();
            transferFocus();
        }
    }
}