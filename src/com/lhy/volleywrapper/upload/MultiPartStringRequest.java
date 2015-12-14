/**
 * Copyright 2013 Mani Selvaraj
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lhy.volleywrapper.upload;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

/**
 * MultipartRequest - To handle the large file uploads. Extended from
 * JSONRequest. You might want to change to StringRequest based on your response
 * type.
 * 
 * @author Mani Selvaraj
 * @author haiyang.liu@xietong110.com
 */
public class MultiPartStringRequest extends Request<JSONObject> implements
        MultiPartRequest {

    private final Listener<JSONObject> mListener;
    /* To hold the parameter name and the File to upload */
    private Map<String, File> fileUploads = new HashMap<String, File>();

    /* To hold the parameter name and the string content to upload */
    private Map<String, String> stringUploads = new HashMap<String, String>();

    private Map<String, String> headers = new HashMap<String, String>();

    /**
     * Creates a new request with the given method.
     * 
     * @param method
     *            the request {@link Method} to use
     * @param url
     *            URL to fetch the string at
     * @param listener
     *            Listener to receive the String response
     * @param errorListener
     *            Error listener, or null to ignore errors
     */
    public MultiPartStringRequest(int method, String url,
            Listener<JSONObject> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    public void addFileUpload(String param, File file) {
        fileUploads.put(param, file);
    }

    public void addStringUpload(String param, String content) {
        stringUploads.put(param, content);
    }

    public Map<String, File> getFileUploads() {
        return fileUploads;
    }

    public Map<String, String> getStringUploads() {
        return stringUploads;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers;
    }

    public void setHeader(String title, String content) {
        headers.put(title, content);
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }

    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));

            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    /**
     * should let it be null here.
     */
    public String getBodyContentType() {
        return null;
    }
}