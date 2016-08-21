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
	String[] xkedu = new String[7];// x轴数据缓冲
	Double[] ycache = new Double[7];
	// private final static int SERISE_NR = 1; //曲线数量
	private XYSeries series;// 用来清空第一个再加下一个
	private XYMultipleSeriesDataset dataset1;// xy轴数据源
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
	public static final String RX3G = "3G下载流量";
	public static final String TX3G = "3G上传流量";
	public static final String RXT = "下载总流量";
	public static final String TXT = "上传总流量";
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
		linearLayout.removeAllViews();// 先remove再add可以实现统计图更新
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

	/*
	 * 方法说明：显示总的流量数据
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
	 * 方法说明：把数据插入到系统数据库中
	 * 
	 */
	private void datainsert() {
		// 获取通过Mobile连接收到的字节总数，这里Android123提示大家不包含WiFi
		long g3_down_total = TrafficStats.getMobileRxBytes();
		// Mobile发送的总字节数
		long g3_up_total = TrafficStats.getMobileTxBytes();
		// 获取总的接受字节数，包含Mobile和WiFi等
		long mrdown_total = TrafficStats.getTotalRxBytes();
		// 总的发送字节数，包含Mobile和WiFi等
		long mtup_total = TrafficStats.getTotalTxBytes();
		// 检测wifi是否存在
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
	 * 获取时间间隔的流量数据
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

	// 更新折线图
	private void updatechart() {
		String datastart, datastop;
		// 判断当前点集中到底有多少点，因为屏幕总共只能容纳5个，所以当点数超过5时，长度永远是5
		/*
		 * int length=series.getItemCount(); int a=length; if(length>7){
		 * length=7; }
		 */
		/*
		 * try { if(guangzhi2.getText().toString()!=null){ addY =
		 * Float.valueOf(guangzhi2.getText().toString());//要不要判断再说 } } catch
		 * (NumberFormatException e) { e.printStackTrace(); }
		 */
		// 获取每个点的X值

		addX = dataday.format(new java.util.Date());
		int dayx = Integer.parseInt(addX);
		String month = datamonth.format(new java.util.Date());
		String year = datayear.format(new java.util.Date());

		render.setYAxisMax(10);// 设置y轴的范围
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
				series.add(i + 1, ycache[i]);// 第一个参数代表第几个点，要与下面语句中的第一个参数对应
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
				series.add(i + 1, ycache[i]);// 第一个参数代表第几个点，要与下面语句中的第一个参数对应
				//log.i("liuliang",""+ycache[i]);
				render.addXTextLabel(i + 1, xkedu[i]);

			}
		}
		// 在数据集中添加新的点集
		dataset1.addSeries(series);
		// 视图更新，没有这一步，曲线不会呈现动态
		chart.invalidate();

		
		
	}

	private XYMultipleSeriesRenderer getdemorenderer() {
		// TODO Auto-generated method stub
		render = new XYMultipleSeriesRenderer();
		render.setChartTitle("流量使用曲线");
		render.setChartTitleTextSize(10);// 设置整个图表标题文字的大小
		render.setAxisTitleTextSize(8);// 设置轴标题文字的大小
		render.setAxesColor(Color.BLACK);
		render.setXTitle("时间");
		render.setYTitle("流量/MB");

		render.setLabelsTextSize(16);// 设置轴刻度文字的大小
		render.setLabelsColor(Color.BLACK);
		render.setXLabelsColor(Color.BLACK);
		render.setYLabelsColor(0, Color.BLACK);
		render.setLegendTextSize(15);// 设置图例文字大小
		render.setShowLegend(false);// 显示不显示在这里设置，非常完美

		XYSeriesRenderer r = new XYSeriesRenderer();// 设置颜色和点类型
		r.setColor(Color.RED);
		r.setPointStyle(PointStyle.CIRCLE);
		r.setFillPoints(true);
		r.setChartValuesSpacing(3);

		render.addSeriesRenderer(r);
		render.setYLabelsAlign(Align.RIGHT);// 刻度值相对于刻度的位置
		render.setShowGrid(true);// 显示网格
		render.setYAxisMax(10);// 设置y轴的范围
		render.setYAxisMin(0);
		render.setYLabels(5);// 分七等份

		render.setInScroll(true);
		render.setLabelsTextSize(7);
		render.setLabelsColor(Color.BLACK);
		// render.getSeriesRendererAt(0).setDisplayChartValues(true);
		// //显示折线上点的数值
		render.setPanEnabled(false, false);// 禁止报表的拖动
		render.setPointSize(5f);// 设置点的大小(图上显示的点的大小和图例中点的大小都会被设置)
		render.setMargins(new int[] { 20, 20, 20, 20 }); // 设置图形四周的留白
		render.setMarginsColor(Color.WHITE);
		render.setXLabels(0);// 取消X坐标的数字zjk,只有自己定义横坐标是才设为此值

		return render;
	}

	private XYMultipleSeriesDataset getdemodataset() {
		// TODO Auto-generated method stub
		dataset1 = new XYMultipleSeriesDataset();// xy轴数据源
		series = new XYSeries("流量 ");// 这个事是显示多条用的，显不显示在上面render设置
		// 这里相当于初始化，初始化中无需添加数据，因为如果这里添加第一个数据的话，
		// 很容易使第一个数据和定时器中更新的第二个数据的时间间隔不为两秒，所以下面语句屏蔽
		// 这里可以一次更新五个数据，这样的话相当于开始的时候就把五个数据全部加进去了，但是数据的时间是不准确或者间隔不为二的
		// for(int i=0;i<5;i++)
		// series.add(1, Math.random()*10);//横坐标date数据类型，纵坐标随即数等待更新

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
