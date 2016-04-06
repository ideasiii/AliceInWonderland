package org.iii.aliceinwonderland;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

public class FlipperView extends RelativeLayout
{
	private ViewFlipper		flipper					= null;
	private final int		mnAnimationInDuration	= 300;
	private final int		mnAnimationOutDuration	= 150;
	public final int		ANIMATION_DOWN			= 0;
	public final int		ANIMATION_UP			= 1;
	private AnimationType	animationType			= null;
	private ThreadHandler	thdFlip					= null;
	private Handler			theHandler				= null;

	public FlipperView(Context context)
	{
		super(context);
		init(context);
	}

	public FlipperView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}

	public FlipperView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context);
	}

	@Override
	protected void finalize() throws Throwable
	{
		flipper.removeAllViews();
		this.removeAllViews();
		super.finalize();
	}

	private void init(Context context)
	{
		animationType = new AnimationType();
		flipper = new ViewFlipper(context);

		RelativeLayout viewChild = new RelativeLayout(context);
		viewChild.setLayoutParams(
				new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		viewChild.setVisibility(View.INVISIBLE);
		flipper.addView(viewChild, 0);
		flipper.setLayoutParams(
				new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		flipper.setInAnimation(animationType.inFromDownAnimation(mnAnimationInDuration));
		flipper.setOutAnimation(animationType.outToDownAnimation(mnAnimationOutDuration));
		this.setVisibility(View.GONE);
		this.addView(flipper);
		this.bringToFront();

		setClickClose(true);
	}

	public void setClickClose(final boolean bClose)
	{
		this.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				if (bClose)
					close();
			}
		});
	}

	public void setNotifyHandler(Handler handler)
	{
		theHandler = handler;
	}

	public int addChild(int nResId)
	{
		int nIndex = flipper.getChildCount();
		LayoutInflater layInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewChild = layInflater.inflate(nResId, null, false);
		flipper.addView(viewChild, nIndex);
		return nIndex;
	}

	public View getChildView(final int nIndex)
	{
		return flipper.getChildAt(nIndex);
	}

	public void showView(int nIndex)
	{
		this.bringToFront();
		this.setVisibility(View.VISIBLE);
		if (0 != flipper.getDisplayedChild())
		{
			flipper.setDisplayedChild(nIndex);
		}
		else
		{
			runShowChild(nIndex);
		}
	}

	private Handler updateHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
			case 0:
				if (null != flipper)
				{
					flipper.setDisplayedChild(msg.arg1);
				}
				break;
			case 1:
				if (null != flipper)
				{
					FlipperView.this.setVisibility(View.GONE);
					Common.postMessage(theHandler, MSG.FLIPPER_CLOSE, 0, 0, null);
				}
				break;
			}
		}
	};

	synchronized public void close()
	{

		flipper.setDisplayedChild(0);
		if (null != thdFlip)
		{
			thdFlip = null;
		}

		thdFlip = new ThreadHandler(new Runnable()
		{
			public void run()
			{
				try
				{
					Thread.sleep(500);
					updateHandler.sendEmptyMessage(1);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});
		thdFlip.start();
	}

	synchronized private void runShowChild(final int nIndex)
	{
		if (null != thdFlip)
		{
			thdFlip = null;
		}

		thdFlip = new ThreadHandler(new Runnable()
		{
			public void run()
			{
				try
				{
					Thread.sleep(200);
					Message msg = new Message();
					msg.what = 0;
					msg.arg1 = nIndex;
					updateHandler.sendMessage(msg);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});
		thdFlip.start();
	}

	public int getShowIndex()
	{
		return flipper.getDisplayedChild();
	}
}
