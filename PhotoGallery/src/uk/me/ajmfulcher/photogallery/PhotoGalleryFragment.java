package uk.me.ajmfulcher.photogallery;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class PhotoGalleryFragment extends Fragment {
	
	private static final String TAG = "PhotoGalleryFragment";
	
	GridView mGridView;
	ArrayList<GalleryItem> mItems;
	ThumbnailDownloader<ImageView> mThumbnailThread;
	
	private Integer flickrPage = 0;
	private Boolean mLoading = true;
	private int mPreviousTotal = 0;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		new FetchItemsTask().execute(flickrPage + 1);
		
		mThumbnailThread = new ThumbnailDownloader<ImageView>(getActivity(), new Handler());
		mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
			public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
				if(isVisible()) {
					imageView.setImageBitmap(thumbnail);
				}
			}
		});
		mThumbnailThread.start();
		mThumbnailThread.getLooper();
		Log.i(TAG, "Background thread started");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
		mGridView = (GridView) v.findViewById(R.id.gridView);
		mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (mLoading) {
					if (totalItemCount > mPreviousTotal) {
						mLoading = false;
						mPreviousTotal = totalItemCount;
						flickrPage++;
					}
				}
				final int lastItem = firstVisibleItem + visibleItemCount;
				if (!mLoading && lastItem == totalItemCount) {
					new FetchItemsTask().execute(flickrPage + 1);
					mLoading = true;
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// Not required
			}
		});
		
		setupAdapter();
		
		return v;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mThumbnailThread.quit();
		Log.i(TAG, "Background thread destroyed");
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mThumbnailThread.clearQueue();
	}
	
	void setupAdapter() {
		if (getActivity() == null || mGridView == null) return;
		
		if (mItems != null) {
			mGridView.setAdapter(new GalleryItemAdapter(mItems));
		} else {
			mGridView.setAdapter(null);
		}
	}
	
	private class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {
		public GalleryItemAdapter(ArrayList<GalleryItem> items) {
			super(getActivity(), 0, items);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item, parent, false);
			}
			
			ImageView imageView = (ImageView)convertView.findViewById(R.id.gallery_item_imageView);
			imageView.setImageResource(R.drawable.brian_up_close);
			GalleryItem item = getItem(position);
			mThumbnailThread.queueThumbnail(imageView, item.getUrl());
			
			return convertView;
		}
	}
	
	private class FetchItemsTask extends AsyncTask<Integer,Void,ArrayList<GalleryItem>> {

		@Override
		protected ArrayList<GalleryItem> doInBackground(Integer... params) {
			return new FlickrFetchr().fetchItems(params[0]);
		}
		
		@Override
		protected void onPostExecute(ArrayList<GalleryItem> items) {
			if (mItems == null) {
				mItems = items;
				setupAdapter();
			} else {
				mItems.addAll(items);
				ArrayAdapter adapter = (ArrayAdapter) mGridView.getAdapter();
				adapter.notifyDataSetChanged();
			}
		}
		
	}

}
