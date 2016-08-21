package com.android.firewalltest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;



/**
 * �˴�����ա�http://www.eoeandroid.com/thread-171911-1-1.html��
 * javaapk.com�ṩ����
 * @author yand
 * 
 */
public class MyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		DataSupport minsert = new DataSupport(context);
		// ��ȡͨ��Mobile�����յ����ֽ�����������Android123��ʾ��Ҳ�����WiFi
		long g3_down_total = TrafficStats.getMobileRxBytes();
		// Mobile���͵����ֽ���
		long g3_up_total = TrafficStats.getMobileTxBytes();
		// ��ȡ�ܵĽ����ֽ���������Mobile��WiFi��
		long mrdown_total = TrafficStats.getTotalRxBytes();
		// �ܵķ����ֽ���������Mobile��WiFi��
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
