package com.fvd.browser;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.webkit.ConsoleMessage;


public interface fvdBrowerEventsListener {
		void onProgressChanged(int progress);
		void onConsoleMessage(String message, int lineNumber, String sourceID);
		void onConsoleMessage(ConsoleMessage cm);
		void onJsAlert(String url, String message, final android.webkit.JsResult result);
		void onReceivedError(int errorCode, String description, String failingUrl);
		void onPageStarted (String url, Bitmap favicon);
		void onPageFinished (String url);
		void onSelectionChanged(final String plainText, final String html, Rect menuRect);
		void onEndSelection();
		void onTap();
		boolean getCanBrowse();
}
