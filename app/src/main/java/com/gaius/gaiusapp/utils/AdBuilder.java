package com.gaius.gaiusapp.utils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class AdBuilder {
    private StringBuilder sb;
    private int objectCount;

    public AdBuilder() {
        this.sb = new StringBuilder();
        this.objectCount = 0;
    }
    //
//    public void addImage(String url) {
//        sb.append("{\"type\":\"img\",\"url\":\"ads://" + url +"\",\n");
//        objectCount++;
//    }
//
//    public void addVideo(String url) {
//        sb.append("{\"type\":\"video\",\"url\":\"ads://" + url +"\",\n");
//        objectCount++;
//    }
    public void addText(String text) {
        sb.append("{\"type\":\"txt\",\"txt\":\"" + text +"\",\"font\":24.0,\"font-type\":\"Roboto Condensed\",\"color\":\"#373f44\"}");
        objectCount++;
    }

    public String makeFile(String dir) {

        File file = new File(Environment.getExternalStorageDirectory().getPath(), dir);
        if (!file.exists()) {
            file.mkdirs();
        }

        String filename = (file.getAbsolutePath() + "/" +new File("ad.txt"));
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