/*
 * Copyright (c) 2015 - 2017. 小石头信息技术有限公司 版权所有
 *
 * 技术扣扣：1801902666
 * 公司网站：http://www.shitouerp.com
 */

package com.xy.android.videocall;

import android.text.TextUtils;
import android.util.Log;

/**
 * Created by DingYu on 2017/4/25.
 */

public class L {

	private static final String TAG = "ding";

	private static boolean debug = false;

	public static void setDebug(boolean isDebug) {
		debug = isDebug;
	}

	private static String getMsg(String msg) {
		if (TextUtils.isEmpty(msg)) {
			msg = "|************************************************|\n";
			msg += "|     the msg to be logcated is Empty or Null    |\n";
			msg += "|************************************************|";
		}
		return msg;
	}

	public static void i(String tag, String msg) {
		if (debug) {
			Log.i(tag, getMsg(msg));
		}
	}

	public static void v(String tag, String msg) {
		if (debug) {
			Log.v(tag, getMsg(msg));
		}
	}

	public static void d(String tag, String msg) {
		if (debug) {
			Log.d(tag, getMsg(msg));
		}
	}

	public static void e(String tag, String msg) {
		if (debug) {
			Log.e(tag, getMsg(msg));
		}
	}

	public static void w(String tag, String msg) {
		if (debug) {
			Log.w(tag, getMsg(msg));
		}
	}

	//
	public static void i(String msg) {
		L.i(TAG, getMsg(msg));
	}

	public static void v(String msg) {
		L.v(TAG, getMsg(msg));
	}

	public static void d(String msg) {
		L.d(TAG, getMsg(msg));
	}

	public static void e(String msg) {
		L.e(TAG, getMsg(msg));
	}

	public static void w(String msg) {
		L.w(TAG, getMsg(msg));
	}
}
