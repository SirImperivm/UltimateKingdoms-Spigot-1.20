package me.sirimperivm.spigot.util.colors.rgb.patterns;

import me.sirimperivm.spigot.util.colors.rgb.RGBColor;

import java.util.regex.Matcher;

@SuppressWarnings("all")
public class Solid implements Pattern {
    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("->S:([0-9A-Fa-f]{6})/|#\\{([0-9A-Fa-f]{6})}");

    public String process(String string) {
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            String color = matcher.group(1);
            if (color == null) color = matcher.group(2);

            string = string.replace(matcher.group(), RGBColor.getColor(color) + "");
        }
        return string;
    }
}