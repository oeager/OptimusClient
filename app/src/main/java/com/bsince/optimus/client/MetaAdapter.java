package com.bsince.optimus.client;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oeager on 2015/5/18.
 */
public interface MetaAdapter {

    void applyOptions(Context mContext,OptimusConfigBuilder builder);

    public static class ManifestParser{

        private static final String META_ADAPTER = "MetaAdapter";

        public static List<MetaAdapter> parse(Context mContext){

            List<MetaAdapter> adapters = new ArrayList<>();

            try {
                ApplicationInfo appInfo = mContext.getPackageManager()
                        .getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
                if (appInfo.metaData != null) {
                    for (String key : appInfo.metaData.keySet()) {
                        if (META_ADAPTER.equals(appInfo.metaData.get(key))) {
                            adapters.add(parseModule(key));
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException("Unable to find metadata to parse MetaAdapter", e);
            }

            return adapters;

        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        private static MetaAdapter parseModule(String className) {
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Unable to find MetaAdapter implementation", e);
            }

            Object module;
            try {
                module = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Unable to instantiate MetaAdapter implementation for " + clazz,
                        e);
            }

            if (!(module instanceof MetaAdapter)) {
                throw new RuntimeException("Expected instanceof MetaAdapter, but found: " + module);
            }
            return (MetaAdapter) module;
        }
    }
}


