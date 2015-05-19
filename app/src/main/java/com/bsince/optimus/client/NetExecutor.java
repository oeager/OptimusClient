package com.bsince.optimus.client;

import android.util.Log;

import com.bsince.optimus.cache.HttpCache;
import com.bsince.optimus.cache.tool.ByteArrayPool;
import com.bsince.optimus.cache.tool.PoolingByteArrayOutputStream;
import com.bsince.optimus.client.req.HttpPatch;
import com.bsince.optimus.client.req.Method;
import com.bsince.optimus.client.respon.OptHttpResponse;
import com.bsince.optimus.client.respon.ResponseHandler;
import com.bsince.optimus.data.Constants;
import com.bsince.optimus.event.Event;
import com.bsince.optimus.exception.UnAuthorizedException;
import com.bsince.optimus.utils.HeaderUtils;
import com.bsince.optimus.utils.L;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Created by oeager on 2015/5/18.
 */
final class NetExecutor {

    private final ResponseHandler handler;

    private final AbstractHttpClient client;

    private final HttpContext context;

    private final HttpCache mCache;

    private final ByteArrayPool mPool;

    public NetExecutor(AbstractHttpClient client,HttpContext context, HttpCache cache, ByteArrayPool mPool,ResponseHandler handler){
        this.handler = handler;
        this.mPool = mPool;
        this.client = client;
        this.context = context;
        this.mCache = cache;
    }

    public <T> void execute(Event<T> mEvent){

        if (mEvent.isCache()) {
            if (isCanceled(mEvent,null)) {
                return;
            }
            HttpCache.Entry entry = mCache.get(mEvent.getCacheKey());
            if (entry != null) {
                mEvent.setCacheEntry(entry);
                if (!entry.isExpired()) {
                    OptHttpResponse<T> httpResponse;
                    httpResponse = mEvent.parse(entry.data,
                            entry.responseHeaders);
                    handler.responSucess(mEvent,
                            HttpStatus.SC_NOT_MODIFIED, httpResponse);
                    if (!entry.refreshNeeded()) {

                        return;
                    }
                    httpResponse.intermediate = true;

                }

            }
        }

        if (isCanceled(mEvent,null)) {
            return;
        }
        HttpUriRequest uriRequest = createHttpUriRequest(mEvent);

        if (mEvent.getDataSet().getHeaders() != null) {
            uriRequest.setHeaders(mEvent.getDataSet().getHeaders());
        }

        HeaderUtils.addCacheHeaders(uriRequest, mEvent.getCacheEntry());

        if (isCanceled(mEvent,uriRequest)) {
            return;
        }

        try {
            makeRequestWithRetries(mEvent,uriRequest);
        } catch (IOException e) {
            if (!isCanceled(mEvent,uriRequest)) {
                handler.responFail(mEvent, Constants.IO_EVENT_ERROR, e);
            }
        }

    }

    public boolean isCanceled(Event<?> mEvent,HttpUriRequest uriRequest) {
        boolean flag = mEvent.isCancel();
        if (flag) {
            if (uriRequest != null) {
                uriRequest.abort();
            }
        }
        return flag;
    };

    private void makeRequestWithRetries(Event<?> mEvent,HttpUriRequest uriRequest) throws IOException {
        boolean retry = true;
        IOException cause = null;
        HttpRequestRetryHandler retryHandler = client
                .getHttpRequestRetryHandler();
        int executionCount = 0;
        try {
            while (retry) {
                try {
                    makeRequest(mEvent,uriRequest);
                    return;
                } catch (UnknownHostException e) {
                    cause = new IOException("UnknownHostException exception: "
                            + e.getMessage());
                    retry = (executionCount > 0)
                            && retryHandler.retryRequest(cause,
                            ++executionCount, context);
                } catch (NullPointerException e) {
                    cause = new IOException("NPE in HttpClient: "
                            + e.getMessage());
                    retry = retryHandler.retryRequest(cause, ++executionCount,
                            context);
                } catch (IOException e) {
                    if (isCanceled(mEvent,uriRequest)) {
                        // Eating exception, as the request was cancelled
                        return;
                    }
                    cause = e;
                    retry = retryHandler.retryRequest(cause, ++executionCount,
                            context);
                }
                if (retry) {
                    L.d("retry " + executionCount + "times");
                }
            }
        } catch (Exception e) {
            Log.e("OptmusClient", "Unhandled exception origin cause", e);
            cause = new IOException("Unhandled exception: " + e.getMessage());
        }

        // cleaned up to throw IOException
        throw (cause);
    }
    private <T> void makeRequest(Event<T> mEvent,HttpUriRequest uriRequest) throws IOException, NullPointerException {
        if (isCanceled(mEvent,uriRequest)) {
            return;
        }

        if (uriRequest.getURI().getScheme() == null) {
            throw new MalformedURLException("No valid URI scheme was provided");
        }

        HttpResponse response = client.execute(uriRequest, context);

        if (isCanceled(mEvent,uriRequest)) {
            return;
        }
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        Map<String, String> responseHeaders = HeaderUtils
                .convertHeaders(response.getAllHeaders());
        byte[] data = null;
        if (statusCode == HttpStatus.SC_NOT_MODIFIED) {
            data = mEvent.getCacheEntry() == null ? null : mEvent
                    .getCacheEntry().data;
            if (mEvent.hasHadResponseDelivered()) {
                mEvent.markFinish("not-modified");
                return;
            }
        } else {
            data = entityToBytes(response.getEntity(),mEvent);

            if (statusCode < 200 || statusCode > 299) {
                if (statusCode == HttpStatus.SC_UNAUTHORIZED
                        || statusCode == HttpStatus.SC_FORBIDDEN) {
                    throw new UnAuthorizedException("statusCode==" + statusCode);
                }
                throw new IOException("statusCode==" + statusCode);
            }
        }

        if (isCanceled(mEvent,uriRequest)) {
            return;
        }
        OptHttpResponse<T> httpResponse = mEvent.parse(data, responseHeaders);

        if (mEvent.isCache() && httpResponse.cacheEntry != null) {
            mCache.put(mEvent.getCacheKey(), httpResponse.cacheEntry);
            L.d("network-cache-written");
        }
        if (isCanceled(mEvent,uriRequest)) {
            return;
        }
        mEvent.markReceiveResult();
        handler.responSucess(mEvent, statusCode, httpResponse);

    }

    private byte[] entityToBytes(HttpEntity entity,Event<?> mEvent)
            throws NullPointerException, IOException {
        PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(
                mPool, (int) entity.getContentLength());
        byte[] buffer = null;
        try {
            InputStream in = entity.getContent();
            if (in == null) {
                throw new NullPointerException("entity.getContent() is null...");
            }
            int total = (int)entity.getContentLength();
            buffer = mPool.getBuf(1024);
            int len;
            int count = 0;
            while ((len = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, len);
                count+=len;
                if(mEvent.loadingProcessor!=null){
                    handler.responProcessing(mEvent, count, total);
                }
            }
            return bytes.toByteArray();
        } finally {
            try {

                entity.consumeContent();
            } catch (IOException e) {
                //
                L.v("Error occured when calling consumingContent");
            }
            mPool.returnBuf(buffer);
            bytes.close();
        }
    }

    private  HttpUriRequest createHttpUriRequest(Event<?> event) {
        String url = event.getDataSet().getUrl();
        switch (event.getDataSet().getMethod()) {
            case Method.HEAD:
                return new HttpHead(url);
            case Method.GET:
            default:
                return new HttpGet(url);
            case Method.DELETE:
                return new HttpDelete(url);
            case Method.OPTIONS:
                return new HttpOptions(url);
            case Method.TRACE:
                return new HttpTrace(url);
            case Method.POST:
                HttpPost httpPost = new HttpPost(url);
                setUriRequestEntity(httpPost, event);
                return httpPost;
            case Method.PUT:
                HttpPut httpPut = new HttpPut(url);
                setUriRequestEntity(httpPut, event);
                return httpPut;
            case Method.PATCH:
                HttpPatch httpPatch = new HttpPatch(url);
                setUriRequestEntity(httpPatch, event);
                return httpPatch;

        }
    }
    private  void setUriRequestEntity(HttpEntityEnclosingRequestBase request,
                                     Event<?> event) {
        HttpEntity entity = event.getDataSet().getHttpEntity(event, handler);

        if (entity != null) {
            String contentType = event.getDataSet().getBodyContentType();
            if (contentType != null) {
                request.setHeader(Constants.HEADER_CONTENT_TYPE, contentType);
            }
            request.setEntity(entity);
        }
    }
}
