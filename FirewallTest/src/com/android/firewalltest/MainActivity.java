package com.android.firewalltest;

import com.android.firewalltest.Api.DroidApp;

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
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		total_data_button = (Button) findViewById(R.id.main_data_button);
		secret_button = (Button) findViewById(R.id.main_secret_button);

		total_data_button.setOnClickListener(opendata);
		secret_button.setOnClickListener(opensecret);
	}

	private OnClickListener opendata = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// ����һ��intent����

			Intent intent = new Intent();
			// ָ��ԭ����class��Ҫ������class
			intent.setClass(MainActivity.this, TotalDataActivity.class);
			// ��������һ���µ�Activity
			startActivity(intent);
			// �ر�ԭ����Activity
			// MainActivity.this.finish();
		}
	};
	
	private OnClickListener opensecret = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// ����һ��intent����

			Intent intent = new Intent();
			// ָ��ԭ����class��Ҫ������class
			intent.setClass(MainActivity.this, SecretActivity.class);
			// ��������һ���µ�Activity
			startActivity(intent);
			// �ر�ԭ����Activity
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
