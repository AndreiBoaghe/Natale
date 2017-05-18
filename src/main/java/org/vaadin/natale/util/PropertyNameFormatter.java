package org.vaadin.natale.util;


public class PropertyNameFormatter {

    public String getConvertedPropertyName(String propertyName) {
        if (!propertyName.contains(".")) {
            propertyName = propertyNameToCaseSensitive(propertyName);
            String firstChar = propertyName.substring(0, 1).toUpperCase();
            return firstChar + propertyName.substring(1);
        }

        StringBuilder sb = new StringBuilder(toUpperFirstCharacter(propertyName.substring(0, propertyName.indexOf("."))));
        char[] arr = propertyName.substring(propertyName.indexOf(".")).toCharArray();

        int dotCounter = countOccurrences(propertyName, ".");
        for (int i = 0; i < arr.length; ++i) {
            if (arr[i] == '.') {
                sb.append("(");
                sb.append(String.valueOf(arr[i + 1]).toUpperCase());
                i++;
            } else {
                sb.append(arr[i]);
            }
        }
        for (int i = 0; i < dotCounter; ++i)
            if (i == dotCounter - 2) {
            }
        sb.append(")");

        return propertyNameToCaseSensitive(sb.toString());
    }

    public static String toUpperFirstCharacter(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String propertyNameToCaseSensitive(String propertyName) {
        return propertyName.replaceAll("(\\B[A-Z])", " $1");
    }

    private int countOccurrences(String main, String sub) {
        return (main.length() - main.replace(sub, "").length()) / sub.length();
    }
}
