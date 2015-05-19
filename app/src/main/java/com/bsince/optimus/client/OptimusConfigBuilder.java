package com.bsince.optimus.client;

import android.os.Environment;
import android.text.TextUtils;

import com.bsince.optimus.custom.imp.MySSLSocketFactory;
import com.bsince.optimus.custom.imp.RetryHandler;
import com.bsince.optimus.data.Constants;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.RedirectHandler;
import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by oeager on 2015/5/18.
 */
public class OptimusConfigBuilder {

    int httpPort;

    int httpsPort;

    int socketBufferSize;

    int maxConnections;

    int connectionTimeOut;

    int responseTimeOut;

    int maxRetryCount;

    int maxHttpCacheSize;

    int retrySleepTime;

    boolean fixNoHttpResponseException = false;

    boolean isUrlEncodeEnable = true;;

    boolean enableRedirects = false;

    boolean isPreemtive = false;

    boolean cache = true;

    String userAgent;

    String endPoint;

    HttpHost proxy;

    Map<String, String> allRequestHeader;

    File httpCacheDirectory;

    CookieStore cookie;

    Map<AuthScope, Credentials> credentials;

    RedirectHandler customRedirectHandler;

    SSLSocketFactory sslSocketFactory;

    ExecutorService mainExecutor;

    ExecutorService assisExecutor;


    public OptimusConfigBuilder httpPort(int httpPort) {
        this.httpPort = httpPort;
        return this;
    }

    public OptimusConfigBuilder httpsPort(int httpsPort) {
        this.httpsPort = httpsPort;
        return this;
    }

    public OptimusConfigBuilder socketBufferSize(int socketBufferSize) {
        this.socketBufferSize = socketBufferSize;
        return this;
    }

    public OptimusConfigBuilder maxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public OptimusConfigBuilder connectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
        return this;
    }

    public OptimusConfigBuilder responseTimeOut(int responseTimeOut) {
        this.responseTimeOut = responseTimeOut;
        return this;
    }

    public OptimusConfigBuilder maxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
        return this;
    }

    public OptimusConfigBuilder maxHttpCacheSize(int maxSize) {
        this.maxHttpCacheSize = maxSize;
        return this;
    }

    public OptimusConfigBuilder retrySleepTime(int retrySleepTime) {
        this.retrySleepTime = retrySleepTime;
        return this;
    }

    public OptimusConfigBuilder fixNoHttpResponseException(
            boolean fixNoHttpResponseException) {
        this.fixNoHttpResponseException = fixNoHttpResponseException;
        return this;
    }

    public OptimusConfigBuilder urlEncodeEnable(boolean isUrlEncodeEnable) {
        this.isUrlEncodeEnable = isUrlEncodeEnable;
        return this;
    }

    public OptimusConfigBuilder enableRedirects(boolean enableRedirects) {
        this.enableRedirects = enableRedirects;
        return this;
    }

    public OptimusConfigBuilder preemtive(boolean isPreemtive) {
        this.isPreemtive = isPreemtive;
        return this;
    }

    public OptimusConfigBuilder isCache(boolean cache) {
        this.cache = cache;
        return this;
    }

    public OptimusConfigBuilder userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public OptimusConfigBuilder endPoint(String endPoint) {
        this.endPoint = endPoint;
        return this;
    }

    public OptimusConfigBuilder proxy(String hostName, int port) {
        this.proxy = new HttpHost(hostName, port);
        return this;
    }

    public OptimusConfigBuilder proxy(HttpHost host) {
        this.proxy = host;
        return this;
    }

    public OptimusConfigBuilder proxy(String hostName, int port, String userName,
                         String passWord) {
        this.proxy = new HttpHost(hostName, port);
        return addcredentials(userName, passWord, hostName, port);
    }

    public OptimusConfigBuilder proxy(String hostName, int port, Credentials credential) {
        this.proxy = new HttpHost(hostName, port);
        return addcredentials(new AuthScope(hostName, port), credential);
    }

    public OptimusConfigBuilder proxy(HttpHost host, Credentials credential) {
        this.proxy = host;
        return addcredentials(
                new AuthScope(host.getHostName(), host.getPort()),
                credential);
    }

    public OptimusConfigBuilder allRequestHeader(Map<String, String> allRequestHeader) {
        this.allRequestHeader = allRequestHeader;
        return this;
    }

    public OptimusConfigBuilder httpCacheDirectory(File rootDirectory) {
        this.httpCacheDirectory = rootDirectory;
        return this;
    }

    public OptimusConfigBuilder cookie(CookieStore cookie) {
        this.cookie = cookie;
        return this;
    }

    public OptimusConfigBuilder addcredentials(String userName, String password) {
        checkCredentials();
        this.credentials.put(AuthScope.ANY,
                new UsernamePasswordCredentials(userName, password));
        return this;
    }

    public OptimusConfigBuilder addcredentials(Credentials credential) {
        checkCredentials();
        this.credentials.put(AuthScope.ANY, credential);
        return this;
    }

    public OptimusConfigBuilder addcredentials(AuthScope scope, Credentials credential) {
        checkCredentials();
        this.credentials.put(scope, credential);
        return this;
    }

    public OptimusConfigBuilder addcredentials(AuthScope scope, String userName,
                                  String password) {
        checkCredentials();
        if (scope == null) {
            scope = AuthScope.ANY;
        }
        this.credentials.put(scope, new UsernamePasswordCredentials(
                userName, password));
        return this;
    }

    public OptimusConfigBuilder addcredentials(String userName, String password,
                                  String hostName, int port) {
        checkCredentials();
        this.credentials.put(new AuthScope(hostName, port),
                new UsernamePasswordCredentials(userName, password));
        return this;
    }

    public OptimusConfigBuilder customRedirectHandler(
            RedirectHandler customRedirectHandler) {
        this.customRedirectHandler = customRedirectHandler;
        return this;
    }

    public OptimusConfigBuilder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    public OptimusConfigBuilder assisExecutor(ExecutorService service) {
        this.assisExecutor = service;
        return this;
    };

    public OptimusConfigBuilder mainExecutor(ExecutorService executorService) {
        this.mainExecutor = executorService;
        return this;
    }

    public OptimusConfigBuilder allowRetryExceptionClass(Class<?> cls) {
        if (cls != null) {
            RetryHandler.addClassToBlacklist(cls);
        }
        return this;
    }


    public OptimusConfigBuilder blockRetryExceptionClass(Class<?> cls) {
        if (cls != null) {
            RetryHandler.addClassToBlacklist(cls);
        }
        return this;
    }

    void checkCredentials() {
        if (this.credentials == null) {
            this.credentials = new HashMap<AuthScope, Credentials>();
        }
    }

     void checkConfiguration() {
        if (httpPort < 1) {
            httpPort = 80;
        }

        if (httpsPort < 1) {
            httpsPort = 443;
        }

        if (socketBufferSize < 1) {
            socketBufferSize = Constants.DEFAULT_SOCKET_BUFFER_SIZE;
        }
        if (maxConnections < 1) {
            maxConnections = Constants.DEFAULT_MAX_CONNECTIONS;
        }
        if (connectionTimeOut < 1) {
            connectionTimeOut = Constants.DEFAULT_SOCKET_TIMEOUT;
        }
        if (responseTimeOut < 1) {
            responseTimeOut = Constants.DEFAULT_SOCKET_TIMEOUT;
        }

        if (maxRetryCount < 0) {
            maxRetryCount = Constants.DEFAULT_MAX_RETRIES;
        }

        if (maxHttpCacheSize <= 0) {
            maxHttpCacheSize = Constants.DEFAULT_DISK_USAGE_BYTES;
        }

        if (retrySleepTime < 0) {
            retrySleepTime = Constants.DEFAULT_RETRY_SLEEP_TIME_MILLIS;
        }

        if (TextUtils.isEmpty(userAgent)) {
            userAgent = "Optimus_bsince_android_version_1.0";
        }

        if (httpCacheDirectory == null) {
            httpCacheDirectory = Environment.getExternalStorageDirectory();
        }
        if (!httpCacheDirectory.exists()) {
            httpCacheDirectory.mkdirs();
        }

        if (sslSocketFactory == null) {
            if (fixNoHttpResponseException) {
                sslSocketFactory = MySSLSocketFactory
                        .getFixedSocketFactory();
            } else {
                sslSocketFactory = SSLSocketFactory.getSocketFactory();
            }
        }

        if (mainExecutor == null) {
            mainExecutor = Executors.newCachedThreadPool();
        }

        if (assisExecutor == null) {
            assisExecutor = Executors.newSingleThreadExecutor();
        }



    }
}

