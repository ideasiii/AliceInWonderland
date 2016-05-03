package org.iii.aliceinwonderland;

import java.util.HashMap;
import android.content.Context;
import sdk.ideas.tracker.Tracker;
import sdk.ideas.common.OnCallbackResult;


public class TrackerHandler
{
	private Tracker mTracker = null;
	
	public static String APP_ID = "1462241606197";
	
	public TrackerHandler(Context mContext)
	{
		mTracker = new Tracker(mContext);
	}
	public void init()
	{
		mTracker.setOnCallbackResultListener(trackerCallback);
		mTracker.startTracker(APP_ID);
	}
	public void send(HashMap<String,String> data)
	{
		mTracker.track(data);
	}
	public void stopTracker()
	{
		mTracker.stopTracker();
	}
	@Override
	protected void finalize() throws Throwable
	{
		mTracker = null;
		super.finalize();
	}
	
	
	
	private OnCallbackResult trackerCallback = new OnCallbackResult()
	{

		@Override
		public void onCallbackResult(final int result, final int what, final int from,
				final HashMap<String, String> message)
		{
			Logs.showTrace("Tracker " + "Result: " + String.valueOf(result) + "from: " + String.valueOf(from)
					+ "message" + message);
		}
		
	};
	
	

}
