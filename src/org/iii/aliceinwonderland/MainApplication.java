package org.iii.aliceinwonderland;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

public class MainApplication extends Application
{
	private BlueToothHandler bluetooth = null;

	public MainApplication()
	{
		Global.theApplication = this;
	}

	@Override
	protected void finalize() throws Throwable
	{
		btRelease();
		super.finalize();
		Terminate();
	}

	public void initBlueTooth(Context context, Handler handler)
	{
		bluetooth = new BlueToothHandler(context);
		bluetooth.init(handler);
	}

	public void btSend(final String strData)
	{
		if (null != bluetooth)
		{
			bluetooth.send(strData);
		}
	}

	public void btRelease()
	{
		if (null != bluetooth)
		{
			bluetooth.stop();
			bluetooth.release();
			bluetooth = null;
		}
	}

	public void Terminate()
	{
		Logs.showTrace("Alice in Wondland Terminate!!");
		System.exit(0);
	}
}
