package org.iii.aliceinwonderland;

import android.app.Application;

public class MainApplication extends Application
{

	public MainApplication()
	{
		Global.theApplication = this;
	}

	public void Terminate()
	{
		Logs.showTrace("Alice in Wondland Terminate!!");
		System.exit(0);
	}
}
