package org.iii.aliceinwonderland;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class CameraActivity extends Activity
{

	private CameraHandler	cameraHandler	= null;
	private SurfaceView		surfaceView		= null;
	private SurfaceHolder	surfaceHolder	= null;
	private RelativeLayout	contain			= null;
	private String			mstrPicturePath	= null;

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
		mstrPicturePath = getSdcardPath() + "Download" + File.separator + "alice.png";
		Logs.showTrace("Alice Picture path:" + mstrPicturePath);

		this.findViewById(R.id.imageViewGetPicture).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// cameraHandler.setFocus();
				// exportBitmap(contain, mstrPicturePath);
				// takeScreenshot();
				cameraHandler.takePicture();
				Logs.showTrace("Capture picture!!");
			}
		});

	}

	public String getSdcardPath()
	{
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist)
		{
			sdDir = Environment.getExternalStorageDirectory();
			return sdDir.toString() + File.separator;
		}

		return null;
	}

	private boolean exportBitmap(View view, String strPath)
	{
		Bitmap bitmap = Bitmap.createBitmap(Device.getWidth(this), Device.getHeight(this), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bitmap);
		view.draw(c);

		FileOutputStream out;
		try
		{
			out = new FileOutputStream(strPath);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			Logs.showTrace("exportBitmap success");
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Logs.showTrace("exportBitmap exception:" + e.toString());
		}
		return false;
	}

	private void takeScreenshot()
	{
		Date now = new Date();
		android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

		try
		{
			// image naming and path to include sd card appending name you choose for file
			String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

			// create bitmap screen capture
			View v1 = getWindow().getDecorView().getRootView();
			v1.setDrawingCacheEnabled(true);
			Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
			v1.setDrawingCacheEnabled(false);

			File imageFile = new File(mPath);

			FileOutputStream outputStream = new FileOutputStream(imageFile);
			int quality = 100;
			bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
			outputStream.flush();
			outputStream.close();

			openScreenshot(imageFile);
		}
		catch (Throwable e)
		{
			// Several error may come out with file handling or OOM
			e.printStackTrace();
		}
	}

	private void openScreenshot(File imageFile)
	{
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(imageFile);
		intent.setDataAndType(uri, "image/*");
		startActivity(intent);
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
																				cameraHandler.open(surfaceHolder,
																						CameraHandler.FRONT);
																				cameraHandler.setAutoFocus(false);
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
																			cameraHandler.release();
																		}
																	};
}
