package org.flightclub.compat;

public class AwtFont implements Font {
    private final java.awt.Font f;

    public AwtFont(String name, int style, int size) {
        this(new java.awt.Font(name, style, size));
    }

    public AwtFont(java.awt.Font f) {
        this.f = f;
    }

    public java.awt.Font getRaw() {
        return f;
    }
}
