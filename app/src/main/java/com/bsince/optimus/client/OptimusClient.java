package com.bsince.optimus.client;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.bsince.optimus.cache.DiskHttpCache;
import com.bsince.optimus.cache.HttpCache;
import com.bsince.optimus.cache.tool.ByteArrayPool;
import com.bsince.optimus.client.interceptor.GzipBothClientHeadersRequestInterceptor;
import com.bsince.optimus.client.interceptor.GzipResponseInterceptor;
import com.bsince.optimus.client.interceptor.PreemtiveAuthRequestInterceptor;
import com.bsince.optimus.client.respon.ResponseHandler;
import com.bsince.optimus.custom.imp.RetryHandler;
import com.bsince.optimus.data.Constants;
import com.bsince.optimus.event.Event;
import com.bsince.optimus.utils.UrlUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

public class OptimusClient {

    public static final String LOG_TAG = "OptimusClient";

    private volatile static OptimusClient instance = null;

    private final NetExecutor mExecutor;

    private final HttpContext httpContext;

    private final DefaultHttpClient httpClient;

    private final ResponseHandler responseHandler;

    private final Map<Object, List<Event<?>>> eventMap;

    private final ByteArrayPool mPool;

    private final ExecutorService majorExecutor;

    private final ExecutorService assistExecutor;

    private final HttpCache mCache;

    private final String endPoint;

    private final boolean isUrlEncodeEnable;

    private final boolean cache;

    private final AtomicInteger mEventNo = new AtomicInteger();

    public static OptimusClient get(Context mContext) {
        if (instance == null) {
            synchronized (OptimusClient.class) {
                if (instance == null) {
                    Context applicationContext = mContext.getApplicationContext();
                    List<MetaAdapter> adapters = MetaAdapter.ManifestParser.parse(applicationContext);
                    OptimusConfigBuilder builder = new OptimusConfigBuilder();
                    for (MetaAdapter adapter : adapters){
                        adapter.applyOptions(applicationContext,builder);
                    }
                    builder.checkConfiguration();
                    instance = new OptimusClient(builder);
                }
            }
        }
        return instance;
    }

    public void submitRunnable(Runnable runnable){
        assistExecutor.submit(runnable);
    }

    @Deprecated
    public static OptimusClient get(){
        if(instance==null){
            synchronized (OptimusClient.class){
                if(instance==null){
                    OptimusConfigBuilder b = new OptimusConfigBuilder();
                    b.checkConfiguration();
                    instance = new OptimusClient(b);
                }
            }
        }
        return instance;
    }

    private OptimusClient(OptimusConfigBuilder builder) {

        majorExecutor = builder.mainExecutor;
        assistExecutor = builder.assisExecutor;
        endPoint = builder.endPoint;
        isUrlEncodeEnable = builder.isUrlEncodeEnable;
//        mCache = builder.;
        cache = builder.cache;

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory
                .getSocketFactory(), builder.httpPort));
        schemeRegistry.register(new Scheme("https", builder.sslSocketFactory,
                builder.httpsPort));
        final BasicHttpParams httpParams = new BasicHttpParams();
        ConnManagerParams.setTimeout(httpParams, builder.connectionTimeOut);
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams,
                new ConnPerRouteBean(builder.maxConnections));
        ConnManagerParams.setMaxTotalConnections(httpParams,
                Constants.DEFAULT_MAX_CONNECTIONS);
        HttpConnectionParams.setSoTimeout(httpParams, builder.responseTimeOut);
        HttpConnectionParams.setConnectionTimeout(httpParams,
                builder.connectionTimeOut);
        HttpConnectionParams.setTcpNoDelay(httpParams, true);
        HttpConnectionParams.setSocketBufferSize(httpParams,
                builder.socketBufferSize);
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(httpParams, builder.userAgent);
        httpParams.setBooleanParameter(ClientPNames.REJECT_RELATIVE_REDIRECT,
                builder.enableRedirects);
        httpParams.setBooleanParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS,
                builder.enableRedirects);
        ClientConnectionManager cm = new ThreadSafeClientConnManager(
                httpParams, schemeRegistry);
        httpClient = new DefaultHttpClient(cm, httpParams);
        httpContext = new SyncBasicHttpContext(new BasicHttpContext());
        responseHandler = new ResponseHandler();
        eventMap = new HashMap<>();
        mCache = new DiskHttpCache(builder.httpCacheDirectory,
                builder.maxHttpCacheSize);
        builder.assisExecutor.execute(new Runnable() {

            @Override
            public void run() {
                mCache.init();
            }
        });
        mPool = new ByteArrayPool(Constants.DEFAULT_POOL_SIZE);
        httpClient
                .addRequestInterceptor(new GzipBothClientHeadersRequestInterceptor(
                        builder.allRequestHeader));
        httpClient.addResponseInterceptor(new GzipResponseInterceptor());

        httpClient.setHttpRequestRetryHandler(new RetryHandler(
                builder.maxRetryCount, builder.retrySleepTime));

        if (builder.cookie != null) {
            httpContext.setAttribute(ClientContext.COOKIE_STORE, builder.cookie);
        }
        Map<AuthScope, Credentials> credentials = builder.credentials;
        if (credentials != null) {
            for (Map.Entry<AuthScope, Credentials> creden : credentials
                    .entrySet()) {
                httpClient.getCredentialsProvider().setCredentials(
                        creden.getKey(), creden.getValue());
            }
        }
        if (builder.proxy != null) {
            httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, builder.proxy);
        }
        if (builder.customRedirectHandler != null) {
            httpClient.setRedirectHandler(builder.customRedirectHandler);
        }

        if (builder.isPreemtive) {
            httpClient.addRequestInterceptor(
                    new PreemtiveAuthRequestInterceptor(), 0);
        } else {
            httpClient
                    .removeRequestInterceptorByClass(PreemtiveAuthRequestInterceptor.class);
        }
        mExecutor = new NetExecutor(httpClient,httpContext,mCache,mPool,responseHandler);

    }

    public <T> Event<T> postEvent(Event<T> mEvent) {
        responseHandler.postPreExecute(mEvent);
        String url = mEvent.getDataSet().getUrl();
        if (url == null) {
            throw new IllegalArgumentException("url must not be null");
        }
        if (!url.startsWith("http")) {
            if (!TextUtils.isEmpty(endPoint)) {
                url = endPoint + url;
            }
        }
        if (mEvent.getDataSet().getMethod() <= 4) {
            url = UrlUtils.getUrlWithQueryString(isUrlEncodeEnable, url,
                    mEvent.getDataSet());
        }
        mEvent.getDataSet().setUrl(url);
        mEvent.setCache(cache ? mEvent.isCache() : false);
        mEvent.setEventNo(mEventNo.incrementAndGet());
        NetworkTask<T> task = new NetworkTask<>(mExecutor,mEvent);

        majorExecutor.execute(task);

        if (mEvent.getTarget() != null) {
            List<Event<?>> list = eventMap.get(mEvent.getTarget());
            synchronized (eventMap) {
                if (list == null) {
                    list = Collections
                            .synchronizedList(new LinkedList<Event<?>>());
                    eventMap.put(mEvent.getTarget(), list);
                }
                list.add(mEvent);
            }
            Iterator<Event<?>> iterator = list.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().shoudGc()) {
                    iterator.remove();
                }
            }
        }
        return mEvent;

    }

    public void terminate() {
        cancelAllRequests();
        majorExecutor.shutdown();
        assistExecutor.shutdown();
        instance = null;

    }

    /**
     * @param scope
     * @param credentials
     */
    public void setBasicAuth(AuthScope scope, Credentials credentials,
                             boolean preemtive) {
        setCredentials(scope, credentials);
        if (preemtive) {
            httpClient.addRequestInterceptor(
                    new PreemtiveAuthRequestInterceptor(), 0);
        } else {
            httpClient
                    .removeRequestInterceptorByClass(PreemtiveAuthRequestInterceptor.class);

        }
    }

    public void setCredentials(AuthScope authScope, Credentials credentials) {
        if (credentials == null) {
            Log.d(LOG_TAG, "Provided credentials are null, not setting");
            return;
        }
        this.httpClient.getCredentialsProvider().setCredentials(
                authScope == null ? AuthScope.ANY : authScope, credentials);
    }

    public void clearCredentialsProvider() {
        this.httpClient.getCredentialsProvider().clear();
    }

    public void cancelRequests(final Object target) {
        if (target == null) {
            Log.e(LOG_TAG, "Passed null Context to cancelRequests");
            return;
        }
        List<Event<?>> requestList = eventMap.get(target);
        if (requestList != null) {
            for (Event<?> requestHandle : requestList) {
                requestHandle.cancel();
            }
            eventMap.remove(target);
        }
    }

    /**
     * Cancels all pending (or potentially active) requests.
     * <p>
     * &nbsp;
     * </p>
     * <b>Note:</b> This will only affect requests which were created with a
     * non-null android Context. This method is intended to be used in the
     * onDestroy method of your android activities to destroy all requests which
     * are no longer required.
     */
    public void cancelAllRequests() {
        for (List<Event<?>> requestList : eventMap.values()) {
            if (requestList != null) {
                for (Event<?> requestHandle : requestList) {
                    requestHandle.cancel();
                }
            }
        }
        eventMap.clear();
    }

    public static boolean isInputStreamGZIPCompressed(
            final PushbackInputStream inputStream) throws IOException {
        if (inputStream == null)
            return false;

        byte[] signature = new byte[2];
        int readStatus = inputStream.read(signature);
        inputStream.unread(signature);
        int streamHeader = ((int) signature[0] & 0xff)
                | ((signature[1] << 8) & 0xff00);
        return readStatus == 2 && GZIPInputStream.GZIP_MAGIC == streamHeader;
    }

    public static class InflatingEntity extends HttpEntityWrapper {

        public InflatingEntity(HttpEntity wrapped) {
            super(wrapped);
        }

        InputStream wrappedStream;
        PushbackInputStream pushbackStream;
        GZIPInputStream gzippedStream;

        @Override
        public InputStream getContent() throws IOException {
            wrappedStream = wrappedEntity.getContent();
            pushbackStream = new PushbackInputStream(wrappedStream, 2);
            if (isInputStreamGZIPCompressed(pushbackStream)) {
                gzippedStream = new GZIPInputStream(pushbackStream);
                return gzippedStream;
            } else {
                return pushbackStream;
            }
        }

        @Override
        public long getContentLength() {
            return wrappedEntity == null ? 0 : wrappedEntity.getContentLength();
        }

        @Override
        public void consumeContent() throws IOException {
            silentCloseInputStream(wrappedStream);
            silentCloseInputStream(pushbackStream);
            silentCloseInputStream(gzippedStream);
            super.consumeContent();
        }
    }

    public static void silentCloseInputStream(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException e) {
            Log.w(LOG_TAG, "Cannot close input stream", e);
        }
    }

    /**
     * A utility function to close an output stream without raising an
     * exception.
     *
     * @param os output stream to close safely
     */
    public static void silentCloseOutputStream(OutputStream os) {
        try {
            if (os != null) {
                os.close();
            }
        } catch (IOException e) {
            Log.w(LOG_TAG, "Cannot close output stream", e);
        }
    }

    public static void readAndCloseStream(InputStream is) {
        final byte[] bytes = new byte[32 * 1024];
        try {
            while (is.read(bytes, 0, 32 * 1024) != -1) ;
        } catch (IOException ignored) {
        } finally {
            silentCloseInputStream(is);
        }
    }

}
