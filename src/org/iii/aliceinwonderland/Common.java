package org.iii.aliceinwonderland;

import android.os.Handler;
import android.os.Message;

public abstract class Common
{
	public static void postMessage(Handler handler, int nWhat, int nArg0, int nArg1, Object obj)
	{
		if (null == handler)
			return;
		Thread t = new Thread(new postMsgRunnable(handler, nWhat, nArg0, nArg1, obj));
		t.start();
	}

	private static class postMsgRunnable implements Runnable
	{
		private Message	message		= null;
		private Handler	theHandler	= null;

		@Override
		public void run()
		{
			if (null == message || null == theHandler)
				return;
			theHandler.sendMessage(message);
		}

		public postMsgRunnable(Handler handler, int nWhat, int nArg1, int nArg2, Object obj)
		{
			theHandler = handler;
			message = new Message();
			message.what = nWhat;
			message.arg1 = nArg1;
			message.arg2 = nArg2;
			message.obj = obj;
		}
	}

}
