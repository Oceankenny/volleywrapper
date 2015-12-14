package com.lhy.volleywrapper.upload;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.HurlStack;

/**
 * @author Mani Selvaraj
 * @author liuhy0206@gmail.com
 * 
 */

public class MultiPartStack extends HurlStack {
    private static final String TAG = MultiPartStack.class.getSimpleName();
    private final static String HEADER_CONTENT_TYPE = "Content-Type";

    private static void addHeaders(HttpUriRequest httpRequest,
            Map<String, String> headers) {
        for (String key : headers.keySet()) {
            httpRequest.setHeader(key, headers.get(key));
        }
    }

    /**
     * If Request is MultiPartRequest type, then set MultipartEntity in the
     * httpRequest object.
     * 
     * @param httpRequest
     * @param request
     * @throws AuthFailureError
     */
    private static void setMultiPartBody(
            HttpEntityEnclosingRequestBase httpRequest, Request<?> request)
            throws AuthFailureError {

        // Return if Request is not MultiPartRequest
        if (!(request instanceof MultiPartRequest)) {
            return;
        }

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.setCharset(Charset.forName(HTTP.UTF_8));

        // Iterate the fileUploads
        Map<String, File> fileUpload = ((MultiPartRequest) request)
                .getFileUploads();
        for (Map.Entry<String, File> entry : fileUpload.entrySet()) {
            File file = (File) entry.getValue();
            String name = (String) entry.getKey();

            builder.addBinaryBody("file", file,
                    ContentType.create("application/octet-stream"), name);
        }

        // Iterate the stringUploads
        Map<String, String> stringUpload = ((MultiPartRequest) request)
                .getStringUploads();
        for (Map.Entry<String, String> entry : stringUpload.entrySet()) {
            try {
                builder.addPart(((String) entry.getKey()), new StringBody(
                        (String) entry.getValue()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        httpRequest.setEntity(builder.build());
    }

    static HttpUriRequest createMultiPartRequest(Request<?> request,
            Map<String, String> additionalHeaders) throws AuthFailureError {
        switch (request.getMethod()) {
        case Method.DEPRECATED_GET_OR_POST: {
            // This is the deprecated way that needs to be handled for backwards
            // compatibility.
            // If the request's post body is null, then the assumption is that
            // the request is
            // GET. Otherwise, it is assumed that the request is a POST.
            byte[] postBody = request.getBody();
            if (postBody != null) {
                HttpPost postRequest = new HttpPost(request.getUrl());
                if (request.getBodyContentType() != null)
                    postRequest.addHeader(HEADER_CONTENT_TYPE,
                            request.getBodyContentType());
                HttpEntity entity;
                entity = new ByteArrayEntity(postBody);
                postRequest.setEntity(entity);
                return postRequest;
            } else {
                return new HttpGet(request.getUrl());
            }
        }
        case Method.GET:
            return new HttpGet(request.getUrl());
        case Method.DELETE:
            return new HttpDelete(request.getUrl());
        case Method.POST: {
            HttpPost postRequest = new HttpPost(request.getUrl());
            if (request.getBodyContentType() != null) {
                postRequest.addHeader(HEADER_CONTENT_TYPE,
                        request.getBodyContentType());
            }
            setMultiPartBody(postRequest, request);
            return postRequest;
        }
        case Method.PUT: {
            HttpPut putRequest = new HttpPut(request.getUrl());
            if (request.getBodyContentType() != null)
                putRequest.addHeader(HEADER_CONTENT_TYPE,
                        request.getBodyContentType());
            setMultiPartBody(putRequest, request);
            return putRequest;
        }
        // Added in source code of Volley libray.
        // case Method.PATCH: {
        // HttpPatch patchRequest = new HttpPatch(request.getUrl());
        // if(request.getBodyContentType() != null)
        // patchRequest.addHeader(HEADER_CONTENT_TYPE,
        // request.getBodyContentType());
        // return patchRequest;
        // }
        default:
            throw new IllegalStateException("Unknown request method.");
        }
    }

    public HttpResponse performMultiPartRequest(Request<?> request,
            Map<String, String> additionalHeaders) throws IOException,
            AuthFailureError {
        HttpUriRequest httpRequest = createMultiPartRequest(request,
                additionalHeaders);
        addHeaders(httpRequest, additionalHeaders);
        addHeaders(httpRequest, request.getHeaders());
        HttpParams httpParams = httpRequest.getParams();
        int timeoutMs = request.getTimeoutMs();

        if (timeoutMs != -1) {
            HttpConnectionParams.setSoTimeout(httpParams, timeoutMs);
        }

        SchemeRegistry registry = new SchemeRegistry();
        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
        socketFactory
                .setHostnameVerifier((X509HostnameVerifier) SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        /* Make a thread safe connection manager for the client */
        HttpClient httpClient = new DefaultHttpClient(httpParams);
        httpClient.getConnectionManager().getSchemeRegistry()
                .register(new Scheme("https", socketFactory, 443));

        return httpClient.execute(httpRequest);
    }

    @Override
    public HttpResponse performRequest(Request<?> request,
            Map<String, String> additionalHeaders) throws IOException,
            AuthFailureError {

        if (!(request instanceof MultiPartRequest)) {
            return super.performRequest(request, additionalHeaders);
        } else {
            return performMultiPartRequest(request, additionalHeaders);
        }
    }
}
