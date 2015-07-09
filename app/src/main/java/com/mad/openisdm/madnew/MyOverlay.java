package com.mad.openisdm.madnew;

import android.content.ClipData;
import android.graphics.drawable.Drawable;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import java.util.List;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.ResourceProxy.bitmap;
import org.osmdroid.api.IMapView;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedOverlay;


import java.util.List;

/**
 * Created by Paul on 2015/7/8.
 */
public class MyOverlay<Item extends OverlayItem> extends ItemizedIconOverlay<Item> {
    public MyOverlay(List<Item> pList, Drawable pDefaultMarker, ItemizedIconOverlay.OnItemGestureListener<Item> pOnItemGestureListener, ResourceProxy pResourceProxy){
        super(pList ,pDefaultMarker, pOnItemGestureListener, pResourceProxy);
    }

    public MyOverlay(List<Item> pList, ItemizedIconOverlay.OnItemGestureListener<Item> pOnItemGestureListener, ResourceProxy pResourceProxy) {
        this(pList, pResourceProxy.getDrawable(bitmap.marker_default), pOnItemGestureListener, pResourceProxy);
    }

    /*public boolean addItem(Item item) {
        boolean result = this.mItemList.add(item);
        this.populate();
        return result;
    }

    public void addItem(int location, Item item) {
        this.mItemList.add(location, item);
        this.populate();
    }
*/
    public boolean addItems(List<Item> items) {
        boolean result = this.mItemList.addAll(items);
        this.populate();
        return result;
    }

    public void removeAllItems() {
        this.removeAllItems(true);
    }

    public void removeAllItems(boolean withPopulate) {
        this.mItemList.clear();
        if(withPopulate) {
            this.populate();
        }

    }

    public boolean removeItem(Item item) {
        boolean result = this.mItemList.remove(item);
        this.populate();
        return result;
    }

    public Item removeItem(int position) {
        OverlayItem result = (OverlayItem)this.mItemList.remove(position);
        this.populate();
        return (Item)result;
    }

}
