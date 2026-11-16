package com.imageng.model;

import java.util.Arrays;
import java.util.List;

public enum AspectRatio {
    SQUARE("Square", "1:1", 1024, 1024),
    LANDSCAPE("Landscape", "16:9", 1024, 576),
    PORTRAIT("Portrait", "9:16", 576, 1024),
    WIDE("Wide", "21:9", 1024, 439);

    private final String name;
    private final String ratio;
    private final int width;
    private final int height;

    AspectRatio(String name, String ratio, int width, int height) {
        this.name = name;
        this.ratio = ratio;
        this.width = width;
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public String getRatio() {
        return ratio;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static AspectRatio fromString(String ratioStr) {
        if (ratioStr == null || ratioStr.isBlank()) {
            return SQUARE;
        }
        for (AspectRatio ar : values()) {
            if (ar.ratio.equalsIgnoreCase(ratioStr) || ar.name.equalsIgnoreCase(ratioStr)
                    || ar.name().equalsIgnoreCase(ratioStr)) {
                return ar;
            }
        }
        return SQUARE;
    }

    public static List<AspectRatio> getAll() {
        return Arrays.asList(values());
    }
}
