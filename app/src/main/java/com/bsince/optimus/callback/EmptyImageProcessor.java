package com.bsince.optimus.callback;

import android.graphics.Bitmap;
import android.view.View;

public class EmptyImageProcessor implements ImageProcessor {

	@Override
	public void onLoadingStarted(String imageUri, View view) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoadingFailed(String imageUri, View view, Exception e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoadingCancelled(String imageUri, View view) {
		// TODO Auto-generated method stub

	}

}
