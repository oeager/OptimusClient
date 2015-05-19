package com.bsince.optimus.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

public class CompatUtils {

	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static boolean isLargeHeap(Context context) {
		return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_LARGE_HEAP) != 0;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static int getLargeMemoryClass(ActivityManager am) {
		return am.getLargeMemoryClass();
	}
}
