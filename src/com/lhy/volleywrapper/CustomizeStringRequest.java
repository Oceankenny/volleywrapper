package com.lhy.volleywrapper;

import java.io.UnsupportedEncodingException;
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
 * 
 * It is a request that send with params and suppose to receive response with
 * json.
 * 
 * @author liuhy0206@gmail.com
 * 
 */

public class CustomizeStringRequest extends Request<JSONObject> {
    private static final String TAG = CustomizeStringRequest.class
            .getSimpleName();

    private Listener<JSONObject> mListener;
    private Map<String, String> mParams;

    public CustomizeStringRequest(int method, String url, ErrorListener listener) {
        super(method, url, listener);
    }

    public CustomizeStringRequest(int method, String url,
            Map<String, String> params, Listener<JSONObject> responseListener,
            ErrorListener listener) {
        super(method, url, listener);

        mListener = responseListener;
        mParams = params;
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }

    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
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
