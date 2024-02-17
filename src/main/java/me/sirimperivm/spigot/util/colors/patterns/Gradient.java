package me.sirimperivm.spigot.util.colors.patterns;

import me.sirimperivm.spigot.util.colors.RGB;

import java.awt.*;
import java.util.regex.Matcher;

@SuppressWarnings("all")
public class Gradient implements Pattern{

    java.util.regex.Pattern patt = java.util.regex.Pattern.compile("->G:([0-9A-Fa-f]{6})/(.*?)/->G:([0-9A-Fa-f]{6})");

    @Override
    public String process(String string) {
        Matcher match = patt.matcher(string);
        while (match.find()) {
            String start = match.group(1);
            String end = match.group(3);
            String content = match.group(2);
            string = string.replace(match.group(), RGB.color(content, new Color(Integer.parseInt(start, 16)), new Color(Integer.parseInt(end, 16))));
        }
        return string;
    }
}
