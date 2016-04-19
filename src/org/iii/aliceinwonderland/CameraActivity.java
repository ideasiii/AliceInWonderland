package org.iii.aliceinwonderland;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

@SuppressWarnings("deprecation")
public class CameraActivity extends Activity
{
	private Camera			mCamera;
	private CameraPreview	mPreview;
	private String			mstrPicPath	= null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);

		if (checkCameraHardware(this))
		{
			mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT, this);
			mCamera.setFaceDetectionListener(new MyFaceDetectionListener());
			mPreview = new CameraPreview(this, mCamera);
			FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
			preview.addView(mPreview);
			// Add a listener to the Capture button
			findViewById(R.id.imageViewCameraCapture).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					// get an image from the camera
					mCamera.takePicture(null, null, mPicture);
				}
			});
		}

	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		releaseCamera(); // release the camera immediately on pause event
	}

	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context)
	{
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
		{
			// this device has a camera
			return true;
		}
		else
		{
			// no camera on this device
			return false;
		}
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(int nType, Context context)
	{
		Camera c = null;
		try
		{
			c = Camera.open(nType); // attempt to get a Camera instance
			Camera.Parameters parameters = c.getParameters();
			parameters.setPreviewSize(1280, 720);
			parameters.setPictureSize(1280, 720);
			parameters.setAutoWhiteBalanceLock(true);
			parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
			parameters.setPictureFormat(ImageFormat.JPEG);
			parameters.setPreviewFrameRate(29);
			parameters.setJpegQuality(100);
			if (isSupportZoom(c))
			{
				parameters.setZoom(1);
			}

			c.setParameters(parameters);
			Logs.showTrace("Camera Opened");
		}
		catch (Exception e)
		{
			Logs.showError("Camera is not available (in use or does not exist) Exception: " + e.getMessage());
		}
		return c; // returns null if camera is unavailable
	}

	/** A basic Camera preview class */
	public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
	{
		private SurfaceHolder	mHolder;
		private Camera			mCamera;

		public CameraPreview(Context context, Camera camera)
		{
			super(context);
			mCamera = camera;

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			// deprecated setting, but required on Android versions prior to 3.0
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		public void surfaceCreated(SurfaceHolder holder)
		{
			// The Surface has been created, now tell the camera where to draw
			// the preview.
			try
			{
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
				// startFaceDetection();
			}
			catch (IOException e)
			{
				Logs.showError("Error setting camera preview: " + e.getMessage());
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder)
		{

		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
		{
			// If your preview can change or rotate, take care of those events
			// here.
			// Make sure to stop the preview before resizing or reformatting it.

			if (mHolder.getSurface() == null)
			{
				return;
			}

			// stop preview before making changes
			try
			{
				mCamera.stopPreview();
			}
			catch (Exception e)
			{
				Logs.showError("Error stopping camera preview: " + e.getMessage());
			}

			// set preview size and make any resize, rotate or
			// reformatting changes here

			// start preview with new settings
			try
			{
				mCamera.setPreviewDisplay(mHolder);
				mCamera.startPreview();
				// startFaceDetection(); // re-start face detection feature

			}
			catch (Exception e)
			{
				Logs.showError("Error starting camera preview: " + e.getMessage());
			}
		}
	}

	public Bitmap convertBmp(Bitmap bmp)
	{
		int w = bmp.getWidth();
		int h = bmp.getHeight();

		Matrix matrix = new Matrix();
		matrix.postScale(-1, 1); // 镜像水平翻转
		Bitmap convertBmp = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);

		return convertBmp;
	}

	private PictureCallback mPicture = new PictureCallback()
	{

		@Override
		public void onPictureTaken(byte[] data, Camera camera)
		{
			Bitmap bmPhoto = convertBmp(BitmapFactory.decodeByteArray(data, 0, data.length));
			Bitmap bmRabbit = BitmapFactory.decodeResource(getResources(), R.drawable.camera_rabbit2);
			Bitmap bmCombine = combineBitmap(bmPhoto, bmRabbit);
			SaveImage(bmCombine);
			bmCombine.recycle();
			bmRabbit.recycle();
			bmPhoto.recycle();
			close();
		}
	};

	private void SaveImage(Bitmap finalBitmap)
	{

		String root = Environment.getExternalStorageDirectory().toString();
		File myDir = new File(root + "/underbox");
		if (!myDir.exists())
			myDir.mkdirs();

		File file = new File(myDir, "alice.jpg");
		if (file.exists())
			file.delete();
		try
		{
			FileOutputStream out = new FileOutputStream(file);
			finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
			mstrPicPath = file.getPath();
		}
		catch (Exception e)
		{
			Logs.showError("Save Image Exception: " + e.getMessage());
		}
	}

	private void releaseCamera()
	{
		if (mCamera != null)
		{
			mCamera.release(); // release the camera for other applications
			mCamera = null;
			Logs.showTrace("Camera Released");
		}
	}

	class MyFaceDetectionListener implements Camera.FaceDetectionListener
	{
		@Override
		public void onFaceDetection(Face[] faces, Camera camera)
		{
			if (faces.length > 0)
			{
				Logs.showTrace("face detected: " + faces.length + " Face 1 Location X: " + faces[0].rect.centerX()
						+ "Y: " + faces[0].rect.centerY());
			}

		}
	}

	public void startFaceDetection()
	{
		// Try starting Face Detection
		Camera.Parameters params = mCamera.getParameters();

		// start face detection only *after* preview has started
		if (params.getMaxNumDetectedFaces() > 0)
		{
			// camera supports face detection, so can start it:
			mCamera.startFaceDetection();
		}
	}

	private static boolean isSupportZoom(Camera camera)
	{
		boolean isSuppport = false;
		if (null != camera && camera.getParameters().isZoomSupported())
		{
			isSuppport = true;
		}
		return isSuppport;
	}

	public static Bitmap combineBitmap(Bitmap background, Bitmap foreground)
	{
		if (background == null)
		{
			return null;
		}
		int bgWidth = background.getWidth();
		int bgHeight = background.getHeight();
		int fgWidth = foreground.getWidth();
		int fgHeight = foreground.getHeight();
		Bitmap newmap = Bitmap.createBitmap(bgWidth, bgHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas(newmap);
		canvas.drawBitmap(background, 0, 0, null);
		canvas.drawBitmap(foreground, (bgWidth - fgWidth), (bgHeight - fgHeight), null);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return newmap;
	}

	private void close()
	{
		Bundle bundle = new Bundle();
		bundle.putString("picture", mstrPicPath);
		Intent intent = new Intent();
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		finish();
	}
}
