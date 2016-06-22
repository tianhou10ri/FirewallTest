package com.android.firewalltest;

import com.android.firewalltest.Api.DroidApp;
import com.android.photostore.Gallery;
import com.android.virus.VirusScanActivity;
import com.android.firewalltest.DialogShuRuMiMa;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	private Button total_data_button;
	private Button secret_button;
	//private Button scanvirus_button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		total_data_button = (Button) findViewById(R.id.main_data_button);
		secret_button = (Button) findViewById(R.id.main_secret_button);
		//scanvirus_button = (Button) findViewById(R.id.scan_virus_button);

		total_data_button.setOnClickListener(opendata);
		secret_button.setOnClickListener(opensecret);
		//scanvirus_button.setOnClickListener(openscanvirus);
	}

	private OnClickListener opendata = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 创建一个intent对象

			Intent intent = new Intent();
			// 指定原本的class和要启动的class
			intent.setClass(MainActivity.this, TotalDataActivity.class);
			// 调用另外一个新的Activity
			startActivity(intent);
			// 关闭原本的Activity
			// MainActivity.this.finish();
		}
	};

	private OnClickListener opensecret = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 创建一个intent对象
			Intent intent = new Intent();
			// 指定原本的class和要启动的class
			intent.setClass(MainActivity.this, SecretActivity.class);
			// 调用另外一个新的Activity
			startActivity(intent);
			// 关闭原本的Activity
			// MainActivity.this.finish();
		}
	};

	private OnClickListener openscanvirus = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// 创建一个intent对象
			Intent intent = new Intent();
			// 指定原本的class和要启动的class
			intent.setClass(MainActivity.this, VirusScanActivity.class);
			// 调用另外一个新的Activity
			startActivity(intent);
			// 关闭原本的Activity
			// MainActivity.this.finish();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
