package org.iii.aliceinwonderland;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

public class MainActivity extends Activity
{

	private final int			LAYOUT_WELCOME	= 0;
	private final int			LAYOUT_HOME		= 1;
	private final int			LAYOUT_STORY	= 2;
	private final int			LAYOUT_MAIN		= 3;

	private final int			MSG_SHOW_MAIN	= 10;
	private final int			MSG_SHOW_HOME	= 20;

	private ViewPagerHandler	pageHandler		= null;
	private FlipperHandler		flipperHandler	= null;
	private View				viewSession1	= null;
	private ImageView			imgInitLogo;
	private int[]				imgRes			= { R.drawable.init_logo, R.drawable.undobox };

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.getActionBar().hide();
		showLayout(LAYOUT_WELCOME);
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

		}

		pageHandler = new ViewPagerHandler(this, selfHandler);
		if (pageHandler.init())
		{
			pageHandler.showPage(1);
			viewSession1 = pageHandler.getView(ViewPagerHandler.PAGE_SESSION1);
			viewSession1.findViewById(R.id.imageViewBook).setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					flipperHandler.showView(FlipperHandler.VIEW_ID_STORY);
				}
			});
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
			}
		}

	};

	private void fadeOutAndHideImage(final ImageView img)
	{
		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator());
		fadeOut.setDuration(2000);

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
		fadeIn.setDuration(2000);

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
}
