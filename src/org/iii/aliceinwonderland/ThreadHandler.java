package org.iii.aliceinwonderland;

import android.os.Process;

public class ThreadHandler
{
	private Thread	mThread;
	private int		mnThdId;
	private boolean	mbThdIdSet;
	private boolean	mbFinished;

	synchronized private void setThdId(int nThdId)
	{
		mnThdId = nThdId;
		mbThdIdSet = true;
		ThreadHandler.this.notifyAll();
	}

	synchronized private void setFinished()
	{
		mbFinished = true;
	}

	public ThreadHandler(final Runnable r)
	{
		Runnable wrapper = new Runnable()
		{
			public void run()
			{
				setThdId(Process.myTid());
				try
				{
					r.run();
				}
				finally
				{
					setFinished();
				}
			}
		};

		mThread = new Thread(wrapper);
	}

	synchronized public void start()
	{
		mThread.start();
	}

	synchronized public void setName(String name)
	{
		mThread.setName(name);
	}

	public void join()
	{
		try
		{
			mThread.join();
		}
		catch (InterruptedException ex)
		{
			ex.printStackTrace();
		}
	}

	public long getId()
	{
		return mThread.getId();
	}

	public Thread realThread()
	{
		return mThread;
	}

	synchronized public void setPriority(int androidOsPriority)
	{
		while (!mbThdIdSet)
		{
			try
			{
				ThreadHandler.this.wait();
			}
			catch (InterruptedException ex)
			{
				ex.printStackTrace();
			}
		}
		if (!mbFinished)
			Process.setThreadPriority(mnThdId, androidOsPriority);
	}

	synchronized public void toBackground()
	{
		setPriority(Process.THREAD_PRIORITY_BACKGROUND);
	}

	synchronized public void toForeground()
	{
		setPriority(Process.THREAD_PRIORITY_FOREGROUND);
	}
}
