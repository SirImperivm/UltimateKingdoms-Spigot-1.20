package me.sirimperivm.spigot.util.other;

import java.util.List;

@SuppressWarnings("all")
public class Strings {

    public static String formatNumber(double number, int format_size, List<String> associations) {
        String toReturn = String.valueOf(number);
        String value_size = "";
        StringBuilder sb = new StringBuilder(format_size);

        for (String association : associations) {
            String[] splitter = association.split("-");
            double ass_value = Double.parseDouble(splitter[0]);
            if (number > ass_value) {
                toReturn = String.valueOf(number/ass_value);
                value_size = splitter[1];
            }
        }

        if (toReturn.length() >= format_size) {
            for (int i=0; i<format_size; i++) {
                if (toReturn.charAt(i) == '.') {
                    break;
                }
                sb.append(toReturn.charAt(i));
            }

            toReturn = sb.toString();
        }

        if (toReturn.contains(".0")) {
            toReturn = toReturn.replace(".0", "");
        }

        toReturn = toReturn+value_size;
        return toReturn;
    }

    public static String formatNumber(int number, int format_size, List<String> associations) {
        String toReturn = String.valueOf(number);
        String value_size = "";
        StringBuilder sb = new StringBuilder(format_size);

        for (String association : associations) {
            String[] splitter = association.split("-");
            int ass_value = Integer.parseInt(splitter[0]);
            if (number > ass_value) {
                toReturn = String.valueOf(number/ass_value);
                value_size = splitter[1];
            }
        }

        if (toReturn.length() >= format_size) {
            for (int i=0; i<format_size; i++) {
                if (toReturn.charAt(i) == '.') {
                    break;
                }
                sb.append(toReturn.charAt(i));
            }

            toReturn = sb.toString();
        }

        if (toReturn.contains(".0")) {
            toReturn = toReturn.replace(".0", "");
        }

        toReturn = toReturn+value_size;
        return toReturn;
    }
}
