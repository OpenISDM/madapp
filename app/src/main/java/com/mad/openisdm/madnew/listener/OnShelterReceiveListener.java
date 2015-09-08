package com.mad.openisdm.madnew.listener;

import java.util.ArrayList;
import com.mad.openisdm.madnew.model.Shelter;
/**
 * Created by JanSu on 9/8/15.
 */
public interface OnShelterReceiveListener {
    /**A callback method when Shelters are received(after fetching from shelter source),
     * the list of Shelter received is passed as arguments*/
    public abstract void onShelterReceive(ArrayList<Shelter> shelters);
}
