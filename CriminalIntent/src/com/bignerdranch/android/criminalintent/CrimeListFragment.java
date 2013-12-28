package com.bignerdranch.android.criminalintent;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class CrimeListFragment extends ListFragment {
	
	private static final String TAG = "CrimeListFragment";
	private ArrayList<Crime> mCrimes;
	private boolean mSubtitleVisible;
	private Callbacks mCallbacks;
	private Button mAddFirstRecordButton;
	
	public interface Callbacks {
		void onCrimeSelected(Crime crime);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallbacks = (Callbacks) activity;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);
		mSubtitleVisible = false;
		getActivity().setTitle(R.string.crimes_title);
		mCrimes = CrimeLab.get(getActivity()).getCrimes();
		CrimeAdapter adapter = new CrimeAdapter(mCrimes);
		setListAdapter(adapter);
	}
	
	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater layout, ViewGroup parent, Bundle savedInstanceState){
		// View v = super.onCreateView(layout, parent, savedInstanceState);
		View v = layout.inflate(R.layout.fragment_crime_list, parent, false);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (mSubtitleVisible) {
				getActivity().getActionBar().setSubtitle(R.string.subtitle);
			}
		}
		
		ListView listView = (ListView) v.findViewById(android.R.id.list);
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			registerForContextMenu(listView);
		} else {
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
				
				public boolean onActionItemClicked(ActionMode mode,
						MenuItem item) {
					switch (item.getItemId()) {
						case R.id.menu_item_delete_crime:
							CrimeAdapter adapter = (CrimeAdapter) getListAdapter();
							CrimeLab crimeLab = CrimeLab.get(getActivity());
							for (int i = adapter.getCount() -1; i >=0; i--) {
								if (getListView().isItemChecked(i)) {
									crimeLab.deleteCrime(adapter.getItem(i));
								}
							}
							mode.finish();
							adapter.notifyDataSetChanged();
							return true;
						default:
							return false;
					}
				}

				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.crime_list_item_context, menu);
					return true;
				}

				public void onDestroyActionMode(ActionMode mode) {
					// Not required
				}

				public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public void onItemCheckedStateChanged(ActionMode arg0,
						int arg1, long arg2, boolean arg3) {
					// TODO Auto-generated method stub
					
				}
				
			});
		}
		
		mAddFirstRecordButton = (Button) v.findViewById(R.id.add_first_record);
		mAddFirstRecordButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				addNewCrime();
			}
		});
		return v;
	}
	
	@Override 
	public void onListItemClick(ListView l, View v, int position, long id){
		Crime c = ((CrimeAdapter)getListAdapter()).getItem(position);
		mCallbacks.onCrimeSelected(c);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		((CrimeAdapter)getListAdapter()).notifyDataSetChanged();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_crime_list, menu);
		MenuItem showSubtitle = menu.findItem(R.id.menu_item_show_subtitle);
		if (showSubtitle != null) {
			if (mSubtitleVisible) {
				showSubtitle.setTitle(R.string.hide_subtitle);
			} else {
				showSubtitle.setTitle(R.string.show_subtitle);
			}
		}
	}
	
	@TargetApi(11)
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
			case R.id.menu_item_new_crime:
				Crime crime = new Crime();
				CrimeLab.get(getActivity()).addCrime(crime);
				// Intent i = new Intent(getActivity(), CrimePagerActivity.class);
				// i.putExtra(CrimeFragment.EXTRA_CRIME_ID,crime.getId());
				// startActivityForResult(i,0);
				((CrimeAdapter)getListAdapter()).notifyDataSetChanged();
				mCallbacks.onCrimeSelected(crime);
				return true;
			case R.id.menu_item_show_subtitle:
				if(getActivity().getActionBar().getSubtitle() == null) {
					getActivity().getActionBar().setSubtitle(R.string.subtitle);
					mSubtitleVisible = true;
					item.setTitle(R.string.hide_subtitle);
				} else {
					getActivity().getActionBar().setSubtitle(null);
					item.setTitle(R.string.show_subtitle);
					mSubtitleVisible = false;
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		getActivity().getMenuInflater().inflate(R.menu.crime_list_item_context, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		int position = info.position;
		CrimeAdapter adapter = (CrimeAdapter)getListAdapter();
		Crime crime = adapter.getItem(position);
		
		switch (item.getItemId()) {
		case R.id.menu_item_delete_crime:
			CrimeLab.get(getActivity()).deleteCrime(crime);
			adapter.notifyDataSetChanged();
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	public void updateUI() {
		((CrimeAdapter)getListAdapter()).notifyDataSetChanged();
	}
	
	private class CrimeAdapter extends ArrayAdapter<Crime> {
		public CrimeAdapter(ArrayList<Crime> crimes){
			super(getActivity(),0,crimes);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			
			if (convertView == null){
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_crime, null);
			}
			
			Crime c = getItem(position);
			
			TextView titleTextView = (TextView) convertView.findViewById(R.id.crime_list_item_titleTextView);
			titleTextView.setText(c.getTitle());
			
			TextView dateTextView = (TextView) convertView.findViewById(R.id.crime_list_item_dateTextView);
			dateTextView.setText(c.getDate().toString());
			
			CheckBox solvedCheckBox = (CheckBox) convertView.findViewById(R.id.crime_list_item_solvedCheckBox);
			solvedCheckBox.setChecked(c.isSolved());
			
			return convertView;
		}
	}
	
	private void addNewCrime(){
		Crime crime = new Crime();
		CrimeLab.get(getActivity()).addCrime(crime);
		Intent i = new Intent(getActivity(), CrimePagerActivity.class);
		i.putExtra(CrimeFragment.EXTRA_CRIME_ID,crime.getId());
		startActivityForResult(i,0);
	}
	
}
