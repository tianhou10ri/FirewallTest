package com.android.firewalltest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;



/**
 * 此代码仿照“http://www.eoeandroid.com/thread-171911-1-1.html”
 * javaapk.com提供测试
 * @author yand
 * 
 */
public class MyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		DataSupport minsert = new DataSupport(context);
		// 获取通过Mobile连接收到的字节总数，这里Android123提示大家不包含WiFi
		long g3_down_total = TrafficStats.getMobileRxBytes();
		// Mobile发送的总字节数
		long g3_up_total = TrafficStats.getMobileTxBytes();
		// 获取总的接受字节数，包含Mobile和WiFi等
		long mrdown_total = TrafficStats.getTotalRxBytes();
		// 总的发送字节数，包含Mobile和WiFi等
		long mtup_total = TrafficStats.getTotalTxBytes();

		minsert.insertNow(g3_down_total, TotalDataActivity.RXG, TotalDataActivity.RX3G,
				TotalDataActivity.SHUTDOWN);
		minsert.insertNow(g3_up_total, TotalDataActivity.TXG, TotalDataActivity.TX3G,
				TotalDataActivity.SHUTDOWN);
		minsert.insertNow(mrdown_total, TotalDataActivity.RX, TotalDataActivity.RXT,
				TotalDataActivity.SHUTDOWN);
		minsert.insertNow(mtup_total, TotalDataActivity.TX, TotalDataActivity.TXT,
				TotalDataActivity.SHUTDOWN);
		// if (MainActivity.isLog) {
		// Log.i("liuliang", "shutdown>>>>>>>>>start");
		// }
	}

}
