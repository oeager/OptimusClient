package com.bsince.optimus.utils;

import android.util.Log;

public class L {
	private final static boolean isDebug = true;
	private static final String LOG_FORMAT = "%1$s\n%2$s";
	private final static String TAGS = "OptimusClient";
	
	
	public static void v(String tag, String msg) {
		if (isDebug)
			Log.v(tag, msg);
	}

	public static void v(String tag, String msg, Throwable t) {
		if (isDebug)
			Log.v(tag, msg, t);
	}

	public static void d(String tag, String msg) {
		if (isDebug)
			Log.d(tag, msg);
	}

	public static void d(String tag, String msg, Throwable t) {
		if (isDebug)
			Log.d(tag, msg, t);
	}

	public static void i(String tag, String msg) {
		if (isDebug)
			Log.i(tag, msg);
	}

	public static void i(String tag, String msg, Throwable t) {
		if (isDebug)
			Log.i(tag, msg, t);
	}

	public static void w(String tag, String msg) {
		if (isDebug)
			Log.w(tag, msg);
	}

	public static void w(String tag, String msg, Throwable t) {
		if (isDebug)
			Log.w(tag, msg, t);
	}

	public static void e(String tag, String msg) {
		if (isDebug)
			Log.e(tag, msg);
	}

	public static void e(String tag, String msg, Throwable t) {
		if (isDebug)
			Log.e(tag, msg, t);
	}
	
	public static void v( String msg) {
		if (isDebug)
			Log.v(TAGS, msg);
	}

	public static void v(String msg, Throwable t) {
		if (isDebug)
			Log.v(TAGS,msg, t);
	}

	public static void d(String msg) {
		if (isDebug)
			Log.d(TAGS, msg);
	}

	public static void d( String msg, Throwable t) {
		if (isDebug)
			Log.d(TAGS, msg, t);
	}

	public static void i( String msg) {
		if (isDebug)
			Log.i(TAGS, msg);
	}

	public static void i(String msg, Throwable t) {
		if (isDebug)
			Log.i(TAGS, msg, t);
	}

	public static void w( String msg) {
		if (isDebug)
			Log.w(TAGS, msg);
	}

	public static void w( String msg, Throwable t) {
		if (isDebug)
			Log.w( msg, t);
	}

	public static void e(String msg) {
		if (isDebug)
			Log.e(TAGS, msg);
	}

	public static void e( String msg, Throwable t) {
		if (isDebug)
			Log.e(TAGS, msg, t);
	}
	
	
	public static void d(String message, Object... args) {
		log(Log.DEBUG, null, message, args);
	}

	public static void i(String message, Object... args) {
		log(Log.INFO, null, message, args);
	}

	public static void w(String message, Object... args) {
		log(Log.WARN, null, message, args);
	}

	public static void e(Throwable ex) {
		log(Log.ERROR, ex, null);
	}

	public static void e(String message, Object... args) {
		log(Log.ERROR, null, message, args);
	}

	public static void e(Throwable ex, String message, Object... args) {
		log(Log.ERROR, ex, message, args);
	}

	private static void log(int priority, Throwable ex, String message, Object... args) {
		if (!isDebug) return;
		if (args.length > 0) {
			message = String.format(message, args);
		}

		String log;
		if (ex == null) {
			log = message;
		} else {
			String logMessage = message == null ? ex.getMessage() : message;
			String logBody = Log.getStackTraceString(ex);
			log = String.format(LOG_FORMAT, logMessage, logBody);
		}
		Log.println(priority, TAGS, log);
	}
	
}
