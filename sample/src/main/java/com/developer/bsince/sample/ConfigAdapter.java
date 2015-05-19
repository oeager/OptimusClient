package com.developer.bsince.sample;

import android.content.Context;
import android.util.Log;

import com.bsince.optimus.client.MetaAdapter;
import com.bsince.optimus.client.OptimusConfigBuilder;
import com.bsince.optimus.cookie.PersistentCookieStore;
import com.bsince.optimus.custom.imp.MySSLSocketFactory;

/**
 * Created by oeager on 2015/5/19.
 */
public class ConfigAdapter implements MetaAdapter {
    @Override
    public void applyOptions(Context mContext, OptimusConfigBuilder builder) {
        builder.endPoint("http://192.168.1.226:8080/HttpService")
                .cookie(new PersistentCookieStore(mContext))
                .urlEncodeEnable(true)
                .connectionTimeOut(5000)
                .isCache(true)
                .responseTimeOut(5000)
//                 .addcredentials()
//                .httpCacheDirectory()
//                .sslSocketFactory(MySSLSocketFactory.getFixedSocketFactory())
//                .allRequestHeader()
//                .assisExecutor()
//                .mainExecutor()
//                .httpCacheDirectory()
//                .enableRedirects()
                //...
                //...
                //...
                .userAgent("optmusclinet/1.0");

    }
}
