package uk.me.ajmfulcher.hellomoon;

import android.content.Context;
import android.media.MediaPlayer;

public class AudioPlayer {
	private MediaPlayer mPlayer;
	private Boolean paused = false;
	
	public void stop() {
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}
	
	public void play(Context c) {
		stop();
		mPlayer = MediaPlayer.create(c, R.raw.one_small_step);
		
		mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				stop();
				
			}
		});
		
		mPlayer.start();
	}
	
	public void pause() {
		if (mPlayer == null)
			return;
		if (paused == false) {
			mPlayer.pause();
			paused = true;
		} else {
			mPlayer.start();
			paused = false;
		}
	}
}
