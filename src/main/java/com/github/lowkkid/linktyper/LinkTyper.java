package com.github.lowkkid.linktyper;

import com.formdev.flatlaf.FlatDarkLaf;
import com.github.lowkkid.linktyper.ui.MainFrame;
import javax.swing.*;

public class LinkTyper {
    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(MainFrame::new);
    }
}