package org.iii.aliceinwonderland;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import sdk.ideas.common.OnCallbackResult;
import sdk.ideas.common.ResponseCode;
import sdk.ideas.ctrl.bluetooth.BluetoothHandler;

public class BlueToothHandler
{
	public final static int			BOND_BONDED			= 1;
	public final static int			BOND_NONE			= 2;
	public final static int			CANCEL_BY_USER		= 3;

	private final String			BT_NAME				= "HC-05";
	private static BluetoothHandler	mBluetoothHandler	= null;
	private boolean					mbBTEnable			= false;
	private Handler					theHandler			= null;
	private Context					theContext			= null;

	public BlueToothHandler(Context context)
	{
		theContext = context;
	}

	@Override
	protected void finalize() throws Throwable
	{
		mBluetoothHandler = null;
		super.finalize();
	}

	public void init(Handler handler)
	{
		mbBTEnable = false;
		theHandler = handler;
		mBluetoothHandler = new BluetoothHandler(theContext);
		mBluetoothHandler.startListenAction();
		mBluetoothHandler.setOnCallbackResultListener(BtCallback);
		mBluetoothHandler.setBluetooth(true);
	}

	public boolean getBtState()
	{
		return mbBTEnable;
	}

	public void send(final String strData)
	{
		if (mbBTEnable && null != mBluetoothHandler)
		{
			mBluetoothHandler.sendMessage(strData);
			Logs.showTrace("BT send: " + strData);
		}
	}

	public void start()
	{
		if (null != mBluetoothHandler)
		{
			mBluetoothHandler.connectDeviceByName(BT_NAME);
		}
	}

	public void stop()
	{
		if (mbBTEnable && null != mBluetoothHandler)
		{
			mBluetoothHandler.closeBluetoothLink();
		}
	}

	public void release()
	{
		if (null != mBluetoothHandler)
		{
			mBluetoothHandler.stopListenAction();
			//mBluetoothHandler.setBluetooth(false);
			mBluetoothHandler = null;
			Logs.showTrace("Release BT");
		}
	}

	public static void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (null != mBluetoothHandler)
		{
			mBluetoothHandler.onActivityResult(requestCode, resultCode, data);
		}
	}

	private OnCallbackResult BtCallback = new OnCallbackResult()
	{
		@Override
		public void onCallbackResult(final int result, final int what, final int from,
				final HashMap<String, String> message)
		{
			Logs.showTrace("BT Callback: result=" + String.valueOf(result) + " what=" + String.valueOf(what) + " from="
					+ String.valueOf(from) + " message=" + message.toString());

			if (ResponseCode.ERR_SUCCESS == result)
			{
				switch(from)
				{
				case ResponseCode.BLUETOOTH_IS_ON:
					start();
					break;
				case ResponseCode.METHOD_OPEN_BLUETOOTH_CONNECTED_LINK:
					mbBTEnable = true;
					Common.postMessage(theHandler, MSG.CALLBACK_BT, BOND_BONDED, 0, null);
					break;
				}
			}
			else if (ResponseCode.ERR_BLUETOOTH_CANCELLED_BY_USER == result)
			{
				Common.postMessage(theHandler, MSG.CALLBACK_BT, CANCEL_BY_USER, 0, null);
			}
			else if (ResponseCode.ERR_BLUETOOTH_DEVICE_BOND_FAIL == result)
			{
				Logs.showTrace("配對失敗");
				mbBTEnable = false;
				Common.postMessage(theHandler, MSG.CALLBACK_BT, BOND_NONE, 0, null);
			}
			else if (ResponseCode.ERR_IO_EXCEPTION == result
					&& from == ResponseCode.METHOD_OPEN_BLUETOOTH_CONNECTED_LINK)
			{
				release();
				init(theHandler);
			}
			else
			{
				mbBTEnable = false;
				Common.postMessage(theHandler, MSG.CALLBACK_BT, BOND_NONE, 0, null);
			}
		}
	};

}
