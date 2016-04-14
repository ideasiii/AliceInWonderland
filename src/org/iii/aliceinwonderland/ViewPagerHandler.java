package org.iii.aliceinwonderland;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

public class ViewPagerHandler extends ViewBaseHandler
{
	private ViewPager			viewPager		= null;
	private ViewPagerAdapter	pagerAdapter	= null;
	public static int			PAGE_INTRO;
	public static int			PAGE_SESSION1;
	public static int			PAGE_SESSION2;
	public static int			PAGE_SESSION3;
	public static int			PAGE_SESSION4;
	public static int			PAGE_ENDING;

	public ViewPagerHandler(Activity activity, Handler handler)
	{
		super(activity, handler);

	}

	public boolean init()
	{
		viewPager = (ViewPager) theActivity.findViewById(R.id.ViewPager);

		if (null == viewPager)
		{
			return false;
		}

		/** Disable scrolling **/
		viewPager.setOnTouchListener(new OnTouchListener()
		{
			public boolean onTouch(View arg0, MotionEvent arg1)
			{
				return true;
			}
		});

		LayoutInflater Inflater = LayoutInflater.from(theActivity);
		pagerAdapter = new ViewPagerAdapter();

		PAGE_INTRO = pagerAdapter.addPage(Inflater.inflate(R.layout.intro, null),
				theActivity.getString(R.string.introduction));
		PAGE_SESSION1 = pagerAdapter.addPage(Inflater.inflate(R.layout.session1, null),
				theActivity.getString(R.string.session1_title2));
		PAGE_SESSION2 = pagerAdapter.addPage(Inflater.inflate(R.layout.session2, null),
				theActivity.getString(R.string.session2_title2));
		PAGE_SESSION3 = pagerAdapter.addPage(Inflater.inflate(R.layout.session3, null),
				theActivity.getString(R.string.session3_title2));
		PAGE_SESSION4 = pagerAdapter.addPage(Inflater.inflate(R.layout.session4, null),
				theActivity.getString(R.string.session4_title2));
		PAGE_ENDING = pagerAdapter.addPage(Inflater.inflate(R.layout.ending, null),
				theActivity.getString(R.string.ending_title));
		viewPager.setAdapter(pagerAdapter);
		return true;
	}

	public View getView(final int nId)
	{
		return pagerAdapter.getView(nId);
	}

	public void showPage(final int nPageIndex)
	{
		if (0 > nPageIndex || pagerAdapter.getCount() <= nPageIndex)
			return;

		viewPager.setCurrentItem(nPageIndex, true);
	}

	private class ViewPagerAdapter extends PagerAdapter
	{

		private class Page
		{
			public View		view		= null;
			public String	strTitle	= null;
		}

		private SparseArray<Page> Pages = null;

		public ViewPagerAdapter()
		{
			Pages = new SparseArray<Page>();
		}

		@Override
		public int getCount()
		{
			return Pages.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1)
		{
			return (arg0 == arg1);
		}

		@Override
		public int getItemPosition(Object object)
		{
			return super.getItemPosition(object);
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			return Pages.get(position).strTitle;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object)
		{
			container.removeView(Pages.get(position).view);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position)
		{
			((ViewPager) container).addView(Pages.get(position).view, 0);
			return Pages.get(position).view;
		}

		public int addPage(final View view, final String strTitle)
		{
			Page page = new Page();
			page.view = view;
			page.strTitle = strTitle;
			Pages.put(Pages.size(), page);
			return (Pages.size() - 1);
		}

		public View getView(final int nId)
		{
			return Pages.get(nId).view;
		}

	}
}
