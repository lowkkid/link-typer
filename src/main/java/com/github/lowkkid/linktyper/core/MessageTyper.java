package com.github.lowkkid.linktyper.core;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.KeyEvent;
import java.util.Random;

public class MessageTyper {

    private enum State { IDLE, TYPING, PAUSED }

    private volatile State state = State.IDLE;
    private volatile int   position = 0;
    private String         text = "";
    private Thread         typingThread;

    private final Robot  robot;
    private final Random random = new Random();

    // delay between keystrokes: random between MIN and MAX ms
    private static final int DELAY_MIN = 40;
    private static final int DELAY_MAX = 120;

    public MessageTyper() throws AWTException {
        this.robot = new Robot();
    }

    // ── public API ─────────────────────────────────────────────────────────

    public synchronized void start(String message) {
        switch (state) {
            case IDLE -> {
                text     = message;
                position = 0;
                state    = State.TYPING;
                startThread();
            }
            case PAUSED -> {
                state = State.TYPING;
                startThread();
            }
            case TYPING -> { }
        }
    }

    private void startThread() {
        typingThread = new Thread(() -> {
            try {
                Thread.sleep(300); // ждём пока отпустят хоткей
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            typeLoop();
        }, "linktyper-thread");
        typingThread.setDaemon(true);
        typingThread.start();
    }

    public synchronized void pause() {
        if (state == State.TYPING) {
            state = State.PAUSED;
            interruptThread();
        }
    }

    public synchronized void stop() {
        state    = State.IDLE;
        position = 0;
        text     = "";
        interruptThread();
    }

    public boolean isIdle() {
        return state == State.IDLE;
    }


    private void interruptThread() {
        if (typingThread != null) {
            typingThread.interrupt();
            typingThread = null;
        }
    }

    private void typeLoop() {
        while (true) {
            int pos;
            synchronized (this) {
                if (state != State.TYPING) return;
                if (position >= text.length()) {
                    state    = State.IDLE;
                    position = 0;
                    return;
                }
                pos = position++;
            }

            char c = text.charAt(pos);
            typeChar(c);

            try {
                int delay = DELAY_MIN + random.nextInt(DELAY_MAX - DELAY_MIN);
                // slightly longer pause after punctuation — feels more natural
                if (c == '.' || c == ',' || c == '!' || c == '?' || c == ':') {
                    delay += 80;
                }
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void typeChar(char c) {
        // For non-ASCII characters (Cyrillic, accented, etc.) we use clipboard
        // paste trick — put single char to clipboard and send Ctrl+V.
        // This works reliably across all layouts and languages.
        if (c > 127) {
            pasteChar(c);
            return;
        }

        // ASCII — use Robot key events
        switch (c) {
            case ' '  -> tap(KeyEvent.VK_SPACE);
            case '\n' -> tap(KeyEvent.VK_ENTER);
            case '\t' -> tap(KeyEvent.VK_TAB);
            default   -> typeAscii(c);
        }
    }

    private void typeAscii(char c) {
        Integer[] keys = asciiToKeys(c);
        if (keys == null) {
            // fallback to clipboard for unknown chars
            pasteChar(c);
            return;
        }
        if (keys.length == 2) {
            robot.keyPress(keys[0]);
            robot.keyPress(keys[1]);
            robot.keyRelease(keys[1]);
            robot.keyRelease(keys[0]);
        } else {
            robot.keyPress(keys[0]);
            robot.keyRelease(keys[0]);
        }
    }

    /**
     * Paste a single character via clipboard.
     * Saves/restores previous clipboard contents.
     */
    private void pasteChar(char c) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        // save current clipboard
        Transferable previous = null;
        try {
            previous = clipboard.getContents(null);
        } catch (Exception ignored) {}

        // put our char
        StringSelection ss = new StringSelection(String.valueOf(c));
        clipboard.setContents(ss, null);

        robot.delay(20);

        // Ctrl+V
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);

        robot.delay(20);

        // restore previous clipboard
        if (previous != null) {
            try {
                clipboard.setContents(previous, null);
            } catch (Exception ignored) {}
        }
    }

    private void tap(int keyCode) {
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
    }

    /**
     * Maps ASCII printable characters to Robot key sequences.
     * Returns {keyCode} for plain keys, {VK_SHIFT, keyCode} for shifted ones.
     */
    private Integer[] asciiToKeys(char c) {
        if (c >= 'a' && c <= 'z') return new Integer[]{KeyEvent.VK_A + (c - 'a')};
        if (c >= 'A' && c <= 'Z') return new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_A + (c - 'A')};
        if (c >= '0' && c <= '9') return new Integer[]{KeyEvent.VK_0 + (c - '0')};

        return switch (c) {
            case '`'  -> new Integer[]{KeyEvent.VK_BACK_QUOTE};
            case '~'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE};
            case '!'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_1};
            case '@'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_2};
            case '#'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_3};
            case '$'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_4};
            case '%'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_5};
            case '^'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_6};
            case '&'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_7};
            case '*'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_8};
            case '('  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_9};
            case ')'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_0};
            case '-'  -> new Integer[]{KeyEvent.VK_MINUS};
            case '_'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_MINUS};
            case '='  -> new Integer[]{KeyEvent.VK_EQUALS};
            case '+'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_EQUALS};
            case '['  -> new Integer[]{KeyEvent.VK_OPEN_BRACKET};
            case '{'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET};
            case ']'  -> new Integer[]{KeyEvent.VK_CLOSE_BRACKET};
            case '}'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET};
            case '\\' -> new Integer[]{KeyEvent.VK_BACK_SLASH};
            case '|'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH};
            case ';'  -> new Integer[]{KeyEvent.VK_SEMICOLON};
            case ':'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_SEMICOLON};
            case '\'' -> new Integer[]{KeyEvent.VK_QUOTE};
            case '"'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_QUOTE};
            case ','  -> new Integer[]{KeyEvent.VK_COMMA};
            case '<'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_COMMA};
            case '.'  -> new Integer[]{KeyEvent.VK_PERIOD};
            case '>'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD};
            case '/'  -> new Integer[]{KeyEvent.VK_SLASH};
            case '?'  -> new Integer[]{KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH};
            default   -> null;
        };
    }
}