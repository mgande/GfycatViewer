package com.magnde.gfycatviewer;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mukga on 12/8/2016.
 */

class Cache {

    private static HashMap<String, Bitmap> mCache = new HashMap<>();
    private static ArrayList<String> mKeys = new ArrayList<>();

    static void saveImage(String url, Bitmap image) {
        mCache.put(url, image);
        mKeys.add(url);

        if (mCache.size() > 100) {
            mCache.remove(mKeys.remove(0));
        }
    }

    static Bitmap getImage(String url) {
        return mCache.get(url);
    }
}
