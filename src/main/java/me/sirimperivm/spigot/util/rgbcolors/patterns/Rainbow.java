package me.sirimperivm.spigot.util.rgbcolors.patterns;

import me.sirimperivm.spigot.util.rgbcolors.RGB;

import java.util.regex.Matcher;

@SuppressWarnings("all")
public class Rainbow implements Pattern{
    java.util.regex.Pattern patt = java.util.regex.Pattern.compile("->R:([0-9]{1,3})/(.*?)/");

    @Override
    public String process(String string) {
        Matcher match = patt.matcher(string);
        while (match.find()) {
            String saturation = match.group(1);
            String content = match.group(2);
            string = string.replace(match.group(), RGB.rainbow(content, Float.parseFloat(saturation)));
        }
        return string;
    }
}
