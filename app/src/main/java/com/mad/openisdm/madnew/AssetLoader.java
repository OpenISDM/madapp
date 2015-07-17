package com.mad.openisdm.madnew;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Paul on 2015/7/8.
 */
public class AssetLoader {
    private Context context;
    public AssetLoader(Context context){
        this.context = context;
    }

    public String loadJSONFromAsset(String fileName) {
        String json = null;
        try {

            InputStream is = context.getAssets().open(fileName);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }
}


