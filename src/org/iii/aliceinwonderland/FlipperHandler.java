package org.iii.aliceinwonderland;

import android.app.Activity;
import android.os.Handler;
import android.view.View;

public class FlipperHandler extends BaseHandler
{
	private final int	RESOURCE_FLIPPER	= R.id.flipperViewOption;
	private FlipperView	flipper				= null;
	public static int	VIEW_ID_KEY_INPUT	= -1;
	public static int	VIEW_ID_SUCCESS		= -1;
	public static int	VIEW_ID_FAIL		= -1;
	private int			mnShowView			= -1;

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
		VIEW_ID_KEY_INPUT = flipper.addChild(R.layout.key_input);
		VIEW_ID_SUCCESS = flipper.addChild(R.layout.success);
		VIEW_ID_FAIL = flipper.addChild(R.layout.fail);

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
			mnShowView = nViewId;
			flipper.showView(nViewId);
		}
	}

	public void close()
	{
		if (null != flipper)
		{
			mnShowView = -1;
			flipper.close();
		}
	}

	public int getShowViewId()
	{
		return mnShowView;
	}
}
