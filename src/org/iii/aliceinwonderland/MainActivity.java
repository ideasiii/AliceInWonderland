package org.iii.aliceinwonderland;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

import com.facebook.appevents.AppEventsLogger;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
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
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends Activity
{

	private final int			LAYOUT_WELCOME					= 0;
	private final int			LAYOUT_HOME						= 1;
	private final int			LAYOUT_STORY					= 2;
	private final int			LAYOUT_MAIN						= 3;
	private final int			LAYOUT_LOGIN					= 4;
	private final int			LAYOUT_BT						= 5;

	private final int			MSG_SHOW_MAIN					= 10;
	private final int			MSG_SHOW_HOME					= 20;
	private final int			MSG_SHOW_CONTENT_SESSION1_END	= 30;
	private final int			MSG_SHOW_CONTENT_SESSION2_END	= 31;
	private final int			MSG_SHOW_CONTENT_SESSION3_END	= 32;
	private final int			MSG_SHOW_CONTENT_SESSION4_END	= 33;
	private final int			MSG_SHOW_SHARE_DIALOG			= 34;
	private final int			MSG_SHOW_LOGIN					= 35;
	private final int			MSG_SHOW_SHARE					= 36;
	private final int			MSG_SHOW_GAMEOVER				= 37;

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
	private String				mstrTimeofPlay					= "00:00:00";
	private static String		mstrPicturePath					= null;
	private Dialog				dialogLoading					= null;
	private MediaHandler		media							= null;
	private static boolean		mbMute							= false;
	private static Bitmap		bmpPasswordBG					= null;
	private static Bitmap		bmpSuccessBG					= null;
	private static Bitmap		bmpFailBG						= null;

	private TrackerHandler		tracker							= null;
	private int					answer_error_count				= 0;

	private class InputLayout
	{
		public int	mnBackground	= 0;
		public int	mnInput			= 0;
		public int	mnSuccess		= 0;
		public int	mnFail			= 0;
		public int	mnSessionNum	= 0;
		public int	mnSessionTitle	= 0;

		public InputLayout(final int nBackground, final int nInput, final int nSuccess, final int nFail,
				final int nSessionNum, final int nSessionTitle)
		{
			mnBackground = nBackground;
			mnInput = nInput;
			mnSuccess = nSuccess;
			mnFail = nFail;
			mnSessionNum = nSessionNum;
			mnSessionTitle = nSessionTitle;
		}
	}

	private SparseArray<InputLayout> listInputLayout = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		tracker = new TrackerHandler(this);
		tracker.init();

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.getActionBar().hide();
		Global.mainHandler = selfHandler;
		showLayout(LAYOUT_WELCOME);
		Logs.showTrace("Alice on Create");
		media = new MediaHandler(this);
		initInputLayout();
	}

	@Override
	protected void onResume()
	{
		Logs.showTrace("onResume");
		if (!mbMute)
			media.start();
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
		if (!mbMute)
			media.pause();
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		Logs.showTrace("onDestroy");

		tracker.stopTracker();
		Global.theApplication.btRelease();
		media.releasePlayer();
		Global.theApplication.Terminate();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Logs.showTrace("onActivityResult: requestCode=" + String.valueOf(requestCode) + " resultCode="
				+ String.valueOf(resultCode));
		super.onActivityResult(requestCode, resultCode, data);
		if (MSG.REQUEST_CODE_CAMERA == requestCode)
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

				if (null != dialogLoading)
				{
					dialogLoading.dismiss();
					dialogLoading = null;
				}
				dialogLoading = DialogHandler.showLoading(this);
				selfHandler.sendEmptyMessageDelayed(MSG_SHOW_SHARE, 5000);
			}
			else
			{
				showGameover();
			}

		}
		else if (MSG.REQUEST_CODE_SHARE == requestCode)
		{
			selfHandler.sendEmptyMessageDelayed(MSG_SHOW_GAMEOVER, 500);
		}
		else
		{
			FacebookHandler.callbackManager.onActivityResult(requestCode, resultCode, data);
			BlueToothHandler.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void initInputLayout()
	{
		listInputLayout = new SparseArray<InputLayout>();
		listInputLayout.append(listInputLayout.size(), new InputLayout(R.drawable.password1_1, R.drawable.input1_1,
				R.drawable.success1_1, R.drawable.fail1_1, R.string.session1_title0, R.string.session1_title2));
		listInputLayout.append(listInputLayout.size(), new InputLayout(R.drawable.password1_2, R.drawable.input1_2,
				R.drawable.success1_2, R.drawable.fail1_2, R.string.session2_title0, R.string.session2_title2));
		listInputLayout.append(listInputLayout.size(), new InputLayout(R.drawable.password1_3, R.drawable.input1_3,
				R.drawable.success1_3, R.drawable.fail1_3, R.string.session3_title0, R.string.session3_title2));
		listInputLayout.append(listInputLayout.size(), new InputLayout(R.drawable.password1_4, R.drawable.input1_4,
				R.drawable.success1_4, R.drawable.fail1_4, R.string.session4_title0, R.string.session4_title2));

	}

	private void closeBox()
	{
		Global.theApplication.btSend("acegi");
	}

	private void showLayout(final int nLayout)
	{
		HashMap<String, String> trackerMessage = new HashMap<String, String>();
		switch(nLayout)
		{
		case LAYOUT_WELCOME:
			trackerMessage.put("PAGE", "LAYOUT_WELCOME");
			tracker.send(trackerMessage);
			setContentView(R.layout.welcome);
			logoShow();
			break;
		case LAYOUT_HOME:
			if (null != Global.bt_name)
			{
				Logs.showTrace("initBluetooth........");
				dialogLoading = DialogHandler.showLoading(this);
				Global.theApplication.initBlueTooth(this, selfHandler);
			}
			setContentView(R.layout.home);
			initHomeStartBtn();
			break;
		case LAYOUT_STORY:
			trackerMessage.put("PAGE", "LAYOUT_STORY");
			tracker.send(trackerMessage);
			setContentView(R.layout.intro);
			initStoryGoBtn();
			closeBox();
			break;
		case LAYOUT_MAIN:
			trackerMessage.put("PAGE", "LAYOUT_MAIN");
			tracker.send(trackerMessage);
			setContentView(R.layout.activity_main);
			initMainHandler();
			break;
		case LAYOUT_LOGIN:
			showLogin();
			break;
		case LAYOUT_BT:
			showBtSelect();
			break;
		}
	}

	private void showBtSelect()
	{
		setContentView(R.layout.btconfig);

		((RadioGroup) findViewById(R.id.rgroup)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
		{

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				switch(checkedId)
				{
				case R.id.radioButtonBT1:
					Global.bt_name = "Toy01";
					break;
				case R.id.radioButtonBT2:
					Global.bt_name = "Toy02";
					break;
				case R.id.radioButtonBT3:
					Global.bt_name = "Toy03";
					break;
				case R.id.radioButtonBT4:
					Global.bt_name = "Toy04";
					break;
				case R.id.radioButtonBT5:
					Global.bt_name = "Toy05";
					break;
				}

				Logs.showTrace("BT Name set to: " + Global.bt_name);
			}
		});

		findViewById(R.id.buttonBTStart).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showLayout(LAYOUT_HOME);
			}
		});
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
			HashMap<String, String> message = new HashMap<String, String>();

			@Override
			public void onClick(View v)
			{
				message.put("PAGE", "LAYOUT_SKIP_LOGIN");
				tracker.send(message);
				// showLayout(LAYOUT_HOME);
				showLayout(LAYOUT_BT);
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
				HashMap<String, String> message = new HashMap<String, String>();
				message.put("PAGE", "LAYOUT_FACEBOOK_LOGIN");
				message.put("FACEBOOK_ID", strFBID);
				message.put("FACEBOOK_NAME", strName);
				message.put("FACEBOOK_EMAIL", strEmail);
				message.put("FACEBOOK_ERROR", strError);
				tracker.send(message);
				// showLayout(LAYOUT_HOME);
				showLayout(LAYOUT_BT);
				Logs.showTrace("Login Facebook: " + strFBID + " " + strName + " " + strEmail + " " + strError);
				Logs.showTrace("Facebook token: " + facebook.getToken());
			}
		});
		facebook.login();
	}

	private void initHomeStartBtn()
	{
		if (!mbMute)
			media.play();
		this.findViewById(R.id.buttonHomeStart).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showLayout(LAYOUT_STORY);
			}
		});

		this.findViewById(R.id.imageViewHomeMute).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				switchMute();
			}
		});
	}

	private void switchMute()
	{
		HashMap<String, String> message = new HashMap<String, String>();
		if (mbMute)
		{
			message.put("PAGE", "LAYOUT_HOME");
			message.put("BGM", "play");
			tracker.send(message);

			mbMute = false;
			media.play();
		}
		else
		{
			message.put("PAGE", "LAYOUT_HOME");
			message.put("BGM", "mute");
			tracker.send(message);

			mbMute = true;
			media.stop();
		}
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

			TextView tv = (TextView) viewSession1.findViewById(R.id.textViewSession1Content);
			tv.setMovementMethod(ScrollingMovementMethod.getInstance());
			fadeInAndShowContent(tv, MSG_SHOW_CONTENT_SESSION1_END);

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

	private void releaseBmp(Bitmap bitmap)
	{
		if (null != bitmap)
		{
			if (!bitmap.isRecycled())
				bitmap.recycle();
			bitmap = null;
		}
	}

	private void setInputLayout(final int nSession)
	{
		if (null == viewKeyInput)
			return;
		releaseBmp(bmpPasswordBG);
		bmpPasswordBG = BitmapFactory.decodeResource(getResources(), listInputLayout.get(nSession).mnBackground);
		viewKeyInput.findViewById(R.id.linearLayoutPasswordMain)
				.setBackground(new BitmapDrawable(getResources(), bmpPasswordBG));

		viewKeyInput.findViewById(R.id.editTextKey1).setBackgroundResource(listInputLayout.get(nSession).mnInput);
		viewKeyInput.findViewById(R.id.editTextKey2).setBackgroundResource(listInputLayout.get(nSession).mnInput);
		viewKeyInput.findViewById(R.id.editTextKey3).setBackgroundResource(listInputLayout.get(nSession).mnInput);

		((TextView) viewKeyInput.findViewById(R.id.textViewKeyInputSession))
				.setText(listInputLayout.get(nSession).mnSessionNum);
		((TextView) viewKeyInput.findViewById(R.id.textViewKeyInputTitle))
				.setText(listInputLayout.get(nSession).mnSessionTitle);

		releaseBmp(bmpFailBG);
		bmpFailBG = BitmapFactory.decodeResource(getResources(), listInputLayout.get(nSession).mnFail);
		flipperHandler.getView(FlipperHandler.VIEW_ID_FAIL)
				.setBackground(new BitmapDrawable(getResources(), bmpFailBG));

		releaseBmp(bmpSuccessBG);
		bmpSuccessBG = BitmapFactory.decodeResource(getResources(), listInputLayout.get(nSession).mnSuccess);
		flipperHandler.getView(FlipperHandler.VIEW_ID_SUCCESS)
				.setBackground(new BitmapDrawable(getResources(), bmpSuccessBG));
	}

	private void initSession1()
	{
		viewSession1.findViewById(R.id.buttonSession1Key).setVisibility(View.VISIBLE);
		viewSession1.findViewById(R.id.buttonSession1Key).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				setInputLayout(0);
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
							answer_error_count++;
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
								Global.theApplication.btSend("b");
								Logs.showTrace("Game Session1 end:" + String.valueOf(session1_e));

								HashMap<String, String> message = new HashMap<String, String>();
								message.put("PAGE", "SESSSON 1");
								message.put("TIME", formatTime(session1_e - session1_s));
								message.put("INCORRECT ANSWER COUNT", String.valueOf(answer_error_count));
								tracker.send(message);
								answer_error_count = 0;

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

												TextView tv = (TextView) viewSession2
														.findViewById(R.id.textViewSession2Content);
												tv.setMovementMethod(ScrollingMovementMethod.getInstance());
												fadeInAndShowContent(tv, MSG_SHOW_CONTENT_SESSION2_END);

												session2_s = System.currentTimeMillis();
												Logs.showTrace("Game Session2 start:" + String.valueOf(session2_s));
											}
										});
							}
							else
							{
								answer_error_count++;
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
				setInputLayout(1);
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
							answer_error_count++;
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
								Global.theApplication.btSend("d");

								Logs.showTrace("Game Session2 end:" + String.valueOf(session2_e));

								HashMap<String, String> message = new HashMap<String, String>();
								message.put("PAGE", "SESSSON 2");
								message.put("TIME", formatTime(session2_e - session2_s));
								message.put("INCORRECT ANSWER COUNT", String.valueOf(answer_error_count));
								tracker.send(message);
								answer_error_count = 0;

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

												TextView tv = (TextView) viewSession3
														.findViewById(R.id.textViewSession3Content);
												tv.setMovementMethod(ScrollingMovementMethod.getInstance());
												fadeInAndShowContent(tv, MSG_SHOW_CONTENT_SESSION3_END);

												session3_s = System.currentTimeMillis();
												Logs.showTrace("Game Session3 start:" + String.valueOf(session3_s));
											}
										});
							}
							else
							{
								answer_error_count++;
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
				setInputLayout(2);
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
							answer_error_count++;
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
								Global.theApplication.btSend("f");

								Logs.showTrace("Game Session3 end:" + String.valueOf(session3_e));

								HashMap<String, String> message = new HashMap<String, String>();
								message.put("PAGE", "SESSSON 3");
								message.put("TIME", formatTime(session3_e - session3_s));
								message.put("INCORRECT ANSWER COUNT", String.valueOf(answer_error_count));
								tracker.send(message);
								answer_error_count = 0;

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

												TextView tv = (TextView) viewSession4
														.findViewById(R.id.textViewSession4Content);
												tv.setMovementMethod(ScrollingMovementMethod.getInstance());
												fadeInAndShowContent(tv, MSG_SHOW_CONTENT_SESSION4_END);

												session4_s = System.currentTimeMillis();
												Logs.showTrace("Game Session4 start:" + String.valueOf(session4_s));
											}
										});
							}
							else
							{
								answer_error_count++;
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
				setInputLayout(3);
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
							answer_error_count++;
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
								Global.theApplication.btSend("h");

								Logs.showTrace("Game Session4 end:" + String.valueOf(session4_e));

								HashMap<String, String> message = new HashMap<String, String>();
								message.put("PAGE", "SESSSON 4");
								message.put("PASS TIME", formatTime(session4_e - session4_s));
								message.put("INCORRECT ANSWER COUNT", String.valueOf(answer_error_count));
								tracker.send(message);
								answer_error_count = 0;

								showEnding();
							}
							else
							{
								answer_error_count++;
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
		Global.theApplication.btRelease();

		String strTime = formatTime(ltotalTime);

		HashMap<String, String> message = new HashMap<String, String>();
		message.put("PAGE", "PAGE_ENDING");
		message.put("TOTAL TIME", strTime);
		tracker.send(message);

		TextView tvTime = (TextView) pageHandler.getView(ViewPagerHandler.PAGE_ENDING)
				.findViewById(R.id.textViewEndingTime);
		tvTime.setText(strTime);
		pageHandler.getView(ViewPagerHandler.PAGE_ENDING).findViewById(R.id.imageViewEndingShare)
				.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						HashMap<String, String> message = new HashMap<String, String>();
						message.put("PAGE", "CAMERA_PAGE");
						tracker.send(message);
						showCamera();
					}
				});

		pageHandler.getView(ViewPagerHandler.PAGE_ENDING).findViewById(R.id.imageViewEndingHome)
				.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						showLayout(LAYOUT_HOME);
					}
				});

		flipperHandler.close();
		pageHandler.showPage(ViewPagerHandler.PAGE_ENDING);
		selfHandler.sendEmptyMessageDelayed(MSG_SHOW_SHARE_DIALOG, 2000);

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
		if (null != dialogLoading)
		{
			dialogLoading.dismiss();
			dialogLoading = null;
		}

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
			case MSG_SHOW_GAMEOVER:
				showGameover();
				break;
			case MSG.CALLBACK_BT:
				btCallback(msg.arg1);
				break;
			}
		}

	};

	private void btCallback(final int nState)
	{
		HashMap<String, String> message = new HashMap<String, String>();

		switch(nState)
		{
		case BlueToothHandler.BOND_BONDED:
			if (null != dialogLoading)
			{
				dialogLoading.dismiss();
				dialogLoading = null;
			}
			message.put("PAGE", "LAYOUT_HOME");
			message.put("BLUETOOTH STATE", "ON");
			message.put("BLUETOOTH LINK", "Success");
			tracker.send(message);

			break;
		case BlueToothHandler.CANCEL_BY_USER:
			if (null != dialogLoading)
			{
				dialogLoading.dismiss();
				dialogLoading = null;
			}

			message.put("PAGE", "LAYOUT_HOME");
			message.put("BLUETOOTH STATE", "CANCEL_BY_USER");
			tracker.send(message);

			break;
		case BlueToothHandler.BOND_NONE:
			if (null != dialogLoading)
			{
				dialogLoading.dismiss();
				dialogLoading = null;
			}
			message.put("PAGE", "LAYOUT_HOME");
			message.put("BLUETOOTH STATE", "ON");
			message.put("BLUETOOTH LINK", "Fail");
			tracker.send(message);

			DialogHandler.showAlert(MainActivity.this, "藍芽通訊失敗\n請重新啟動程式", false);
			break;

		}
	}

	private void showGameover()
	{
		this.setContentView(R.layout.gameover);

		HashMap<String, String> message = new HashMap<String, String>();
		message.put("PAGE", "LAYOUT_GAMEOVER");
		tracker.send(message);

		this.findViewById(R.id.buttonGameoverExit).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

				finish();
				System.exit(0);
			}
		});

		Logs.showTrace("Show Game Over: photo: " + mstrPicturePath);
		if (null != mstrPicturePath)
		{
			File imgFile = new File(mstrPicturePath);
			if (imgFile.exists())
			{
				Logs.showTrace("Show photo on gameover");
				Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
				ImageView imgPic = (ImageView) this.findViewById(R.id.imageViewGameoverPic);
				imgPic.setImageBitmap(myBitmap);
			}
		}
	}

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

		if (null != img)
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
		openCameraIntent.putExtra("time", mstrTimeofPlay);
		startActivityForResult(openCameraIntent, MSG.REQUEST_CODE_CAMERA);
		// this.setContentView(R.layout.welcome);
	}

}
