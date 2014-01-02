package uk.me.ajmfulcher.photogallery;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;

public class ThumbnailDownloader<Token> extends HandlerThread {

	private static final String TAG = "ThumbnailDownloader";
	private static final int MESSAGE_DOWNLOAD = 0;
	
	private LruCache mMemoryCache;
	private Context mContext;
	
	Handler mHandler;
	Map<Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());
	Handler mResponseHandler;
	Listener<Token> mListener;
	
	public interface Listener<Token> {
		void onThumbnailDownloaded(Token token, Bitmap thumbnail);
	}
	
	public void setListener(Listener<Token> listener) {
		mListener = listener;
	}
	
	public ThumbnailDownloader(Context context, Handler responseHandler) {
		super(TAG);
		mContext = context;
		mResponseHandler = responseHandler;
		createCache();
	}
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onLooperPrepared() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == MESSAGE_DOWNLOAD) {
					@SuppressWarnings("unchecked")
					Token token = (Token)msg.obj;
					Log.i(TAG, "Got a request for url: " + requestMap.get(token));
					handleRequest(token);
				}
			}
		};
	}
	
	public void queueThumbnail(Token token, String url) {
		Log.i(TAG, "Got an URL: " + url);
		requestMap.put(token, url);
		
		mHandler.obtainMessage(MESSAGE_DOWNLOAD, token).sendToTarget();
	}
	
	public void clearQueue() {
		mHandler.removeMessages(MESSAGE_DOWNLOAD);
		requestMap.clear();
	}
	
	private void handleRequest(final Token token) {
		try {
			final String url = requestMap.get(token);
			final Bitmap bitmap;
			if (url == null) return;
			
			if (getBitmapFromMemCache(url) == null) {
				byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
				if (bitmapBytes == null) return;
				bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
				addBitmapToMemoryCache(url,bitmap);
				Log.i(TAG, "Bitmap created");
			} else {
				bitmap = getBitmapFromMemCache(url);
				Log.i(TAG, "Bitmap retrieved from cache");
			}
			
			mResponseHandler.post(new Runnable() {
				public void run() {
					if (requestMap.get(token) != url) return;
					requestMap.remove(token);
					mListener.onThumbnailDownloaded(token, bitmap);
				}
			});
		} catch (IOException ioe) {
			Log.e(TAG, "error downloading image", ioe);
		}
	}
	
	private void createCache() {
		ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		final int memClass = activityManager.getMemoryClass();
		final int cacheSize = 1024* 1024 * memClass / 8;
		
		mMemoryCache = new LruCache<String,Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getRowBytes() * bitmap.getHeight();
			}
		};
	}
	
	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}
	
	private Bitmap getBitmapFromMemCache(String key) {
		return (Bitmap)mMemoryCache.get(key);
	}
}
