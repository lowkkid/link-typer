package com.github.lowkkid.linktyper.core;

import java.io.*;
import java.nio.file.*;

public class XBindKeysManager {

    private static final Path CONFIG_FILE = Path.of(System.getProperty("user.home"), ".xbindkeysrc");
    private static final Path SIGNAL_DIR  = Path.of(System.getProperty("user.home"), ".linktyper");

    public static void writeConfig(KeybindingConfig config) throws IOException {
        String content = String.format("""
            "%s"
              %s
                            
            "%s"
              %s
                            
            "%s"
              %s
            """,
                signalCmd("cmd_start"), formatCombo(config.getStartCombo()),
                signalCmd("cmd_pause"), formatCombo(config.getPauseCombo()),
                signalCmd("cmd_stop"),  formatCombo(config.getStopCombo())
        );
        System.out.println("[XBindKeys] writing config:\n" + content);
        Files.writeString(CONFIG_FILE, content);
    }

    public static void reloadOrStart() throws IOException, InterruptedException {
        // убиваем старый экземпляр если есть
        new ProcessBuilder("pkill", "xbindkeys").start().waitFor();
        Thread.sleep(300); // даём время завершиться

        // запускаем новый явно указывая конфиг
        ProcessBuilder pb = new ProcessBuilder("xbindkeys", "-f", CONFIG_FILE.toString());
        pb.inheritIO();
        pb.start();
        System.out.println("[XBindKeys] started with config: " + CONFIG_FILE);
    }

    private static String signalCmd(String signal) {
        return "touch " + SIGNAL_DIR + "/" + signal;
    }

    // formatCombo превращает "ctrl+shift+s" в формат xbindkeys: "control+shift+s"
    private static String formatCombo(String combo) {
        return combo
                .toLowerCase()
                .replace("ctrl", "control")
                .replace("+", " + ");
    }
}