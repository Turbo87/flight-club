package org.flightclub.graphics.android;

import org.flightclub.graphics.Font;
import org.flightclub.graphics.FontFactory;

public class AndroidFont implements Font {
    private final String name;
    private final FontFactory.Style style;
    private final int size;

    public AndroidFont(String name, FontFactory.Style style, int size) {
        this.name = name;
        this.style = style;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public FontFactory.Style getStyle() {
        return style;
    }

    public int getSize() {
        return size;
    }
}
