package com.bsince.optimus.event;

import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;

import com.bsince.optimus.callback.LoadingProcessor;
import com.bsince.optimus.callback.Processor;
import com.bsince.optimus.client.respon.OptHttpResponse;
import com.bsince.optimus.data.SimpleDataSet;
import com.bsince.optimus.utils.HeaderUtils;
import com.bsince.optimus.utils.L;


public class ImageEvent extends Event<Bitmap> {

	private final Config mDecodeConfig;
	private final int mMaxWidth;
	private final int mMaxHeight;
	private static final Object sDecodeLock = new Object();
	
	public ImageEvent(String url, Processor<Bitmap> processor, int maxWidth, int maxHeight,
			Config decodeConfig) {
		super(new SimpleDataSet(url), processor);
		this.mMaxWidth = maxWidth;
		this.mMaxHeight = maxHeight;
		this.mDecodeConfig = decodeConfig;
		
	}
	
	public ImageEvent(String url, Processor<Bitmap> processor, int maxWidth, int maxHeight,
			Config decodeConfig,LoadingProcessor loadingProcessor) {
		super(new SimpleDataSet(url), processor,loadingProcessor);
		this.mMaxWidth = maxWidth;
		this.mMaxHeight = maxHeight;
		this.mDecodeConfig = decodeConfig;
		
	}

	 private static int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary,
	            int actualSecondary) {
	        if (maxPrimary == 0 && maxSecondary == 0) {
	            return actualPrimary;
	        }
	        // If primary is unspecified, scale primary to match secondary's scaling ratio.
	        if (maxPrimary == 0) {
	            double ratio = (double) maxSecondary / (double) actualSecondary;
	            return (int) (actualPrimary * ratio);
	        }

	        if (maxSecondary == 0) {
	            return maxPrimary;
	        }

	        double ratio = (double) actualSecondary / (double) actualPrimary;
	        int resized = maxPrimary;
	        if (resized * ratio > maxSecondary) {
	            resized = (int) (maxSecondary / ratio);
	        }
	        return resized;
	    }

	 /**
     * Returns the largest power-of-two divisor for use in downscaling a bitmap
     * that will not result in the scaling past the desired dimensions.
     *
     * @param actualWidth Actual width of the bitmap
     * @param actualHeight Actual height of the bitmap
     * @param desiredWidth Desired width of the bitmap
     * @param desiredHeight Desired height of the bitmap
     */
    // Visible for testing.
    static int findBestSampleSize(
            int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }

        return (int) n;
    }

	@Override
	public OptHttpResponse<Bitmap> parse(byte[] data,
			Map<String, String> responseHeaders) {
		// Serialize all decode on a global lock to reduce concurrent heap usage.
        synchronized (sDecodeLock) {
            try {
                return doParse(data,responseHeaders);
            } catch (OutOfMemoryError e) {
                L.e("Caught OOM for"+data.length+"byte image, url="+getDataSet().getUrl());
                return OptHttpResponse.error(new Exception(e));
            }
        }
	}
	
	OptHttpResponse<Bitmap> doParse(byte [] data,Map<String, String> responseHeaders){

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        Bitmap bitmap = null;
        if (mMaxWidth == 0 && mMaxHeight == 0) {
            decodeOptions.inPreferredConfig = mDecodeConfig;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
        } else {
            // If we have to resize this image, first get the natural bounds.
            decodeOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
            int actualWidth = decodeOptions.outWidth;
            int actualHeight = decodeOptions.outHeight;

            // Then compute the dimensions we would ideally like to decode to.
            int desiredWidth = getResizedDimension(mMaxWidth, mMaxHeight,
                    actualWidth, actualHeight);
            int desiredHeight = getResizedDimension(mMaxHeight, mMaxWidth,
                    actualHeight, actualWidth);

            // Decode to the nearest power of two scaling factor.
            decodeOptions.inJustDecodeBounds = false;
            // TODO(ficus): Do we need this or is it okay since API 8 doesn't support it?
            // decodeOptions.inPreferQualityOverSpeed = PREFER_QUALITY_OVER_SPEED;
            decodeOptions.inSampleSize =
                findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
            Bitmap tempBitmap =
                BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);

            // If necessary, scale down to the maximal acceptable size.
            if (tempBitmap != null && (tempBitmap.getWidth() > desiredWidth ||
                    tempBitmap.getHeight() > desiredHeight)) {
                bitmap = Bitmap.createScaledBitmap(tempBitmap,
                        desiredWidth, desiredHeight, true);
                tempBitmap.recycle();
            } else {
                bitmap = tempBitmap;
            }
        }

        if (bitmap == null) {
            return OptHttpResponse.error(new Exception("parse image erro ,because of the bitmap is null"));
        } else {
            return OptHttpResponse.success(bitmap, HeaderUtils.parseCacheHeaders(responseHeaders,data));
        }
    
	}

}
