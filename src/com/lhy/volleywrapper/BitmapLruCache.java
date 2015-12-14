package com.lhy.volleywrapper;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

/**
 * 
 * @author liuhy0206@gmail.com
 * 
 */
public class BitmapLruCache implements ImageLoader.ImageCache {
    private static final int CACHE_SIZE = 10 * 1024 * 1024; // 10M

    private LruCache<String, Bitmap> mCache;

    public BitmapLruCache() {
        mCache = new LruCache<String, Bitmap>(CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
    }

    @Override
    public Bitmap getBitmap(String url) {
        return mCache.get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        mCache.put(url, bitmap);
    }

}
