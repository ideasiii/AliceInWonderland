package org.iii.aliceinwonderland;

import android.app.Activity;
import android.os.Handler;
import android.view.View;

public class FlipperHandler extends BaseHandler
{
	private final int	RESOURCE_FLIPPER	= R.id.flipperViewOption;
	private FlipperView	flipper				= null;
	public static int	VIEW_ID_STORY		= 0;

	public FlipperHandler(Activity activity, Handler handler)
	{
		super(activity, handler);
	}

	public boolean init()
	{
		flipper = (FlipperView) theActivity.findViewById(RESOURCE_FLIPPER);
		if (null == flipper)
		{
			Logs.showTrace("Flipper View Init Fail");
			return false;
		}

		flipper.setNotifyHandler(theHandler);
		VIEW_ID_STORY = flipper.addChild(R.layout.flipper_story);

		return true;
	}

	public View getView(final int nId)
	{
		return flipper.getChildView(nId);
	}

	public void showView(final int nViewId)
	{
		if (null != flipper)
		{
			flipper.showView(nViewId);
		}
	}

	public void close()
	{
		if (null != flipper)
		{
			flipper.close();
		}
	}
}
