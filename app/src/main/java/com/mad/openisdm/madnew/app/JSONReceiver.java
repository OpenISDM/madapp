package com.mad.openisdm.madnew.app;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Paul on 2015/7/29.
 */
public interface JSONReceiver {
    public void handleJSON(JSONObject jsonObject) throws JSONException;
}
