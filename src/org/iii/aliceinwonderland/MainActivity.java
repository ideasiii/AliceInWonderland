package org.iii.aliceinwonderland;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
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

public class MainActivity extends Activity
{

	private final int			LAYOUT_WELCOME					= 0;
	private final int			LAYOUT_HOME						= 1;
	private final int			LAYOUT_STORY					= 2;
	private final int			LAYOUT_MAIN						= 3;

	private final int			MSG_SHOW_MAIN					= 10;
	private final int			MSG_SHOW_HOME					= 20;
	private final int			MSG_SHOW_CONTENT_SESSION1_END	= 30;
	private final int			MSG_SHOW_CONTENT_SESSION2_END	= 31;
	private final int			MSG_SHOW_CONTENT_SESSION3_END	= 32;
	private final int			MSG_SHOW_CONTENT_SESSION4_END	= 33;
	private final int			MSG_SHOW_SHARE_DIALOG			= 34;

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

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.getActionBar().hide();
		showLayout(LAYOUT_WELCOME);

		FacebookSdk.sdkInitialize(getApplicationContext());
		AppEventsLogger.activateApp(this);
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
			break;
		case LAYOUT_MAIN:
			setContentView(R.layout.activity_main);
			initMainHandler();
			break;
		}
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
		// viewSession1.findViewById(R.id.linearLayoutSession1Main).setBackgroundResource(R.color.Black_Gray_Deep);
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
		// viewSession2.findViewById(R.id.linearLayoutSession2Main).setBackgroundResource(R.color.Black_Gray_Deep);
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
		// viewSession3.findViewById(R.id.linearLayoutSession3Main).setBackgroundResource(R.color.Black_Gray_Deep);
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
		// viewSession4.findViewById(R.id.linearLayoutSession4Main).setBackgroundResource(R.color.Black_Gray_Deep);
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
		pageHandler.getView(ViewPagerHandler.PAGE_ENDING).findViewById(R.id.imageViewEndingShare)
				.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						Intent sendIntent = new Intent();
						sendIntent.setAction(Intent.ACTION_SEND);
						sendIntent.putExtra(Intent.EXTRA_TEXT, "xxxxxxxxxx");
						sendIntent.setType("text/plain");
						sendIntent.setPackage("com.facebook.orca");
						startActivity(sendIntent);

					}
				});

		flipperHandler.close();
		pageHandler.showPage(ViewPagerHandler.PAGE_ENDING);
		selfHandler.sendEmptyMessageDelayed(MSG_SHOW_SHARE_DIALOG, 3000);

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
				selfHandler.sendEmptyMessageDelayed(MSG_SHOW_HOME, 2000);
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

}
