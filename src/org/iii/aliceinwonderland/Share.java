package org.iii.aliceinwonderland;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.util.SparseArray;

public class Share
{

	private Activity theActivity = null;

	public Share(Activity activity)
	{
		super();
		// TODO Auto-generated constructor stub
		theActivity = activity;
	}

	@Override
	protected void finalize() throws Throwable
	{
		// TODO Auto-generated method stub
		super.finalize();
	}

	public void shareAll(String strTitle, String strSubject, String strMessage, SparseArray<String> listImagePath)
	{
		Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_TITLE, strTitle);
		intent.putExtra(Intent.EXTRA_SUBJECT, strSubject);
		if (null != strMessage)
		{
			intent.putExtra(Intent.EXTRA_TEXT, strMessage);
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (null != listImagePath && 0 < listImagePath.size())
		{
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, getUriListForImages(listImagePath));
		}

		// theActivity.startActivity(Intent.createChooser(intent, "分享到"));
		theActivity.startActivityForResult(Intent.createChooser(intent, "分享到"), MSG.REQUEST_CODE_SHARE);
	}

	private ArrayList<Uri> getUriListForImages(SparseArray<String> listImagePath)
	{
		if (null == listImagePath || 0 >= listImagePath.size())
		{
			return null;
		}

		ArrayList<Uri> myList = new ArrayList<Uri>();

		File imageFile = null;
		ContentValues values = null;
		for (int i = 0; i < listImagePath.size(); ++i)
		{
			imageFile = new File(listImagePath.get(i));
			values = new ContentValues(7);
			values.put(Images.Media.TITLE, imageFile.getName());
			values.put(Images.Media.DISPLAY_NAME, imageFile.getName());
			values.put(Images.Media.DATE_TAKEN, new Date().getTime());
			values.put(Images.Media.MIME_TYPE, "image/jpeg");
			values.put(Images.ImageColumns.BUCKET_ID, imageFile.hashCode());
			values.put(Images.ImageColumns.BUCKET_DISPLAY_NAME, imageFile.getName());
			values.put("_data", listImagePath.get(i));
			ContentResolver contentResolver = theActivity.getApplicationContext().getContentResolver();
			Uri uri = contentResolver.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
			myList.add(uri);
			imageFile = null;
			values = null;
		}
		return myList;
	}

}
