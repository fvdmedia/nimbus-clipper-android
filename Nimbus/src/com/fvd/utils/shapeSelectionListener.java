package com.fvd.utils;

import android.view.MotionEvent;

public interface shapeSelectionListener {
	public void onSelectionChanged(int shSize, int fSize, int shColor);
	public void onTextChanged(String text, boolean stroke);
	public void onTouch(MotionEvent event, int act);
}
