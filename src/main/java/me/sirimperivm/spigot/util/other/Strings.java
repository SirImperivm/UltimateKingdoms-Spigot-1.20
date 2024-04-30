package me.sirimperivm.spigot.util.other;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.util.ConfUtil;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("all")
public class Strings {

    private Main plugin;
    private ConfUtil config;
    public Strings(Main plugin) {
        this.plugin = plugin;
        config = plugin.getCM();
    }

    public String formatNumber(Object target) {
        String toReturn = "";
        int formatSize = config.getSettings().getInt("other.strings.number-formatter.format-size");
        List<String> associations = config.getSettings().getStringList("other.strings.number-formatter.associations");
        HashMap<String, String> associatedValues = new HashMap<>();
        for (String association : associations) {
            String[] slice = association.split("-");
            String numeric = slice[0];
            String alfabetical = slice[1];
            associatedValues.put(numeric, alfabetical);
        }

        if (target instanceof Integer) {
            int number = (Integer) target;
            toReturn = String.valueOf(number);
            HashMap<Integer, String> newAssociatedValues = new HashMap<>();
            for (String key : associatedValues.keySet()) {
                String value = associatedValues.get(key);
                key = key.replace(".0", "");
                newAssociatedValues.put(Integer.parseInt(key), value);
            }
            String valueCurrency = "";
            StringBuilder sb = new StringBuilder(formatSize);

            for (Integer key : newAssociatedValues.keySet()) {
                String alfabetical = newAssociatedValues.get(key);
                if (number >= key) {
                    toReturn = String.valueOf(number/key);
                    valueCurrency = alfabetical;
                }
            }

            if (toReturn.length() >= formatSize) {
                for (int i=0; i<formatSize; i++) {
                    if (toReturn.charAt(i) == '.') break;
                    sb.append(toReturn.charAt(i));
                }
            }

            if (toReturn.contains(".0")) {
                toReturn = toReturn.replace(".0", "");
            }

            toReturn = toReturn + valueCurrency;
        } else if (target instanceof Double) {
            double number = (Double) target;
            toReturn = String.valueOf(number);
            HashMap<Double, String> newAssociatedValues = new HashMap<>();
            for (String key : associatedValues.keySet()) {
                String value = associatedValues.get(key);
                newAssociatedValues.put(Double.parseDouble(key), value);
            }
            String valueCurrency = "";
            StringBuilder sb = new StringBuilder(formatSize);

            for (Double key : newAssociatedValues.keySet()) {
                String alfabetical = newAssociatedValues.get(key);
                if (number >= key) {
                    toReturn = String.valueOf(number/key);
                    valueCurrency = alfabetical;
                }
            }

            if (toReturn.length() >= formatSize) {
                for (int i=0; i<formatSize; i++) {
                    sb.append(toReturn.charAt(i));
                }
            }

            if (toReturn.contains(".0")) {
                toReturn = toReturn.replace(".0", "");
            }

            toReturn = toReturn + valueCurrency;
        }
        return toReturn;
    }

    public String capitalize(String target) {
        return target.substring(0, 1).toUpperCase()+target.substring(1);
    }
}
