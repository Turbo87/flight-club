package org.flightclub.compat;

public class Font {
    private java.awt.Font f;

    /**
     * The plain style constant.
     */
    public static final int PLAIN       = 0;

    /**
     * The bold style constant.  This can be combined with the other style
     * constants (except PLAIN) for mixed styles.
     */
    public static final int BOLD        = 1;

    /**
     * The italicized style constant.  This can be combined with the other
     * style constants (except PLAIN) for mixed styles.
     */
    public static final int ITALIC      = 2;

    public Font(String name, int style, int size) {
        this(new java.awt.Font(name, style, size));
    }

    public Font(java.awt.Font f) {
        this.f = f;
    }

    public java.awt.Font getFont() {
        return f;
    }
}
