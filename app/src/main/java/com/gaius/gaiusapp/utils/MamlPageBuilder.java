package com.gaius.gaiusapp.utils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MamlPageBuilder {
    private StringBuilder sb;
    private int objectCount;

    public MamlPageBuilder() {
        this.sb = new StringBuilder();
        this.objectCount = 0;
    }

    public void addBackground(String color) {
        sb.append("{\"type\":\"bg\",\"color\":\""+color+"\"}\n");
    }

    public void addBackground(String color, String bg) {
        sb.append("{\"type\":\"bg\",\"bgFile\":\"" + bg + "\",\"color\":\""+color+"\"}\n");
    }

    public void addImage(String url, int x, int y, int width, int height) {
        if (width == 0 || height == 0)
            return;
        sb.append("{\"type\":\"img\",\"url\":\"" + url +"\",\"x\":" + x + ",\"y\":" +y+ ",\"w\":" + width +",\"h\":" +height + "}\n");
        objectCount++;
    }

    public void addVideo(String url, int x, int y, int width, int height) {
        if (width == 0 || height == 0)
            return;
        sb.append("{\"type\":\"video\",\"url\":\"" + url +"\",\"x\":" + x + ",\"y\":" +y+ ",\"w\":" + width +",\"h\":" +height + "}\n");
        objectCount++;
    }

    public void addText(String text, String font, float fontSize, int x, int y, int width, int height, String color) {
        if (width == 0 || height == 0 || text == null)
            return;
        //text = text.replaceAll("(\\r|\\n|\\r\\n)+", "\\\\n");
        text = text.replaceAll("\n","<br>").replaceAll("\"","<doubleQuote>");
        sb.append("{\"type\":\"txt\",\"txt\":\"" + text +"\",\"x\":" + x + ",\"y\":" +y+ ",\"w\":" + width +",\"h\":" +height + ",\"font\":"+fontSize+",\"font-type\":\""+font+"\",\"color\":\""+color+"\"}\n");
        objectCount++;
    }

    public void addRect(int x, int y, int width, int height, int color) {
        if (width == 0 || height == 0)
            return;
        sb.append("{\"type\":\"rect\",\"x\":" + x + ",\"y\":" +y+ ",\"w\":" + width +",\"h\":" +height + ",\"color\":\"" +String.format("#%08X", (0xFFFFFFFF & color))+ "\",\"border\":\"#FF0000\"}\n");
        objectCount++;
    }

    public void addAd(int x, int y, int width, int height) {
        if (width == 0 || height == 0)
            return;
        sb.insert(0,"\"No ads\"\n");
        sb.append("{\"type\":\"ad\",\"x\":" + x + ",\"y\":" +y+ ",\"w\":" + width +",\"h\":" +height + "}\n");
        objectCount++;
    }
    public String makeFile(String dir) {

        File file = new File(Environment.getExternalStorageDirectory().getPath(), dir);
        if (!file.exists()) {
            file.mkdirs();
        }

        String filename = (file.getAbsolutePath() + "/" +new File("index.maml"));
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(filename));
            writer.write(sb.toString());
            if (writer != null)
                writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filename;
    }
    public int getObjectCount() {
        return objectCount;
    }

    public String getPageAsString() {
        return sb.toString();
    }
}
