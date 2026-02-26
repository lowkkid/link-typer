package com.github.lowkkid.linktyper.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.*;

public class ConfigManager {

    private static final Path CONFIG_DIR  = Path.of(System.getProperty("user.home"), ".linktyper");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");
    private static final Gson GSON        = new GsonBuilder().setPrettyPrinting().create();

    public static KeybindingConfig load() {
        if (!Files.exists(CONFIG_FILE)) {
            KeybindingConfig defaults = new KeybindingConfig();
            save(defaults);
            return defaults;
        }
        try (Reader r = Files.newBufferedReader(CONFIG_FILE)) {
            return GSON.fromJson(r, KeybindingConfig.class);
        } catch (IOException e) {
            return new KeybindingConfig();
        }
    }

    public static void save(KeybindingConfig config) {
        try {
            Files.createDirectories(CONFIG_DIR);
            try (Writer w = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(config, w);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}