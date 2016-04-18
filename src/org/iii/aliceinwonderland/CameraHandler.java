package org.iii.aliceinwonderland;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

public final class CameraHandler
{
	private static CameraHandler	cameraHandler				= null;
	private boolean					useOneShotPreviewCallback	= false;
	private Context					theContext					= null;
	private Camera					camera						= null;
	private Point					cameraResolution			= null;
	private final int				TEN_DESIRED_ZOOM			= 27;
	private final int				MSG_AUTO_FOCUS				= 111;
	private OnPreviewListener		onPreviewListener			= null;
	private boolean					bAutoFocus					= true;
	public static final int			FRONT						= 1;	// 前鏡頭
	public static final int			BACK						= 2;	// 後鏡頭
	// 0表示后置，1表示前置
	private int						cameraPosition				= 1;

	public static interface OnPreviewListener
	{
		void onPreview(byte[] data);
	}

	public void setOnPreviewListener(OnPreviewListener listener)
	{
		onPreviewListener = listener;
	}

	private CameraHandler(Context context)
	{
		theContext = context;
		if (3 < Device.getSdkVer())
		{
			useOneShotPreviewCallback = true;
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		release();
		super.finalize();
	}

	public static CameraHandler getInstance(Context context)
	{
		if (null == cameraHandler)
		{
			cameraHandler = new CameraHandler(context);
		}

		return cameraHandler;
	}

	public Size getPreviewSize()
	{
		return camera.getParameters().getPreviewSize();
	}

	public int getPreviewFormat()
	{
		return camera.getParameters().getPreviewFormat();
	}

	public Camera switchCamera(SurfaceHolder holder)
	{
		int cameraCount = 0;
		CameraInfo cameraInfo = new CameraInfo();
		cameraCount = Camera.getNumberOfCameras();

		for (int i = 0; i < cameraCount; i++)
		{
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraPosition == 1)
			{
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
				{
					release();
					return Camera.open(i);
				}
			}
			else
			{
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
				{
					release();
					return Camera.open(i);
				}
			}

		}
		return null;
	}

	private Camera openCamera(int type)
	{

		int frontIndex = -1;
		int backIndex = -1;
		int cameraCount = Camera.getNumberOfCameras();
		CameraInfo info = new CameraInfo();
		for (int cameraIndex = 0; cameraIndex < cameraCount; cameraIndex++)
		{
			Camera.getCameraInfo(cameraIndex, info);
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT)
			{
				frontIndex = cameraIndex;
			}
			else if (info.facing == CameraInfo.CAMERA_FACING_BACK)
			{
				backIndex = cameraIndex;
			}
		}

		if (type == FRONT && frontIndex != -1)
		{
			return Camera.open(frontIndex);
		}
		else if (type == BACK && backIndex != -1)
		{
			return Camera.open(backIndex);
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public void open(SurfaceHolder holder, final int nType) throws IOException
	{
		if (null == camera)
		{
			camera = openCamera(nType);
			if (null == camera)
			{
				Logs.showTrace("Camera Open Exception");
				throw new IOException();
			}
			camera.setPreviewDisplay(holder);

			Camera.Parameters parameters = camera.getParameters();

			Point screenResolution = Device.getScreenResolution(theContext);

			Point screenResolutionForCamera = new Point();
			screenResolutionForCamera.x = screenResolution.x;
			screenResolutionForCamera.y = screenResolution.y;

			cameraResolution = getCameraResolution(parameters, screenResolutionForCamera);
			parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
			if (Build.MODEL.contains("Behold II") && Device.getSdkVer() == 3)
			{ // 3
				parameters.set("flash-value", 1);
			}
			else
			{
				parameters.set("flash-value", 2);
			}
			parameters.set("flash-mode", "off");
			setZoom(parameters);
			parameters.setPictureFormat(ImageFormat.JPEG);
			camera.setParameters(parameters);
		}
	}

	public Point getCameraResolution()
	{
		return cameraResolution;
	}

	private void setZoom(Camera.Parameters parameters)
	{

		String zoomSupportedString = parameters.get("zoom-supported");
		if (zoomSupportedString != null && !Boolean.parseBoolean(zoomSupportedString))
		{
			Logs.showTrace("Camera Zoom Support: " + zoomSupportedString);
			return;
		}

		int tenDesiredZoom = TEN_DESIRED_ZOOM;

		String maxZoomString = parameters.get("max-zoom");
		if (maxZoomString != null)
		{
			Logs.showTrace("Camera Max Zoom: " + maxZoomString);
			try
			{
				int tenMaxZoom = (int) (10.0 * Double.parseDouble(maxZoomString));
				if (tenDesiredZoom > tenMaxZoom)
				{
					tenDesiredZoom = tenMaxZoom;
				}
			}
			catch (NumberFormatException nfe)
			{
				Logs.showError("Bad max-zoom: " + maxZoomString);
			}
		}

		String takingPictureZoomMaxString = parameters.get("taking-picture-zoom-max");
		if (takingPictureZoomMaxString != null)
		{
			Logs.showTrace("Camera Picture Zoom Max: " + takingPictureZoomMaxString);
			try
			{
				int tenMaxZoom = Integer.parseInt(takingPictureZoomMaxString);
				if (tenDesiredZoom > tenMaxZoom)
				{
					tenDesiredZoom = tenMaxZoom;
				}
			}
			catch (NumberFormatException nfe)
			{
				Logs.showError("Bad taking-picture-zoom-max: " + takingPictureZoomMaxString);
			}
		}

		String motZoomValuesString = parameters.get("mot-zoom-values");
		if (motZoomValuesString != null)
		{
			Logs.showTrace("Camera Mot Zoom: " + motZoomValuesString);
			tenDesiredZoom = findBestMotZoomValue(motZoomValuesString, tenDesiredZoom);
		}

		String motZoomStepString = parameters.get("mot-zoom-step");
		if (motZoomStepString != null)
		{
			Logs.showTrace("Camera Mot Zoom Step: " + motZoomStepString);
			try
			{
				double motZoomStep = Double.parseDouble(motZoomStepString.trim());
				int tenZoomStep = (int) (10.0 * motZoomStep);
				if (tenZoomStep > 1)
				{
					tenDesiredZoom -= tenDesiredZoom % tenZoomStep;
				}
			}
			catch (NumberFormatException nfe)
			{
				// continue
			}
		}

		// Set zoom. This helps encourage the user to pull back.
		// Some devices like the Behold have a zoom parameter
		if (maxZoomString != null || motZoomValuesString != null)
		{
			parameters.set("zoom", String.valueOf(tenDesiredZoom / 10.0));
		}

		// Most devices, like the Hero, appear to expose this zoom parameter.
		// It takes on values like "27" which appears to mean 2.7x zoom
		if (takingPictureZoomMaxString != null)
		{
			parameters.set("taking-picture-zoom", tenDesiredZoom);
		}
	}

	private static int findBestMotZoomValue(CharSequence stringValues, int tenDesiredZoom)
	{
		int tenBestValue = 0;
		for (String stringValue : Pattern.compile(",").split(stringValues))
		{
			stringValue = stringValue.trim();
			double value;
			try
			{
				value = Double.parseDouble(stringValue);
			}
			catch (NumberFormatException nfe)
			{
				return tenDesiredZoom;
			}
			int tenValue = (int) (10.0 * value);
			if (Math.abs(tenDesiredZoom - value) < Math.abs(tenDesiredZoom - tenBestValue))
			{
				tenBestValue = tenValue;
			}
		}
		return tenBestValue;
	}

	private Point getCameraResolution(Camera.Parameters parameters, Point screenResolution)
	{
		String previewSizeValueString = parameters.get("preview-size-values");
		if (previewSizeValueString == null)
		{
			previewSizeValueString = parameters.get("preview-size-value");
		}

		Logs.showTrace("Camera Preview Size:" + previewSizeValueString);
		Point cameraResolution = null;

		if (previewSizeValueString != null)
		{
			cameraResolution = findBestPreviewSizeValue(previewSizeValueString, screenResolution);
		}

		if (cameraResolution == null)
		{
			cameraResolution = new Point((screenResolution.x >> 3) << 3, (screenResolution.y >> 3) << 3);
		}

		return cameraResolution;
	}

	private Point findBestPreviewSizeValue(CharSequence previewSizeValueString, Point screenResolution)
	{
		int bestX = 0;
		int bestY = 0;
		int diff = Integer.MAX_VALUE;
		for (String previewSize : Pattern.compile(",").split(previewSizeValueString))
		{
			previewSize = previewSize.trim();
			int dimPosition = previewSize.indexOf('x');
			if (dimPosition < 0)
			{
				continue;
			}

			int newX;
			int newY;
			try
			{
				newX = Integer.parseInt(previewSize.substring(0, dimPosition));
				newY = Integer.parseInt(previewSize.substring(dimPosition + 1));
			}
			catch (NumberFormatException nfe)
			{
				continue;
			}

			int newDiff = Math.abs(newX - screenResolution.x) + Math.abs(newY - screenResolution.y);
			if (newDiff == 0)
			{
				bestX = newX;
				bestY = newY;
				break;
			}
			else if (newDiff < diff)
			{
				bestX = newX;
				bestY = newY;
				diff = newDiff;
			}

		}

		if (bestX > 0 && bestY > 0)
		{
			return new Point(bestX, bestY);
		}
		return null;
	}

	public void startPreview()
	{
		camera.startPreview();
		if (useOneShotPreviewCallback)
		{
			camera.setOneShotPreviewCallback(previewCallback);
		}
		else
		{
			camera.setPreviewCallback(previewCallback);
		}
		// camera.autoFocus(autoFocusCallback);
	}

	public void stopPreview()
	{
		if (null != camera)
		{
			camera.stopPreview();
			camera.setPreviewCallback(null);
		}
	}

	public void release()
	{
		if (null != camera)
		{
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	public void setAutoFocus(boolean bEnable)
	{
		bAutoFocus = bEnable;
		if (bAutoFocus)
		{
			camera.autoFocus(autoFocusCallback);
		}
	}

	public void setCameraFocus()
	{
		if (null != camera)
		{
			camera.autoFocus(autoFocusCallback);
		}
	}

	public void takePicture()
	{
		if (null != camera)
		{
			camera.takePicture(null, null, picture);
		}
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

	public Bitmap convertBmp(Bitmap bmp)
	{
		int w = bmp.getWidth();
		int h = bmp.getHeight();

		Matrix matrix = new Matrix();
		matrix.postScale(-1, 1); // 镜像水平翻转
		Bitmap convertBmp = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);

		return convertBmp;
	}

	@SuppressWarnings("deprecation")
	private PictureCallback				picture				= new PictureCallback()
															{
																@Override
																public void onPictureTaken(byte[] data, Camera camera)
																{
																	try
																	{

																		String mstrPicturePath = getSdcardPath()
																				+ "Download" + File.separator
																				+ "alice.png";
																		Bitmap bm = convertBmp(BitmapFactory
																				.decodeByteArray(data, 0, data.length));
																		File file = new File(mstrPicturePath);
																		BufferedOutputStream bos = new BufferedOutputStream(
																				new FileOutputStream(file));
																		bm.compress(Bitmap.CompressFormat.JPEG, 100,
																				bos);

																		bos.flush();
																		bos.close();
																		bm.recycle();
																		Logs.showTrace("save picture");
																	}
																	catch (IOException e)
																	{
																		// TODO Auto-generated catch block
																		e.printStackTrace();
																	}

																}
															};

	private Camera.PreviewCallback		previewCallback		= new Camera.PreviewCallback()
															{
																@Override
																public void onPreviewFrame(byte[] data, Camera camera)
																{
																	Logs.showTrace("Camera Preview Callback");
																	if (null != onPreviewListener)
																	{
																		onPreviewListener.onPreview(data);
																	}
																}
															};

	private Camera.AutoFocusCallback	autoFocusCallback	= new Camera.AutoFocusCallback()
															{
																@Override
																public void onAutoFocus(boolean success, Camera camera)
																{
																	Logs.showTrace("Camera Auto Focus Callback");
																	// camera.takePicture(null, null, picture);
																}
															};

	private Handler						handler				= new Handler()
															{

																@Override
																public void handleMessage(Message msg)
																{
																	switch(msg.what)
																	{
																	case MSG_AUTO_FOCUS:
																		if (null != camera)
																		{
																			camera.autoFocus(autoFocusCallback);
																			startPreview();
																		}
																		break;
																	}
																}
															};
}
