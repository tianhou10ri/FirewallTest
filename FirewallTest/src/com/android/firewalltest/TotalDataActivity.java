package com.android.firewalltest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TotalDataActivity extends Activity {

	private GraphicalView chart;
	private Timer timer = new Timer();
	private TimerTask task;
	private Float addY;
	private String addX;
	String[] xkedu = new String[7];// x�����ݻ���
	Double[] ycache = new Double[7];
	// private final static int SERISE_NR = 1; //��������
	private XYSeries series;// ������յ�һ���ټ���һ��
	private XYMultipleSeriesDataset dataset1;// xy������Դ
	private XYMultipleSeriesRenderer render;
	SimpleDateFormat datayear = new SimpleDateFormat("yyyy");
	SimpleDateFormat datamonth = new SimpleDateFormat("MM");
	SimpleDateFormat dataday = new SimpleDateFormat("dd");
	Handler handler2;

	private long total = 1024;

	private Button example;
	private TextView totaldata;

	private int id_number_r = 0, id_number_t = 0;
	public static final String RXG = "rxg";
	public static final String TXG = "txg";
	public static final String RX = "rx";
	public static final String TX = "tx";
	public static final String SHUTDOWN = "d";
	public static final String NORMAL = "n";
	public static final String RX3G = "3G��������";
	public static final String TX3G = "3G�ϴ�����";
	public static final String RXT = "����������";
	public static final String TXT = "�ϴ�������";
	public static final String flag = "first";
	public static final String flagname = "nomber1";
	public static boolean isLog = false;
	private DataSupport minsert = new DataSupport(this);
	private Calendar calendar = Calendar.getInstance();
	private Calendar mcalendar = Calendar.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_total_data);

		example = (Button) findViewById(R.id.example_data_button);
		example.setOnClickListener(openexample);

		totaldata = (TextView) findViewById(R.id.total_data_textView1);

		datainsert();
		showdata();

		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.liuliangzhexian);
		chart = ChartFactory.getLineChartView(this, getdemodataset(),
				getdemorenderer());
		linearLayout.removeAllViews();// ��remove��add����ʵ��ͳ��ͼ����
		linearLayout.addView(chart, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		handler2 = new Handler() {
			public void handleMessage(Message msg) {
				updatechart();
			}

		};
		Message msg = new Message();
		msg.what = 200;
		handler2.sendMessage(msg);

	}

	private OnClickListener openexample = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// ����һ��intent����

			Intent intent = new Intent();
			// ָ��ԭ����class��Ҫ������class
			intent.setClass(TotalDataActivity.this, ItemDataActivity.class);
			// ��������һ���µ�Activity
			startActivity(intent);
			// �ر�ԭ����Activity
			// MainActivity.this.finish();
		}
	};

	/*
	 * ����˵������ʾ�ܵ���������
	 */
	private void showdata() {

		long grx = 0, gtx = 0, rx = 0, tx = 0;
		Cursor rcursor = minsert.selectNow(RX);
		Cursor rdaycursor = minsert.selectday(RX, NORMAL);
		Cursor tcursor = minsert.selectNow(TX);
		Cursor tdaycursor = minsert.selectday(TX, NORMAL);
		if (rcursor.moveToNext()) {
			int rnumbor = rcursor.getColumnIndex("liuliang");
			grx = rcursor.getLong(rnumbor);
			//while (rdaycursor.moveToNext()) {
			//	int rnumborday = rdaycursor.getColumnIndex("liuliang");
			//	grx = grx + rdaycursor.getLong(rnumborday);
			//}

		}
		if (tcursor.moveToNext()) {
			int tnumbor = tcursor.getColumnIndex("liuliang");
			gtx = tcursor.getLong(tnumbor);
			//while (tdaycursor.moveToNext()) {
			//	int tnumborday = tdaycursor.getColumnIndex("liuliang");
			//	gtx = gtx + tdaycursor.getLong(tnumborday);
			//}

		}
		long g_total = grx + gtx;

		long temp = g_total;

		if (temp < 1024) {
			totaldata.setText(temp + "B");
		} else if ((temp = temp / 1024) < 1024) {
			totaldata.setText(temp + "KB");
		} else if ((temp = temp / 1024) < 1024) {
			totaldata.setText(temp + "MB");
		}
		rcursor.close();
		rdaycursor.close();
		tcursor.close();
		tdaycursor.close();
		

	}

	/**
	 * 
	 * ����˵���������ݲ��뵽ϵͳ���ݿ���
	 * 
	 */
	private void datainsert() {
		// ��ȡͨ��Mobile�����յ����ֽ�����������Android123��ʾ��Ҳ�����WiFi
		long g3_down_total = TrafficStats.getMobileRxBytes();
		// Mobile���͵����ֽ���
		long g3_up_total = TrafficStats.getMobileTxBytes();
		// ��ȡ�ܵĽ����ֽ���������Mobile��WiFi��
		long mrdown_total = TrafficStats.getTotalRxBytes();
		// �ܵķ����ֽ���������Mobile��WiFi��
		long mtup_total = TrafficStats.getTotalTxBytes();
		// ���wifi�Ƿ����
		//WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		//ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		//NetworkInfo info = connect.getActiveNetworkInfo();
		//if (info != null) {
			//if (wifi.isWifiEnabled()) {
				minsert.insertNow(mrdown_total, RX, RXT, NORMAL);
				minsert.insertNow(mtup_total, TX, TXT, NORMAL);
				minsert.insertNow(g3_down_total, RXG, RX3G, NORMAL);
				minsert.insertNow(g3_up_total, TXG, TX3G, NORMAL);
			//}
			/*if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
				minsert.insertNow(g3_down_total, RXG, RX3G, NORMAL);
				minsert.insertNow(g3_up_total, TXG, TX3G, NORMAL);
			}*/
		//}
	}

	/*
	 * ��ȡʱ��������������
	 */
	private double serch_result(String date_start_textview,
			String date_over_textview) {
		double g3down = 0, g3up = 0, g3total = 0, rxdown = 0, txup = 0, alltotal = 0;
		String startdate =date_start_textview;
		String overdate = date_over_textview;
		Cursor rst = minsert.selectBettweenstart(startdate, overdate, RX);
		Cursor rov = minsert.selectBettweenstop(startdate, overdate, RX);
		Cursor tst = minsert.selectBettweenstart(startdate, overdate, TX);
		Cursor tov = minsert.selectBettweenstop(startdate, overdate, TX);
		Cursor rshutdown = minsert.selectbetweenday(RX, SHUTDOWN, startdate,
				overdate);
		Cursor tshutdown = minsert.selectbetweenday(TX, SHUTDOWN, startdate,
				overdate);
		if (rst.moveToNext()) {
			int number6 = rst.getColumnIndex("liuliang");
			rxdown = rst.getLong(number6);
			int number_id_rs = rst.getColumnIndex("id");
			id_number_r = rst.getInt(number_id_rs);
			if (rov.moveToNext()) {
				int number_id_ro = rov.getColumnIndex("id");
				id_number_t = rst.getInt(number_id_ro);
				if (id_number_r == id_number_t) {
				} else {
					int number7 = rov.getColumnIndex("liuliang");
					rxdown = rov.getLong(number7);// - rxdown;
				}
			}
			while (rshutdown.moveToNext()) {
				int number8 = rshutdown.getColumnIndex("liuliang");
				rxdown = rxdown + rshutdown.getLong(number8);
			}
			rxdown = rxdown / 1024 / 1024;

		}
		if (tst.moveToNext()) {
			int number9 = tst.getColumnIndex("liuliang");
			txup = tst.getLong(number9);
			int number_id_ts = tst.getColumnIndex("id");
			id_number_r = tst.getInt(number_id_ts);
			if (tov.moveToNext()) {
				int number_id_to = tst.getColumnIndex("id");
				id_number_t = tst.getInt(number_id_to);
				if (id_number_r == id_number_t) {
				} else {
					int number10 = tov.getColumnIndex("liuliang");
					txup = tov.getLong(number10) ;//- txup;
				}
			}
			while (tshutdown.moveToNext()) {
				int number11 = tshutdown.getColumnIndex("liuliang");
				txup = txup + tshutdown.getLong(number11);
			}
			txup = txup / 1024 / 1024;

		}
		alltotal = rxdown + txup;

		return (double)alltotal;
	}

	// ��������ͼ
	private void updatechart() {
		String datastart, datastop;
		// �жϵ�ǰ�㼯�е����ж��ٵ㣬��Ϊ��Ļ�ܹ�ֻ������5�������Ե���������5ʱ��������Զ��5
		/*
		 * int length=series.getItemCount(); int a=length; if(length>7){
		 * length=7; }
		 */
		/*
		 * try { if(guangzhi2.getText().toString()!=null){ addY =
		 * Float.valueOf(guangzhi2.getText().toString());//Ҫ��Ҫ�ж���˵ } } catch
		 * (NumberFormatException e) { e.printStackTrace(); }
		 */
		// ��ȡÿ�����Xֵ

		addX = dataday.format(new java.util.Date());
		int dayx = Integer.parseInt(addX);
		String month = datamonth.format(new java.util.Date());
		String year = datayear.format(new java.util.Date());

		render.setYAxisMax(10);// ����y��ķ�Χ
		render.setYAxisMin(0);
		
		dataset1.removeSeries(series);
		if (dayx < 7) {
			for (int j = 1; j < dayx; j++) {
				xkedu[j - 1] = month + "-0" + j;
				int k=j-1;
				if(k==0){
					k=1;
				}
				datastart = year + "-" + month + "-0" + k;
				datastop = year + "-" + month + "-0" + j;
				ycache[j - 1] = serch_result(datastart, datastop);
			}
			
			series.clear();
			for (int i = 0; i < dayx - 1; i++) {
				series.add(i + 1, ycache[i]);// ��һ����������ڼ����㣬Ҫ����������еĵ�һ��������Ӧ
				render.addXTextLabel(i + 1, xkedu[i]);

			}
		} else {
			for (int j = 1; j < 8; j++) {
				int t =dayx-7+j;

				if (t < 10) {
					xkedu[j - 1] = month + "-0" + t;
					datastart = year + "-" + month + "-0" + (t-1);
					datastop = year + "-" + month + "-0" + t;
				} else {
					if(t==10){
						xkedu[j - 1] = month + "-" + t;
						datastart = year + "-" + month + "-0" + (t-1);
						datastop = year + "-" + month + "-" + t;
					}
					else{
						xkedu[j - 1] = month + "-" + t;
						datastart = year + "-" + month + "-" + (t-1);
						datastop = year + "-" + month + "-" + t;
					}
				}

				ycache[j - 1] = serch_result(datastart, datastop);
			}

			series.clear();
			for (int i = 0; i < 7; i++) {
				series.add(i + 1, ycache[i]);// ��һ����������ڼ����㣬Ҫ����������еĵ�һ��������Ӧ
				//log.i("liuliang",""+ycache[i]);
				render.addXTextLabel(i + 1, xkedu[i]);

			}
		}
		// �����ݼ�������µĵ㼯
		dataset1.addSeries(series);
		// ��ͼ���£�û����һ�������߲�����ֶ�̬
		chart.invalidate();

		
		
	}

	private XYMultipleSeriesRenderer getdemorenderer() {
		// TODO Auto-generated method stub
		render = new XYMultipleSeriesRenderer();
		render.setChartTitle("����ʹ������");
		render.setChartTitleTextSize(10);// ��������ͼ��������ֵĴ�С
		render.setAxisTitleTextSize(8);// ������������ֵĴ�С
		render.setAxesColor(Color.BLACK);
		render.setXTitle("ʱ��");
		render.setYTitle("����/MB");

		render.setLabelsTextSize(16);// ������̶����ֵĴ�С
		render.setLabelsColor(Color.BLACK);
		render.setXLabelsColor(Color.BLACK);
		render.setYLabelsColor(0, Color.BLACK);
		render.setLegendTextSize(15);// ����ͼ�����ִ�С
		render.setShowLegend(false);// ��ʾ����ʾ���������ã��ǳ�����

		XYSeriesRenderer r = new XYSeriesRenderer();// ������ɫ�͵�����
		r.setColor(Color.RED);
		r.setPointStyle(PointStyle.CIRCLE);
		r.setFillPoints(true);
		r.setChartValuesSpacing(3);

		render.addSeriesRenderer(r);
		render.setYLabelsAlign(Align.RIGHT);// �̶�ֵ����ڿ̶ȵ�λ��
		render.setShowGrid(true);// ��ʾ����
		render.setYAxisMax(10);// ����y��ķ�Χ
		render.setYAxisMin(0);
		render.setYLabels(5);// ���ߵȷ�

		render.setInScroll(true);
		render.setLabelsTextSize(7);
		render.setLabelsColor(Color.BLACK);
		// render.getSeriesRendererAt(0).setDisplayChartValues(true);
		// //��ʾ�����ϵ����ֵ
		render.setPanEnabled(false, false);// ��ֹ������϶�
		render.setPointSize(5f);// ���õ�Ĵ�С(ͼ����ʾ�ĵ�Ĵ�С��ͼ���е�Ĵ�С���ᱻ����)
		render.setMargins(new int[] { 20, 20, 20, 20 }); // ����ͼ�����ܵ�����
		render.setMarginsColor(Color.WHITE);
		render.setXLabels(0);// ȡ��X���������zjk,ֻ���Լ�����������ǲ���Ϊ��ֵ

		return render;
	}

	private XYMultipleSeriesDataset getdemodataset() {
		// TODO Auto-generated method stub
		dataset1 = new XYMultipleSeriesDataset();// xy������Դ
		series = new XYSeries("���� ");// ���������ʾ�����õģ��Բ���ʾ������render����
		// �����൱�ڳ�ʼ������ʼ��������������ݣ���Ϊ���������ӵ�һ�����ݵĻ���
		// ������ʹ��һ�����ݺͶ�ʱ���и��µĵڶ������ݵ�ʱ������Ϊ���룬���������������
		// �������һ�θ���������ݣ������Ļ��൱�ڿ�ʼ��ʱ��Ͱ��������ȫ���ӽ�ȥ�ˣ��������ݵ�ʱ���ǲ�׼ȷ���߼����Ϊ����
		// for(int i=0;i<5;i++)
		// series.add(1, Math.random()*10);//������date�������ͣ��������漴���ȴ�����

		dataset1.addSeries(series);
		return dataset1;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.total_data, menu);
		return true;
	}

	
	
}
