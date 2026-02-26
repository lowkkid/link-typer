package com.github.lowkkid.linktyper.core;

public class KeybindingConfig {

    private String startCombo;
    private String pauseCombo;
    private String stopCombo;

    public KeybindingConfig() {
        startCombo = "control+alt+s";
        pauseCombo = "control+alt+p";
        stopCombo  = "control+alt+x";
    }

    public String getStartCombo() { return startCombo; }
    public String getPauseCombo() { return pauseCombo; }
    public String getStopCombo()  { return stopCombo; }

    public void setStartCombo(String combo) { startCombo = combo; }
    public void setPauseCombo(String combo) { pauseCombo = combo; }
    public void setStopCombo(String combo)  { stopCombo  = combo; }

    /** Human-readable label for status bar, e.g. "Ctrl+Shift+S" */
    public String getStartLabel() { return toLabel(startCombo); }
    public String getPauseLabel() { return toLabel(pauseCombo); }
    public String getStopLabel()  { return toLabel(stopCombo); }

    private String toLabel(String combo) {
        String[] parts = combo.split("\\+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append("+");
            sb.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}