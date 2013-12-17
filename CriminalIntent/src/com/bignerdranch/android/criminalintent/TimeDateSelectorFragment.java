package com.bignerdranch.android.criminalintent;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;

public class TimeDateSelectorFragment extends DialogFragment {
	
	public static final String EXTRA_DATE = "com.bignerdranch.android.criminalintent.date";
	private Button mDateButton;
	private Button mTimeButton;
	private Date mDate;
	public static final String DIALOG_DATE = "date";
	private static final int REQUEST_DATE = 0;
	
	public Dialog onCreateDialog(Bundle savedInstanceState){
		View v = getActivity().getLayoutInflater()
				.inflate(R.layout.dialog_time_date_selector, null);
		mDate = (Date) getArguments().getSerializable(EXTRA_DATE);
		mTimeButton = (Button) v.findViewById(R.id.dialog_time_date_selector_timeSelect);
		mDateButton = (Button) v.findViewById(R.id.dialog_time_date_selector_dateSelect);
		mDateButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FragmentManager fm = getActivity()
						.getSupportFragmentManager();
				DatePickerFragment dialog = DatePickerFragment.newInstance(mDate);
				dialog.setTargetFragment(TimeDateSelectorFragment.this, REQUEST_DATE);
				dialog.show(fm, DIALOG_DATE);
			}
		});
		
		return new AlertDialog.Builder(getActivity())
		.setView(v)
		.setTitle(R.string.date_picker_title)
		.create();
	}
	
	public static TimeDateSelectorFragment newInstance (Date date){
		Bundle args = new Bundle();
		args.putSerializable(EXTRA_DATE, date);
		TimeDateSelectorFragment fragment = new TimeDateSelectorFragment();
		fragment.setArguments(args);
		return fragment;
	}
	
	private void sendResult(int resultCode){
		if (getTargetFragment() == null)
			return;
		Intent i = new Intent();
		i.putExtra(EXTRA_DATE, mDate);
		getTargetFragment()
			.onActivityResult(getTargetRequestCode(), resultCode, i);
	}
	
	public void onPause(){
		super.onPause();
		sendResult(Activity.RESULT_OK);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if (resultCode != Activity.RESULT_OK)
			return;
		if (requestCode == REQUEST_DATE){
			mDate = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);		
		}
	}

}
