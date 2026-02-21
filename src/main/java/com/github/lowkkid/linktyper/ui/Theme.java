package com.github.lowkkid.linktyper.ui;

import java.awt.*;

public final class Theme {

    private Theme() {}

    public static final Color BG         = new Color(18, 18, 20);
    public static final Color BG_SURFACE = new Color(26, 26, 30);
    public static final Color BG_INPUT   = new Color(36, 36, 40);
    public static final Color BORDER     = new Color(62, 62, 70);
    public static final Color ACCENT     = new Color(99, 102, 241);
    public static final Color ACCENT_HOV = new Color(118, 121, 255);
    public static final Color TEXT_PRI   = new Color(255, 255, 255);
    public static final Color TEXT_SEC   = new Color(180, 180, 195);
    public static final Color TEXT_MUTED = new Color(110, 110, 125);
    public static final Color DANGER     = new Color(220, 65, 65);
    public static final Color SELECTION  = new Color(99, 102, 241, 90);

    public static final Font FONT_UI     = new Font("Inter",          Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("Inter",          Font.PLAIN, 12);
    public static final Font FONT_MONO   = new Font("JetBrains Mono", Font.PLAIN, 13);

    public static final int ICON_SM  = 15;
    public static final int ICON_MD  = 17;
    public static final int ICON_LG  = 19;
}