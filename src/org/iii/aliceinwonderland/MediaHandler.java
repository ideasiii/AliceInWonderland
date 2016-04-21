package org.iii.aliceinwonderland;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class MediaHandler
{
	private MediaPlayer		mPlayer			= null;
	private AudioManager	audioManager	= null;
	private Activity		theActivity		= null;

	public MediaHandler(Activity activity)
	{
		theActivity = activity;
		audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
	}

	@Override
	protected void finalize() throws Throwable
	{
		audioManager = null;
		if (null != mPlayer)
		{
			mPlayer.release();
			mPlayer = null;
		}
		super.finalize();
	}

	public boolean play()
	{
		int nMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nMaxVolume / 2, AudioManager.FLAG_PLAY_SOUND);
		releasePlayer();
		mPlayer = MediaPlayer.create(theActivity, R.raw.alice);
		mPlayer.setLooping(true);
		mPlayer.setOnCompletionListener(new OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mp)
			{

			}
		});
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mPlayer.setVolume(1f, 1f);
		mPlayer.start();

		return mPlayer.isPlaying();
	}

	public void stop()
	{
		if (null != mPlayer)
		{
			if (mPlayer.isPlaying())
				mPlayer.stop();
		}
	}

	public void pause()
	{
		if (null != mPlayer)
		{
			if (mPlayer.isPlaying())
				mPlayer.pause();
		}
	}

	public void start()
	{
		if (null != mPlayer)
		{
			if (!mPlayer.isPlaying())
			{
				mPlayer.start();
			}
		}
	}

	public void releasePlayer()
	{
		stop();
		if (null != mPlayer)
		{
			mPlayer.release();
			mPlayer = null;
		}
	}

}
