package me.sirimperivm.spigot.util;

import me.sirimperivm.spigot.util.rgbcolors.RGB;

@SuppressWarnings("all")
public class Colors {

    public static String translateString(String target) {
        return RGB.process(target);
    }
}
