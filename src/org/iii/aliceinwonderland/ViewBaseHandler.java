package org.iii.aliceinwonderland;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public abstract class ViewBaseHandler
{
	public Activity			theActivity	= null;
	public Handler			theHandler	= null;
	private LayoutInflater	inflater	= null;

	public ViewBaseHandler(Activity activity, Handler handler)
	{
		theActivity = activity;
		theHandler = handler;
		if (null != theActivity)
		{
			inflater = (LayoutInflater) theActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
	}

	public void sendMsg(int nWhat, int nArg1, int nArg2, Object obj)
	{
		if (null != theHandler)
		{
			Message msg = new Message();
			msg.what = nWhat;
			msg.arg1 = nArg1;
			msg.arg2 = nArg2;
			msg.obj = obj;
			theHandler.sendMessage(msg);
		}
	}

	public void postMsg(int nWhat, int nArg1, int nArg2, Object obj)
	{
		if (null != theHandler)
		{
			Thread t = new Thread(new postMsgRunnable(nWhat, nArg1, nArg2, obj));
			t.start();
		}
	}

	class postMsgRunnable implements Runnable
	{
		private Message	message	= null;

		@Override
		public void run()
		{
			if (null == message)
				return;
			theHandler.sendMessage(message);
		}

		public postMsgRunnable(int nWhat, int nArg1, int nArg2, Object obj)
		{
			message = new Message();
			message.what = nWhat;
			message.arg1 = nArg1;
			message.arg2 = nArg2;
			message.obj = obj;
		}
	}

	public void hideKeyboard()
	{
		if (null == theActivity)
			return;
		View view = theActivity.getCurrentFocus();
		if (view != null)
		{
			InputMethodManager inputManager = (InputMethodManager) theActivity
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	protected View createView(final int nResourceId)
	{
		if (null == inflater)
			return null;
		return inflater.inflate(nResourceId, null);
	}
}
