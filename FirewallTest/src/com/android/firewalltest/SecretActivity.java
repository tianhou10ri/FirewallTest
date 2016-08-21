package com.android.firewalltest;

import java.io.File;

import com.android.photostore.Constant;
import com.android.photostore.Constant.ImageFolderInfo;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SecretActivity extends Activity {

	private static final int UPDATELIST = 0;
	private Button gallery_button;
	private Button camera_button;
	private Button delete_button;
	final private String mCardPath = Environment.getExternalStorageDirectory().getPath();
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_secret);

		gallery_button = (Button) findViewById(R.id.serect_galler_button);
		camera_button = (Button) findViewById(R.id.serect_camera_button);
		delete_button=(Button)findViewById(R.id.delete_button);

		gallery_button.setOnClickListener(opengallery);
		camera_button.setOnClickListener(opencamera);
		delete_button.setOnClickListener(deletepicture);
	}

	private OnClickListener opengallery = new OnClickListener() {
		@Override
		public void onClick(View v) {

			mima();
		}
	};

	private void mima() {
		new DialogShuRuMiMa(this, this, "");

	}

	private OnClickListener opencamera = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			// 创建一个intent对象

			Intent intent = new Intent();
			// 指定原本的class和要启动的class
			intent.setClass(SecretActivity.this, SecretCameraActivity.class);
			// 调用另外一个新的Activity
			startActivity(intent);
			// 关闭原本的Activity
			// MainActivity.this.finish();

		}
	};
	
	
	private OnClickListener deletepicture=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			deletFiles(mCardPath+ File.separator + "DrawEncode");
			showMsg("删除图片成功");
		}
	}; 
	
	private void deletFiles(String path) {
		Code code =new Code();
		File f = new File(path);
		File[] files = f.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				final File ff = files[i];
				if (ff.isDirectory()) {
					deletFiles(ff.getPath());
				} else {
					String fName = ff.getName();
					if (fName.indexOf(".") > -1) {
						String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toUpperCase();
						/*if (Constant.getExtens().contains(end)) {
							ff.delete();
								
						}*/
						if(end.equals("JPG")){
							ff.delete();
						}
					}
				}
			}
		}
		
		
	}
	
	public void showMsg(String msg) {
		Toast toast=Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
		toast.show();
	}

}
