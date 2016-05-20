package com.android.firewalltest;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TotalDataActivity extends Activity {
	
	
	private long total=1024;

	private Button example;

	private TextView totaldata;

	private DataSupport minsert = new DataSupport(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_total_data);
		
		
		example = (Button) findViewById(R.id.example_data_button);
		example.setOnClickListener(openexample);
		
		totaldata=(TextView)findViewById(R.id.total_data_textView1);
		
		//datainsert();
		showdata();
	}

	private OnClickListener openexample = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 创建一个intent对象

			Intent intent = new Intent();
			// 指定原本的class和要启动的class
			intent.setClass(TotalDataActivity.this, ItemDataActivity.class);
			// 调用另外一个新的Activity
			startActivity(intent);
			// 关闭原本的Activity
			// MainActivity.this.finish();
		}
	};

	private void showdata() {
		
		
		//String startdate;
		//String overdate;
		
		//Cursor r3gst = minsert.selectBettweenstart(startdate, overdate, "RX3G");
		//Cursor r3gov = minsert.selectBettweenstop(startdate, overdate, "RX3G");
		//Cursor t3gst = minsert.selectBettweenstart(startdate, overdate, "TX3G");
		//Cursor t3gov = minsert.selectBettweenstop(startdate, overdate, "TX3G");
		
		// 获取通过Mobile连接收到的字节总数，这里Android123提示大家不包含WiFi
		long g3_down_total = TrafficStats.getMobileRxBytes();
		// Mobile发送的总字节数
		long g3_up_total = TrafficStats.getMobileTxBytes();
		// 获取总的接受字节数，包含Mobile和WiFi等
		long mrdown_total = TrafficStats.getTotalRxBytes();
		// 总的发送字节数，包含Mobile和WiFi等
		long mtup_total = TrafficStats.getTotalTxBytes();
		
		total=mrdown_total+mtup_total;
		
		long temp = total;
		
		if(temp<1024){
			totaldata.setText(temp+"B");
		}else if((temp=temp/1024)<1024){
			totaldata.setText(temp+"KB");
		}else if((temp=temp/1024)<1024){
			totaldata.setText(temp+"MB");
		}
		
		
		
		

	}

	/**
	 * 
	 * 方法说明：把数据插入到系统数据库中
	 * 
	 */
	/**private void datainsert() {
		// 获取通过Mobile连接收到的字节总数，这里Android123提示大家不包含WiFi
		long g3_down_total = TrafficStats.getMobileRxBytes();
		// Mobile发送的总字节数
		long g3_up_total = TrafficStats.getMobileTxBytes();
		// 获取总的接受字节数，包含Mobile和WiFi等
		long mrdown_total = TrafficStats.getTotalRxBytes();
		// 总的发送字节数，包含Mobile和WiFi等
		long mtup_total = TrafficStats.getTotalTxBytes();
		// 检测wifi是否存在
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connect.getActiveNetworkInfo();
		if (info != null) {
			if (wifi.isWifiEnabled()) {
				minsert.insertNow(mrdown_total, "RX", "RXT", "NORMAL");
				minsert.insertNow(mtup_total, "TX", "TXT", "NORMAL");
				minsert.insertNow(g3_down_total, "RXG", "RX3G", "NORMAL");
				minsert.insertNow(g3_up_total, "TXG", "TX3G", "NORMAL");
			}
			if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
				minsert.insertNow(g3_down_total, "RXG", "RX3G", "NORMAL");
				minsert.insertNow(g3_up_total, "TXG", "TX3G", "NORMAL");
			}
		}
	}
	*/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.total_data, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
