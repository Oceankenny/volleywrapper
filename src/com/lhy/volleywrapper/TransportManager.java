package com.lhy.volleywrapper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lhy.volleywrapper.upload.MultiPartStack;
import com.lhy.volleywrapper.upload.MultiPartStringRequest;

/**
 * 
 * @author liuhy0206@gmail.com
 * 
 */
public class TransportManager {
    private static final String TAG = TransportManager.class.getSimpleName();

    private static TransportManager instance = null;
    private static Object mLock = new Object();

    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;
    private static RequestQueue mFileUploadRequestQueue;

    private TransportManager() {
    }

    public static TransportManager Instance() {
        if (null == instance) {
            synchronized (mLock) {
                if (null == instance) {
                    instance = new TransportManager();
                }
            }
        }

        return instance;
    }

    public void init(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
        mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache());
        mFileUploadRequestQueue = Volley.newRequestQueue(context,
                new MultiPartStack());
    }

    /**
     * Send a request
     * 
     * @param request
     * @param tag
     */
    public void sendRequest(Request<?> request, Object tag) {
        if (tag != null) {
            request.setTag(tag);
        }

        mRequestQueue.add(request);
    }

    /**
     * Cancel all requests with tag.
     * 
     * @param tag
     */
    public void cancelAllRequest(Object tag) {
        mRequestQueue.cancelAll(tag);
    }

    /**
     * Send a post request without params, and response with string
     * 
     * @param url
     * @param response
     */
    public void sendPostSimpleRequest(String url,
            final IResponse<String> response) {
        sendSimpleRequest(Method.POST, url, response);
    }

    /**
     * Send a post request with params, and response with string
     * 
     * @param url
     * @param params
     * @param response
     */
    public void sendPostRequestWithParams(String url,
            final Map<String, String> params, final IResponse<String> response) {
        sendSimpleRequestWithParams(Method.POST, url, params, response);
    }

    /**
     * Send a post request with json, and response with json
     * 
     * @param url
     * @param json
     * @param response
     */
    public void sendPostRequestWithJson(String url, final String json,
            final IResponse<JSONObject> response) {
        sendRequestWithJson(Method.POST, url, json, response);
    }

    /**
     * Send a post request with json, its url should append with params, and
     * response with json
     * 
     * @param url
     * @param params
     *            - params should be appended to url
     * @param json
     * @param response
     */
    public void sendPostRequestWithJson(String url,
            final Map<String, String> params, final String json,
            final IResponse<JSONObject> response) {
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                url += "?";
                url += entry.getKey() + "=" + entry.getValue();
            }
        }
        sendRequestWithJson(Method.POST, url, json, response);
    }

    /**
     * Send a put request with json, its url should append with params, and
     * response with json
     * 
     * @param url
     * @param params
     *            - params should be appended to url
     * @param json
     * @param response
     */
    public void sendPutRequestWithJson(String url,
            final Map<String, String> params, final String json,
            final IResponse<JSONObject> response) {
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                url += "?";
                url += entry.getKey() + "=" + entry.getValue();
            }
        }
        sendRequestWithJson(Method.PUT, url, json, response);
    }

    /**
     * Send a post request with json ARRAY, its url should append with params,
     * and response with json
     * 
     * @param url
     * @param params
     *            - params should be appended to url
     * @param json
     * @param response
     */
    public void sendPostRequestWithJsonArray(String url,
            final Map<String, String> params, final String json,
            final IResponse<JSONObject> response) {
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                url += "?";
                url += entry.getKey() + "=" + entry.getValue();
            }
        }

        SendRequestJsonArray(Method.POST, url, json, response);
    }

    /**
     * Send a PUT request with json ARRAY, its url should append with params,
     * and response with json
     * 
     * @param url
     * @param params
     *            - params should be appended to url
     * @param json
     * @param response
     */
    public void sendPutRequestWithJsonArray(String url,
            final Map<String, String> params, final String json,
            final IResponse<JSONObject> response) {
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                url += "?";
                url += entry.getKey() + "=" + entry.getValue();
            }
        }

        SendRequestJsonArray(Method.PUT, url, json, response);
    }

    /**
     * Send a post request with params, and response with json
     * 
     * @param url
     * @param params
     * @param response
     */
    public void sendPostRequestJson(String url,
            final Map<String, String> params,
            final IResponse<JSONObject> response) {
        sendRequestJson(Method.POST, url, params, response);
    }

    /**
     * Send a get request with params, and response with json
     * 
     * @param url
     * @param params
     * @param response
     */
    public void sendGetRequestJson(String url,
            final Map<String, String> params,
            final IResponse<JSONObject> response) {
        if (params != null && params.size() > 0) {
            int i = 0;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (i == 0) url += "?";
                url += entry.getKey() + "=" + entry.getValue();
                if (i < params.size() - 1) url += "&";

                ++i;
            }
        }
        sendRequestJson(Method.GET, url, null, response);
    }

    public void sendSimpleRequest(int method, String url,
            final IResponse<String> response) {
        StringRequest request = new StringRequest(method, url,
                getResponseLisener(response), getErrorListener(response));
        request.setShouldCache(false);
        mRequestQueue.add(request);
    }

    public void sendSimpleRequestWithParams(int method, String url,
            final Map<String, String> params, final IResponse<String> response) {
        StringRequest request = new StringRequest(method, url,
                getResponseLisener(response), getErrorListener(response)) {

            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        request.setShouldCache(false);
        mRequestQueue.add(request);
    }

    public void sendRequestWithJson(int method, String url, final String json,
            final IResponse<JSONObject> response) {

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(method, url,
                jsonObject, getResponseLisener(response),
                getErrorListener(response));
        request.setShouldCache(false);
        mRequestQueue.add(request);
    }

    public void SendRequestJsonArray(int method, String url, final String json,
            final IResponse<JSONObject> response) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectWithJasonArrayRequest request = new JsonObjectWithJasonArrayRequest(
                method, url, jsonArray, getResponseLisener(response),
                getErrorListener(response));
        request.setShouldCache(false);
        mRequestQueue.add(request);
    }

    public void sendRequestJson(int method, String url,
            final Map<String, String> params,
            final IResponse<JSONObject> response) {
        CustomizeStringRequest request = new CustomizeStringRequest(method,
                url, params, getResponseLisener(response),
                getErrorListener(response));
        request.setShouldCache(false);
        mRequestQueue.add(request);
    }

    public void upLoadFile(String url, final Map<String, String> filePaths,
            final Map<String, String> params,
            final IResponse<JSONObject> response) {
        MultiPartStringRequest request = new MultiPartStringRequest(
                Method.POST, url, getResponseLisener(response),
                getErrorListener(response)) {

            @Override
            public Map<String, File> getFileUploads() {
                Map<String, File> fileUploads = new HashMap<String, File>();
                for (Map.Entry<String, String> entry : filePaths.entrySet()) {
                    fileUploads.put(entry.getKey(), new File(entry.getValue()));
                }

                return fileUploads;
            }

            @Override
            public Map<String, String> getStringUploads() {
                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        request.setShouldCache(false);
        mFileUploadRequestQueue.add(request);
    }

    private static <T> Response.ErrorListener getErrorListener(
            IResponse<T> response) {
        return new ResponseErrorListener<T>(response);
    }

    private static <T> Response.Listener<T> getResponseLisener(
            IResponse<T> response) {
        return new ResponseListener<T>(response);
    }

    private static class ResponseListener<T> implements Response.Listener<T> {
        private IResponse<T> response;

        ResponseListener(IResponse<T> res) {
            response = res;
        }

        @Override
        public void onResponse(T res) {
            if (response != null) {
                response.onRespose(res);
                response = null;
            }
        }
        
    }

    private static class ResponseErrorListener<T> implements Response.ErrorListener {
        private IResponse<T> response;

        ResponseErrorListener(IResponse<T> res) {
            response = res;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if (response != null) {
                response.onError(error.getMessage());
                response = null;
            }
        }
        
    }

    /**
     * TODO: extends to support more kinds of image view.
     * 
     * @param imageView
     *            - view to show image
     * @param url
     * @param defaultImageId
     *            - default drawable resource id
     * @return
     */
    public void loadImage(NetworkImageView imageView, String url,
            int defaultImageId) {
        if (imageView == null) {
            Log.w(TAG, "image view is null.");
            return;
        }

        imageView.setDefaultImageResId(defaultImageId);
        imageView.setImageUrl(url, mImageLoader);

    }

    public void getImage(ImageView imageView, String url, int defaultImageId,
            int maxWidth, int maxHeight) {
        mImageLoader.get(url, ImageLoader.getImageListener(imageView,
                defaultImageId, defaultImageId), maxWidth, maxHeight);
    }
}
