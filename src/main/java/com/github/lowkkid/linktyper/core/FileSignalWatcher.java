package com.github.lowkkid.linktyper.core;

import java.io.IOException;
import java.nio.file.*;

public class FileSignalWatcher {

    private static final Path WATCH_DIR = Path.of(System.getProperty("user.home"), ".linktyper");

    private final Runnable onStart;
    private final Runnable onPause;
    private final Runnable onStop;
    private Thread watchThread;

    public FileSignalWatcher(Runnable onStart, Runnable onPause, Runnable onStop) {
        this.onStart = onStart;
        this.onPause = onPause;
        this.onStop  = onStop;
    }

    public void start() {
        try {
            Files.createDirectories(WATCH_DIR);
            System.out.println("[Watcher] watching: " + WATCH_DIR);
        } catch (IOException e) {
            System.err.println("[Watcher] cannot create dir: " + e.getMessage());
            return;
        }

        watchThread = new Thread(() -> {
            try (WatchService ws = FileSystems.getDefault().newWatchService()) {
                WATCH_DIR.register(ws, StandardWatchEventKinds.ENTRY_CREATE);
                System.out.println("[Watcher] started");
                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key = ws.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        String name = event.context().toString();
                        System.out.println("[Watcher] signal: " + name);
                        Path signal = WATCH_DIR.resolve(name);
                        Files.deleteIfExists(signal);
                        switch (name) {
                            case "cmd_start" -> { System.out.println("[Watcher] START"); onStart.run(); }
                            case "cmd_pause" -> { System.out.println("[Watcher] PAUSE"); onPause.run(); }
                            case "cmd_stop"  -> { System.out.println("[Watcher] STOP");  onStop.run(); }
                            default -> System.out.println("[Watcher] unknown signal: " + name);
                        }
                    }
                    key.reset();
                }
            } catch (InterruptedException e) {
                System.out.println("[Watcher] interrupted");
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                System.err.println("[Watcher] error: " + e.getMessage());
            }
        }, "signal-watcher");
        watchThread.setDaemon(true);
        watchThread.start();
    }

    public void stop() {
        if (watchThread != null) watchThread.interrupt();
    }
}