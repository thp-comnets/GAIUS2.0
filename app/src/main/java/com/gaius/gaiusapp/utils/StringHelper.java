package com.gaius.gaiusapp.utils;

public class StringHelper {

    public static String getFirstNWords(String s, int n) {
        if (s == null) return null;
        String [] sArr = s.split("\\s+");
        if (n >= sArr.length)
            return s;

        String firstN = "";

        for (int i=0; i<n-1; i++) {
            firstN += sArr[i] + " ";
        }
        firstN += sArr[n-1];
        return firstN + "...";
    }
}
