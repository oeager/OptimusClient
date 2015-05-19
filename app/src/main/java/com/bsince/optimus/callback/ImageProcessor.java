package com.bsince.optimus.callback;

import android.graphics.Bitmap;
import android.view.View;

public interface ImageProcessor {



	void onLoadingStarted(String imageUri, View view);

	void onLoadingFailed(String imageUri, View view, Exception e);

	
	void onLoadingComplete(String imageUri, View view, Bitmap bitmap);

	
	void onLoadingCancelled(String imageUri, View view);

}
