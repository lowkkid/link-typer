package com.github.lowkkid.linktyper;

import com.github.lowkkid.linktyper.core.*;
import com.github.lowkkid.linktyper.ui.MainFrame;

import java.awt.*;
import javax.swing.*;

public class LinkTyper {

    public static void main(String[] args) throws AWTException {
        KeybindingConfig config = ConfigManager.load();
        MessageTyper     typer  = new MessageTyper();

        MainFrame[] mainFrame = new MainFrame[1];

        FileSignalWatcher watcher = new FileSignalWatcher(
                () -> {
                    if (mainFrame[0] == null) return;
                    String text = mainFrame[0].getMessage();
                    if (!text.isBlank()) typer.start(text);
                },
                typer::pause,
                typer::stop
        );
        watcher.start();

        // записываем xbindkeys конфиг и запускаем/перезапускаем xbindkeys
        try {
            XBindKeysManager.writeConfig(config);
            XBindKeysManager.reloadOrStart();
        } catch (Exception e) {
            System.err.println("xbindkeys init failed: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> mainFrame[0] = new MainFrame(config));

        Runtime.getRuntime().addShutdownHook(new Thread(watcher::stop));
    }
}