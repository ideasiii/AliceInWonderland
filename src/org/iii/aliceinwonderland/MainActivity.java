package org.iii.aliceinwonderland;

import java.util.HashMap;
import java.util.Locale;

import com.facebook.appevents.AppEventsLogger;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import sdk.ideas.common.OnCallbackResult;
import sdk.ideas.common.ResponseCode;
import sdk.ideas.ctrl.bluetooth.BluetoothHandler;

public class MainActivity extends Activity
{

	private final int			LAYOUT_WELCOME					= 0;
	private final int			LAYOUT_HOME						= 1;
	private final int			LAYOUT_STORY					= 2;
	private final int			LAYOUT_MAIN						= 3;
	private final int			LAYOUT_LOGIN					= 4;

	private final int			MSG_SHOW_MAIN					= 10;
	private final int			MSG_SHOW_HOME					= 20;
	private final int			MSG_SHOW_CONTENT_SESSION1_END	= 30;
	private final int			MSG_SHOW_CONTENT_SESSION2_END	= 31;
	private final int			MSG_SHOW_CONTENT_SESSION3_END	= 32;
	private final int			MSG_SHOW_CONTENT_SESSION4_END	= 33;
	private final int			MSG_SHOW_SHARE_DIALOG			= 34;
	private final int			MSG_SHOW_LOGIN					= 35;
	private final int			MSG_SHOW_SHARE					= 36;
	private final int			REQUEST_CODE_CAMERA				= 666;
	private final String		BT_NAME							= "HC-05";										// "150737-R30A-IDS"; // //

	private ViewPagerHandler	pageHandler						= null;
	private FlipperHandler		flipperHandler					= null;
	private View				viewSession1					= null;
	private View				viewSession2					= null;
	private View				viewSession3					= null;
	private View				viewSession4					= null;
	private ImageView			imgInitLogo;
	private int[]				imgRes							= { R.drawable.init_logo, R.drawable.undobox };
	private View				viewKeyInput					= null;
	private EditText			edKey1							= null;
	private EditText			edKey2							= null;
	private EditText			edKey3							= null;
	private FacebookHandler		facebook						= null;
	private Long				session1_s						= 0L;
	private Long				session1_e						= 0L;
	private Long				session2_s						= 0L;
	private Long				session2_e						= 0L;
	private Long				session3_s						= 0L;
	private Long				session3_e						= 0L;
	private Long				session4_s						= 0L;
	private Long				session4_e						= 0L;
	private Share				share							= null;
	private BluetoothHandler	mBluetoothHandler				= null;
	private boolean				mbBTEnable						= false;
	private String				mstrTimeofPlay					= "00:00:00";
	private String				mstrPicturePath					= null;
	private Dialog				dialogLoading					= null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.getActionBar().hide();
		Global.mainHandler = selfHandler;
		showLayout(LAYOUT_WELCOME);
		Logs.showTrace("Alice on Create");
	}

	@Override
	protected void onResume()
	{
		Logs.showTrace("onResume");
		super.onResume();
		AppEventsLogger.activateApp(this);
	}

	@Override
	protected void onPause()
	{
		Logs.showTrace("onPause");
		super.onPause();
		AppEventsLogger.deactivateApp(this);
	}

	@Override
	protected void onStop()
	{
		Logs.showTrace("onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		Logs.showTrace("onDestroy");
		releaseBT();
		super.onDestroy();
		Logs.showTrace("System Exit");
		android.os.Process.killProcess(android.os.Process.myPid());
		// System.exit(0);

	}

	private void initBluetooth()
	{
		Logs.showTrace("initBluetooth........");
		if (null != dialogLoading)
		{
			dialogLoading.dismiss();
			dialogLoading = null;
		}
		dialogLoading = DialogHandler.showLoading(this);
		mBluetoothHandler = new BluetoothHandler(this);
		mBluetoothHandler.startListenAction();

		mBluetoothHandler.setOnCallbackResultListener(new OnCallbackResult()
		{

			@Override
			public void onCallbackResult(final int result, final int what, final int from,
					final HashMap<String, String> message)
			{

				Logs.showTrace("bluetooth lisetener Result: " + String.valueOf(result) + " What: "
						+ String.valueOf(what) + " From: " + String.valueOf(from) + " Message: " + message);

				if (from == ResponseCode.METHOD_BLUETOOTH_DISCOVERING_NEW_DEVICE)
				{

					if (null != message.get("deviceName") && message.get("deviceName").equals(BT_NAME))
					{
						mBluetoothHandler.stopDiscovery();
						Logs.showTrace("BT get " + BT_NAME);
					}
				}
				if (result == ResponseCode.ERR_SUCCESS && from == ResponseCode.METHOD_BOND_STATE_CHANGE_BLUETOOTH)
				{
					Logs.showTrace("METHOD_BOND_STATE_CHANGE_BLUETOOTH");
					if (null != message && null != message.get("state"))
					{
						if (message.get("state").equals("BOND_NONE"))
						{
							Logs.showTrace("配對失敗");
							mbBTEnable = false;
							if (null != dialogLoading)
							{
								dialogLoading.dismiss();
								dialogLoading = null;
							}
						}
						else if (message.get("state").equals("BOND_BONDED"))
						{
							Logs.showTrace("配對成功");
							mbBTEnable = true;
							if (null != dialogLoading)
							{
								dialogLoading.dismiss();
								dialogLoading = null;
							}
						}
					}
					else
					{
						Logs.showTrace("BT message invalid");
						if (null != dialogLoading)
						{
							dialogLoading.dismiss();
							dialogLoading = null;
						}
					}

				}
				if (result == ResponseCode.ERR_SUCCESS && from == ResponseCode.BLUETOOTH_IS_ON)
				{
					Logs.showTrace("BT start to request Discoverable");
					if (null != mBluetoothHandler)
					{
						mBluetoothHandler.requestBluetoothDiscoverable();
					}
					else
					{
						Logs.showError("requestBluetoothDiscoverable BT is null");
					}
				}
				if (result == ResponseCode.ERR_SUCCESS && from == ResponseCode.METHOD_DISCOVERABLE_BLUETOOTH)
				{
					Logs.showTrace("BT start to discover");
					mBluetoothHandler.startDiscovery();
				}
				if (result == ResponseCode.ERR_SUCCESS && from == ResponseCode.METHOD_BLUETOOTH_DISCOVER_FINISHED)
				{
					mBluetoothHandler.connectDeviceByName(BT_NAME);

				}
				if (result == -26)
				{
					if (null != dialogLoading)
					{
						dialogLoading.dismiss();
						dialogLoading = null;
					}
				}
			}
		});

		mBluetoothHandler.setBluetooth(true);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (REQUEST_CODE_CAMERA == requestCode)
		{
			Logs.showTrace("Camera Activity Finish and Call Result");
			if (resultCode == RESULT_OK)
			{
				Bundle extras = data.getExtras();
				mstrPicturePath = null;
				if (extras != null)
				{
					mstrPicturePath = extras.getString("picture");
					Logs.showTrace("Camera picture path: " + mstrPicturePath);
				}
				selfHandler.sendEmptyMessageDelayed(MSG_SHOW_SHARE, 2000);
			}
		}
		else
		{
			FacebookHandler.callbackManager.onActivityResult(requestCode, resultCode, data);
			if (null != mBluetoothHandler)
			{
				mBluetoothHandler.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	private void closeBox()
	{
		if (null != mBluetoothHandler && mbBTEnable)
		{
			mBluetoothHandler.sendData("a");
			mBluetoothHandler.sendData("c");
			mBluetoothHandler.sendData("e");
			mBluetoothHandler.sendData("g");
			mBluetoothHandler.sendData("i");
		}
	}

	private void releaseBT()
	{
		if (mbBTEnable && null != mBluetoothHandler)
		{
			mBluetoothHandler.closeBluetoothLink();
		}

		if (null != mBluetoothHandler)
		{
			mBluetoothHandler.stopListenAction();

			mBluetoothHandler.setBluetooth(false);
			mBluetoothHandler = null;
			Logs.showTrace("Release BT");
		}
	}

	private void showLayout(final int nLayout)
	{
		switch(nLayout)
		{
		case LAYOUT_WELCOME:
			setContentView(R.layout.welcome);
			logoShow();
			break;
		case LAYOUT_HOME:
			setContentView(R.layout.home);
			initHomeStartBtn();
			break;
		case LAYOUT_STORY:
			setContentView(R.layout.intro);
			initStoryGoBtn();
			closeBox();
			break;
		case LAYOUT_MAIN:
			setContentView(R.layout.activity_main);
			initMainHandler();
			break;
		case LAYOUT_LOGIN:
			showLogin();
			break;
		}
	}

	private void showLogin()
	{
		setContentView(R.layout.login);
		findViewById(R.id.textViewLoginFacebook).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (Utility.checkInternet(MainActivity.this))
				{
					showFacebookLogin();
				}
				else
				{
					DialogHandler.showNetworkError(MainActivity.this, false);
				}
			}
		});
		findViewById(R.id.textViewLoginSkip).setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				initBluetooth();
				showLayout(LAYOUT_HOME);
			}
		});
	}

	private void showFacebookLogin()
	{
		Logs.showTrace("Facebook Login Start");
		facebook = new FacebookHandler(this);
		facebook.init();
		facebook.setOnFacebookLoginResultListener(new FacebookHandler.OnFacebookLoginResult()
		{
			@Override
			public void onLoginResult(String strFBID, String strName, String strEmail, String strError)
			{
				initBluetooth();
				showLayout(LAYOUT_HOME);
				Logs.showTrace("Login Facebook: " + strFBID + " " + strName + " " + strEmail + " " + strError);
				Logs.showTrace("Facebook token: " + facebook.getToken());
			}
		});
		facebook.login();
	}

	private void initHomeStartBtn()
	{
		this.findViewById(R.id.buttonHomeStart).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showLayout(LAYOUT_STORY);
			}
		});
	}

	private void initStoryGoBtn()
	{
		this.findViewById(R.id.buttonStoryStart).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showLayout(LAYOUT_MAIN);
			}
		});
	}

	private void logoShow()
	{
		imgInitLogo = (ImageView) findViewById(R.id.imageViewInitLogo);
		fadeOutAndHideImage(imgInitLogo);
	}

	private void initMainHandler()
	{
		flipperHandler = new FlipperHandler(this, selfHandler);
		if (flipperHandler.init())
		{
			viewKeyInput = flipperHandler.getView(FlipperHandler.VIEW_ID_KEY_INPUT);
			edKey1 = (EditText) viewKeyInput.findViewById(R.id.editTextKey1);
			edKey2 = (EditText) viewKeyInput.findViewById(R.id.editTextKey2);
			edKey3 = (EditText) viewKeyInput.findViewById(R.id.editTextKey3);
			edKey1.addTextChangedListener(new TextWatcher()
			{

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after)
				{

				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count)
				{

				}

				@Override
				public void afterTextChanged(Editable s)
				{
					if (null != s && 0 < s.toString().trim().length())
						edKey2.requestFocus();
				}
			});

			edKey2.addTextChangedListener(new TextWatcher()
			{

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after)
				{

				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count)
				{

				}

				@Override
				public void afterTextChanged(Editable s)
				{
					if (null != s && 0 < s.toString().trim().length())
						edKey3.requestFocus();
				}
			});

			edKey3.addTextChangedListener(new TextWatcher()
			{

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after)
				{

				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count)
				{

				}

				@Override
				public void afterTextChanged(Editable s)
				{
					if (null != s && 0 < s.toString().trim().length())
					{
						InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
						imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
					}
				}
			});

		}

		pageHandler = new ViewPagerHandler(this, selfHandler);
		if (pageHandler.init())
		{
			pageHandler.showPage(ViewPagerHandler.PAGE_SESSION1);
			viewSession1 = pageHandler.getView(ViewPagerHandler.PAGE_SESSION1);
			fadeInAndShowContent(viewSession1.findViewById(R.id.scrollViewSession1Content),
					MSG_SHOW_CONTENT_SESSION1_END);
			session1_s = System.currentTimeMillis();
			Logs.showTrace("Game Session1 start:" + String.valueOf(session1_s));
		}

	}

	public Handler getSelfHandler()
	{
		return selfHandler;
	}

	public void setSelfHandler(Handler selfHandler)
	{
		this.selfHandler = selfHandler;
	}

	private void initSession1()
	{
		viewSession1.findViewById(R.id.buttonSession1Key).setVisibility(View.VISIBLE);
		viewSession1.findViewById(R.id.buttonSession1Key).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				TextView vTitle = (TextView) viewKeyInput.findViewById(R.id.textViewKeyInputTitle1);
				vTitle.setText(MainActivity.this.getString(R.string.session1_title1));
				vTitle = (TextView) viewKeyInput.findViewById(R.id.textViewKeyInputTitle2);
				vTitle.setText(MainActivity.this.getString(R.string.session1_title2));
				viewKeyInput.findViewById(R.id.imageButtonKeyInput).setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						String strKey = "";
						strKey = edKey1.getText().toString() + edKey2.getText().toString()
								+ edKey3.getText().toString();
						Logs.showTrace("Input Key:" + strKey);
						if (null == strKey || 0 >= strKey.length())
						{
							flipperHandler.showView(FlipperHandler.VIEW_ID_FAIL);
							flipperHandler.getView(FlipperHandler.VIEW_ID_FAIL).findViewById(R.id.buttonFail)
									.setOnClickListener(new OnClickListener()
									{
										@Override
										public void onClick(View v)
										{
											flipperHandler.showView(FlipperHandler.VIEW_ID_KEY_INPUT);
										}
									});
						}
						else
						{
							int nKey = Integer.valueOf(strKey);
							if (693 == nKey)
							{
								session1_e = System.currentTimeMillis();
								if (mbBTEnable)
								{
									mBluetoothHandler.sendData("b");
									Logs.showTrace("Send b to BT");
								}
								Logs.showTrace("Game Session1 end:" + String.valueOf(session1_e));
								flipperHandler.showView(FlipperHandler.VIEW_ID_SUCCESS);
								flipperHandler.getView(FlipperHandler.VIEW_ID_SUCCESS).findViewById(R.id.buttonSuccess)
										.setOnClickListener(new OnClickListener()
										{
											@Override
											public void onClick(View v)
											{
												flipperHandler.close();
												pageHandler.showPage(ViewPagerHandler.PAGE_SESSION2);
												viewSession2 = pageHandler.getView(ViewPagerHandler.PAGE_SESSION2);
												fadeInAndShowContent(
														viewSession2.findViewById(R.id.scrollViewSession2Content),
														MSG_SHOW_CONTENT_SESSION2_END);
												session2_s = System.currentTimeMillis();
												Logs.showTrace("Game Session2 start:" + String.valueOf(session2_s));
											}
										});
							}
							else
							{
								flipperHandler.showView(FlipperHandler.VIEW_ID_FAIL);
								flipperHandler.getView(FlipperHandler.VIEW_ID_FAIL).findViewById(R.id.buttonFail)
										.setOnClickListener(new OnClickListener()
										{
											@Override
											public void onClick(View v)
											{
												flipperHandler.showView(FlipperHandler.VIEW_ID_KEY_INPUT);
											}
										});
							}
						}

					}

				});
				flipperHandler.showView(FlipperHandler.VIEW_ID_KEY_INPUT);
			}
		});
	}

	private void initSession2()
	{
		viewSession2.findViewById(R.id.buttonSession2Key).setVisibility(View.VISIBLE);
		viewSession2.findViewById(R.id.buttonSession2Key).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				TextView vTitle = (TextView) viewKeyInput.findViewById(R.id.textViewKeyInputTitle1);
				vTitle.setText(MainActivity.this.getString(R.string.session2_title1));
				vTitle = (TextView) viewKeyInput.findViewById(R.id.textViewKeyInputTitle2);
				vTitle.setText(MainActivity.this.getString(R.string.session2_title2));
				viewKeyInput.findViewById(R.id.imageButtonKeyInput).setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						String strKey = "";
						EditText edKey = (EditText) viewKeyInput.findViewById(R.id.editTextKey1);
						strKey = edKey.getText().toString();
						edKey = (EditText) viewKeyInput.findViewById(R.id.editTextKey2);
						strKey += edKey.getText().toString();
						edKey = (EditText) viewKeyInput.findViewById(R.id.editTextKey3);
						strKey += edKey.getText().toString();
						Logs.showTrace("Input Key:" + strKey);
						if (null == strKey || 0 >= strKey.length())
						{
							flipperHandler.showView(FlipperHandler.VIEW_ID_FAIL);
							flipperHandler.getView(FlipperHandler.VIEW_ID_FAIL).findViewById(R.id.buttonFail)
									.setOnClickListener(new OnClickListener()
									{
										@Override
										public void onClick(View v)
										{
											flipperHandler.showView(FlipperHandler.VIEW_ID_KEY_INPUT);
										}
									});
						}
						else
						{
							int nKey = Integer.valueOf(strKey);
							if (786 == nKey)
							{
								session2_e = System.currentTimeMillis();
								if (mbBTEnable)
								{
									mBluetoothHandler.sendData("d");
									Logs.showTrace("Send d to BT");
								}
								Logs.showTrace("Game Session2 end:" + String.valueOf(session2_e));
								flipperHandler.showView(FlipperHandler.VIEW_ID_SUCCESS);
								flipperHandler.getView(FlipperHandler.VIEW_ID_SUCCESS).findViewById(R.id.buttonSuccess)
										.setOnClickListener(new OnClickListener()
										{
											@Override
											public void onClick(View v)
											{
												flipperHandler.close();
												pageHandler.showPage(ViewPagerHandler.PAGE_SESSION3);
												viewSession3 = pageHandler.getView(ViewPagerHandler.PAGE_SESSION3);
												fadeInAndShowContent(
														viewSession3.findViewById(R.id.scrollViewSession3Content),
														MSG_SHOW_CONTENT_SESSION3_END);
												session3_s = System.currentTimeMillis();
												Logs.showTrace("Game Session3 start:" + String.valueOf(session3_s));
											}
										});
							}
							else
							{
								flipperHandler.showView(FlipperHandler.VIEW_ID_FAIL);
								flipperHandler.getView(FlipperHandler.VIEW_ID_FAIL).findViewById(R.id.buttonFail)
										.setOnClickListener(new OnClickListener()
										{
											@Override
											public void onClick(View v)
											{
												flipperHandler.showView(FlipperHandler.VIEW_ID_KEY_INPUT);
											}
										});
							}
						}
					}

				});
				clearKeyInput();
				flipperHandler.showView(FlipperHandler.VIEW_ID_KEY_INPUT);
			}
		});
	}

	private void initSession3()
	{
		viewSession3.findViewById(R.id.buttonSession3Key).setVisibility(View.VISIBLE);
		viewSession3.findViewById(R.id.buttonSession3Key).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				TextView vTitle = (TextView) viewKeyInput.findViewById(R.id.textViewKeyInputTitle1);
				vTitle.setText(MainActivity.this.getString(R.string.session3_title1));
				vTitle = (TextView) viewKeyInput.findViewById(R.id.textViewKeyInputTitle2);
				vTitle.setText(MainActivity.this.getString(R.string.session3_title2));
				viewKeyInput.findViewById(R.id.imageButtonKeyInput).setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						String strKey = "";
						EditText edKey = (EditText) viewKeyInput.findViewById(R.id.editTextKey1);
						strKey = edKey.getText().toString();
						edKey = (EditText) viewKeyInput.findViewById(R.id.editTextKey2);
						strKey += edKey.getText().toString();
						edKey = (EditText) viewKeyInput.findViewById(R.id.editTextKey3);
						strKey += edKey.getText().toString();
						Logs.showTrace("Input Key:" + strKey);
						if (null == strKey || 0 >= strKey.length())
						{
							flipperHandler.showView(FlipperHandler.VIEW_ID_FAIL);
							flipperHandler.getView(FlipperHandler.VIEW_ID_FAIL).findViewById(R.id.buttonFail)
									.setOnClickListener(new OnClickListener()
									{
										@Override
										public void onClick(View v)
										{
											flipperHandler.showView(FlipperHandler.VIEW_ID_KEY_INPUT);
										}
									});
						}
						else
						{
							int nKey = Integer.valueOf(strKey);
							if (321 == nKey)
							{
								session3_e = System.currentTimeMillis();
								if (mbBTEnable)
								{
									mBluetoothHandler.sendData("f");
									Logs.showTrace("Send f to BT");
								}
								Logs.showTrace("Game Session3 end:" + String.valueOf(session3_e));
								flipperHandler.showView(FlipperHandler.VIEW_ID_SUCCESS);
								flipperHandler.getView(FlipperHandler.VIEW_ID_SUCCESS).findViewById(R.id.buttonSuccess)
										.setOnClickListener(new OnClickListener()
										{
											@Override
											public void onClick(View v)
											{
												flipperHandler.close();
												pageHandler.showPage(ViewPagerHandler.PAGE_SESSION4);
												viewSession4 = pageHandler.getView(ViewPagerHandler.PAGE_SESSION4);
												fadeInAndShowContent(
														viewSession4.findViewById(R.id.scrollViewSession4Content),
														MSG_SHOW_CONTENT_SESSION4_END);
												session4_s = System.currentTimeMillis();
												Logs.showTrace("Game Session4 start:" + String.valueOf(session4_s));
											}
										});
							}
							else
							{
								flipperHandler.showView(FlipperHandler.VIEW_ID_FAIL);
								flipperHandler.getView(FlipperHandler.VIEW_ID_FAIL).findViewById(R.id.buttonFail)
										.setOnClickListener(new OnClickListener()
										{
											@Override
											public void onClick(View v)
											{
												flipperHandler.showView(FlipperHandler.VIEW_ID_KEY_INPUT);
											}
										});
							}
						}

					}

				});
				clearKeyInput();
				flipperHandler.showView(FlipperHandler.VIEW_ID_KEY_INPUT);
			}
		});
	}

	private void initSession4()
	{
		viewSession4.findViewById(R.id.buttonSession4Key).setVisibility(View.VISIBLE);
		viewSession4.findViewById(R.id.buttonSession4Key).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				TextView vTitle = (TextView) viewKeyInput.findViewById(R.id.textViewKeyInputTitle1);
				vTitle.setText(MainActivity.this.getString(R.string.session4_title1));
				vTitle = (TextView) viewKeyInput.findViewById(R.id.textViewKeyInputTitle2);
				vTitle.setText(MainActivity.this.getString(R.string.session4_title2));
				viewKeyInput.findViewById(R.id.imageButtonKeyInput).setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						String strKey = "";
						EditText edKey = (EditText) viewKeyInput.findViewById(R.id.editTextKey1);
						strKey = edKey.getText().toString();
						edKey = (EditText) viewKeyInput.findViewById(R.id.editTextKey2);
						strKey += edKey.getText().toString();
						edKey = (EditText) viewKeyInput.findViewById(R.id.editTextKey3);
						strKey += edKey.getText().toString();
						Logs.showTrace("Input Key:" + strKey);
						if (null == strKey || 0 >= strKey.length())
						{
							flipperHandler.showView(FlipperHandler.VIEW_ID_FAIL);
							flipperHandler.getView(FlipperHandler.VIEW_ID_FAIL).findViewById(R.id.buttonFail)
									.setOnClickListener(new OnClickListener()
									{
										@Override
										public void onClick(View v)
										{
											flipperHandler.showView(FlipperHandler.VIEW_ID_KEY_INPUT);
										}
									});
						}
						else
						{
							int nKey = Integer.valueOf(strKey);
							if (245 == nKey)
							{
								session4_e = System.currentTimeMillis();
								if (mbBTEnable)
								{
									mBluetoothHandler.sendData("h");
									Logs.showTrace("Send h to BT");
								}
								Logs.showTrace("Game Session4 end:" + String.valueOf(session4_e));
								showEnding();
							}
							else
							{
								flipperHandler.showView(FlipperHandler.VIEW_ID_FAIL);
								flipperHandler.getView(FlipperHandler.VIEW_ID_FAIL).findViewById(R.id.buttonFail)
										.setOnClickListener(new OnClickListener()
										{
											@Override
											public void onClick(View v)
											{
												flipperHandler.showView(FlipperHandler.VIEW_ID_KEY_INPUT);
											}
										});
							}
						}

					}

				});
				clearKeyInput();
				flipperHandler.showView(FlipperHandler.VIEW_ID_KEY_INPUT);
			}
		});
	}

	private void showEnding()
	{
		Long ltotalTime = session4_e - session1_s;

		releaseBT();

		String strTime = formatTime(ltotalTime);

		TextView tvTime = (TextView) pageHandler.getView(ViewPagerHandler.PAGE_ENDING)
				.findViewById(R.id.textViewEndingTime);
		tvTime.setText(strTime);
		pageHandler.getView(ViewPagerHandler.PAGE_ENDING).findViewById(R.id.imageViewEndingShare)
				.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						showCamera();
					}
				});

		flipperHandler.close();
		pageHandler.showPage(ViewPagerHandler.PAGE_ENDING);
		selfHandler.sendEmptyMessageDelayed(MSG_SHOW_SHARE_DIALOG, 3000);

	}

	private String formatTime(Long lMillsecond)
	{
		// 計算目前已過分鐘數
		int minius = (int) ((lMillsecond / 1000) / 60);
		// 計算目前已過秒數
		int seconds = (int) ((lMillsecond / 1000) % 60);
		// 計算目前已過小時
		int hourse = minius / 60;
		mstrTimeofPlay = String.format(Locale.TAIWAN, "%02d:%02d:%02d", hourse, minius, seconds);
		return mstrTimeofPlay;
	}

	private void clearKeyInput()
	{
		EditText edKey = (EditText) viewKeyInput.findViewById(R.id.editTextKey1);
		edKey.setText("");
		edKey = (EditText) viewKeyInput.findViewById(R.id.editTextKey2);
		edKey.setText("");
		edKey = (EditText) viewKeyInput.findViewById(R.id.editTextKey3);
		edKey.setText("");
	}

	private void showShareDialog()
	{
		View view = pageHandler.getView(ViewPagerHandler.PAGE_ENDING);
		if (null != view)
		{
			View vDialog = view.findViewById(R.id.textViewShareDialog);
			if (null != vDialog)
			{
				vDialog.setVisibility(View.VISIBLE);
			}
		}
	}

	private void runShare()
	{
		share = new Share(MainActivity.this);
		SparseArray<String> listImagePath = null;
		if (null != mstrPicturePath)
		{
			listImagePath = new SparseArray<String>();
			listImagePath.append(0, mstrPicturePath);
		}
		String strMsg = "愛麗絲夢遊仙境 – 地洞冒險(上) 遊戲完成時間：" + mstrTimeofPlay;
		share.shareAll("愛麗絲夢遊仙境 – 地洞冒險(上)", "愛麗絲夢遊仙境 – 地洞冒險(上)", strMsg, listImagePath);

		if (null != listImagePath)
		{
			listImagePath.clear();
			listImagePath = null;
		}
		selfHandler.sendEmptyMessageDelayed(MSG_SHOW_HOME, 1000);
	}

	private Handler selfHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
			case MSG_SHOW_HOME:
				showLayout(LAYOUT_HOME);
				break;
			case MSG_SHOW_MAIN:
				showLayout(LAYOUT_MAIN);
				break;
			case MSG_SHOW_CONTENT_SESSION1_END:
				initSession1();
				break;
			case MSG_SHOW_CONTENT_SESSION2_END:
				initSession2();
				break;
			case MSG_SHOW_CONTENT_SESSION3_END:
				initSession3();
				break;
			case MSG_SHOW_CONTENT_SESSION4_END:
				initSession4();
				break;
			case MSG_SHOW_SHARE_DIALOG:
				showShareDialog();
				break;
			case MSG_SHOW_LOGIN:
				showLayout(LAYOUT_LOGIN);
				break;
			case MSG.FB_LOGIN:
				Logs.showTrace("Facebook Relogin");
				facebook.login();
				break;
			case MSG_SHOW_SHARE:
				runShare();
				break;
			}
		}

	};

	private void fadeOutAndHideImage(final ImageView img)
	{
		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator());
		fadeOut.setDuration(1500);

		fadeOut.setAnimationListener(new AnimationListener()
		{
			public void onAnimationEnd(Animation animation)
			{
				img.setImageResource(imgRes[1]);
				fadeInAndShowImage(img);
			}

			public void onAnimationRepeat(Animation animation)
			{
			}

			public void onAnimationStart(Animation animation)
			{
			}
		});

		img.startAnimation(fadeOut);
	}

	private void fadeInAndShowImage(final ImageView img)
	{
		Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator(new AccelerateInterpolator());
		fadeIn.setDuration(1000);

		fadeIn.setAnimationListener(new AnimationListener()
		{
			public void onAnimationEnd(Animation animation)
			{
				selfHandler.sendEmptyMessageDelayed(MSG_SHOW_LOGIN, 2000);
			}

			public void onAnimationRepeat(Animation animation)
			{
			}

			public void onAnimationStart(Animation animation)
			{
			}
		});

		img.startAnimation(fadeIn);
	}

	private void fadeInAndShowContent(final View view, final int nCallbackId)
	{
		Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator(new AccelerateInterpolator());
		fadeIn.setDuration(1000);

		fadeIn.setAnimationListener(new AnimationListener()
		{
			public void onAnimationEnd(Animation animation)
			{
				selfHandler.sendEmptyMessageDelayed(nCallbackId, 100);
			}

			public void onAnimationRepeat(Animation animation)
			{
			}

			public void onAnimationStart(Animation animation)
			{
			}
		});

		view.startAnimation(fadeIn);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (KeyEvent.KEYCODE_BACK == keyCode)
		{
			if (null != flipperHandler)
			{
				if (-1 != flipperHandler.getShowViewId())
				{
					flipperHandler.close();
					return true;
				}
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	private void showCamera()
	{
		Intent openCameraIntent = new Intent(MainActivity.this, CameraActivity.class);
		openCameraIntent.putExtra("time", "00:00:00");
		startActivityForResult(openCameraIntent, REQUEST_CODE_CAMERA);
	}

}
