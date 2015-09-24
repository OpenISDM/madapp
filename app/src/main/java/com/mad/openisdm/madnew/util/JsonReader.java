package com.mad.openisdm.madnew.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * A class that provides static method for reading JSON files (in  plain text format) from a URL
 */
public class JsonReader {
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            // If target device's memory is smaller, and this function will throw out a message
            // with "OutOfMemory"
            sb.append((char) cp);
        }
        return sb.toString();
    }

    /**Read a (JSON) file from the given URL, and returns the file in plain text*/
    public static String readJsonFromUrl(String url) throws IOException{
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

            return readAll(rd);
        }finally {
            is.close();
        }
    }
}
