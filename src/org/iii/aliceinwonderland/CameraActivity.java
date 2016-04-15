package org.iii.aliceinwonderland;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class CameraActivity extends Activity
{

	private CameraHandler	cameraHandler	= null;
	private SurfaceView		surfaceView		= null;
	private SurfaceHolder	surfaceHolder	= null;
	private RelativeLayout	contain			= null;

	public CameraActivity()
	{

	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.camera);
		surfaceView = (SurfaceView) this.findViewById(R.id.surfaceViewScanner);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(surfaceHolderCallback);
		cameraHandler = CameraHandler.getInstance(this);
		cameraHandler.setOnPreviewListener(previewListener);
		contain = (RelativeLayout) this.findViewById(R.id.relativeLayoutScanMain);
	}

	private CameraHandler.OnPreviewListener	previewListener			= new CameraHandler.OnPreviewListener()
																	{
																		@Override
																		public void onPreview(byte[] data)
																		{
																			Logs.showTrace("Get Preview Data:"
																					+ data.toString());

																		}
																	};

	private SurfaceHolder.Callback			surfaceHolderCallback	= new SurfaceHolder.Callback()
																	{
																		@Override
																		public void surfaceCreated(SurfaceHolder holder)
																		{
																			Logs.showTrace("surfaceCreated");

																			try
																			{

																				cameraHandler.open(surfaceHolder);
																				cameraHandler.setAutoFocus(true);
																				cameraHandler.startPreview();
																			}
																			catch (IOException e)
																			{
																				Logs.showError(
																						"Exception: " + e.getMessage());
																			}

																		}

																		@Override
																		public void surfaceChanged(SurfaceHolder holder,
																				int format, int width, int height)
																		{
																			Logs.showTrace("surfaceChanged");

																		}

																		@Override
																		public void surfaceDestroyed(
																				SurfaceHolder holder)
																		{
																			Logs.showTrace("surfaceDestroyed");

																		}
																	};
}
