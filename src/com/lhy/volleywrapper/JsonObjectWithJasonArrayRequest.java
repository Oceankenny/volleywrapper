package com.lhy.volleywrapper;

import java.io.UnsupportedEncodingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

/**
 * A request for retrieving a {@link JSONObject} response body at a given URL,
 * allowing for an optional {@link JSONArray} to be passed in as part of the
 * request body.
 * 
 * @author liuhy0206@gmail.com
 * 
 */
public class JsonObjectWithJasonArrayRequest extends JsonRequest<JSONObject> {

    public JsonObjectWithJasonArrayRequest(int method, String url,
            JSONArray jsonRequest, Listener<JSONObject> listener,
            ErrorListener errorListener) {
        super(method, url, jsonRequest.toString(), listener, errorListener);
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
}
