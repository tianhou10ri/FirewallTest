package com.android.firewalltest;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class SecretCameraActivity extends Activity {

	protected static final int CAMERA_RESULT = 0;
	SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");// 设置日期格式
	String Time = df.format(new Date());// new Date()为获取当前系统时间
	String imageFilePath = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + File.separator + Time + "mypicture.jpg";

	private ImageView imv;
	private Button take_picture;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_secret_camera);

		take_picture = (Button) findViewById(R.id.take_picture_button);
		take_picture.setOnClickListener(takepicture);

	}

	private OnClickListener takepicture = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			File imageFile = new File(imageFilePath);
			Uri imageFileUri = Uri.fromFile(imageFile);

			Intent i = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
			startActivityForResult(i, CAMERA_RESULT);

		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, intent);

		// 如果拍照成功
		if (resultCode == RESULT_OK) {

			// Bundle extras = intent.getExtras();
			// Bitmap bmp = (Bitmap)extras.get("data");

			imv = (ImageView) findViewById(R.id.imageView);

			// 取得屏幕的显示大小
			Display currentDisplay = getWindowManager().getDefaultDisplay();
			int dw = currentDisplay.getWidth();
			int dh = currentDisplay.getHeight();

			// 对拍出的照片进行缩放
			BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
			bmpFactoryOptions.inJustDecodeBounds = true;
			Bitmap bmp = BitmapFactory.decodeFile(imageFilePath,
					bmpFactoryOptions);

			int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight
					/ (float) dh);
			int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth
					/ (float) dw);

			if (heightRatio > 1 && widthRatio > 1) {

				if (heightRatio > widthRatio) {

					bmpFactoryOptions.inSampleSize = heightRatio;
				} else {
					bmpFactoryOptions.inSampleSize = widthRatio;
				}

			}

			bmpFactoryOptions.inJustDecodeBounds = false;
			bmp = BitmapFactory.decodeFile(imageFilePath, bmpFactoryOptions);

			imv.setImageBitmap(bmp);

			Code coding = new Code();
			coding.encode(imageFilePath);
			new File(imageFilePath).delete();

		}

	}

}
