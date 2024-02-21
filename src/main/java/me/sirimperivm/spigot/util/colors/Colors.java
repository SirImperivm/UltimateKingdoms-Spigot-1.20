package me.sirimperivm.spigot.util.colors;

import me.sirimperivm.spigot.util.colors.rgb.RGBColor;

@SuppressWarnings("all")
public class Colors {

    public static String translateString(String t) {
        return RGBColor.process(t);
    }
}
