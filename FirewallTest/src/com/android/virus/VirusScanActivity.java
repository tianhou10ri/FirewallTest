package com.android.virus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;




import com.android.firewalltest.R;
import com.android.firewalltest.R.layout;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class VirusScanActivity extends Activity {
	
	protected static final int SCAN_BENGIN = 100;
	protected static final int SCANNING = 101;
	protected static final int SCAN_FINISH = 102;
	private int total;
	private int process;
	private TextView mProcessTV;
	private PackageManager pm;
	private boolean flag;
	private boolean isStop;
	private TextView mScanAppTV;
	private Button mCancleBtn;
	private ImageView mScanningIcon;
	private RotateAnimation rani;
	private ListView mScanListView;
	private ScanVirusAdapter adapter;
	private List<ScanAppInfo> mScanAppInfos = new ArrayList<ScanAppInfo>();
	private SharedPreferences mSP;

	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {

			case SCAN_BENGIN:
				mScanAppTV.setText("��ʼ��ɱ��������...");
				break;

			case SCANNING:
				ScanAppInfo info = (ScanAppInfo) msg.obj;
				mScanAppTV.setText("����ɨ��: " + info.appName);
				int speed = msg.arg1;
				mProcessTV.setText((speed * 100 / total) + "%");
				mScanAppInfos.add(info);
				adapter.notifyDataSetChanged();
				mScanListView.setSelection(mScanAppInfos.size());
				break;
			case SCAN_FINISH:
				mScanAppTV.setText("ɨ����ɣ�");
				mScanningIcon.clearAnimation();
				mCancleBtn.setBackgroundResource(R.drawable.scan_complete);
				saveScanTime();
				break;
			}
		}
		private void saveScanTime() {
			Editor edit = mSP.edit();
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());		
			String currentTime=sdf.format(new Date());
			currentTime="�ϴβ�ɱ�� "+currentTime;
			edit.putString("lastVirusScan", currentTime);
			edit.commit();
		};
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_virus_scan);
		copyDB("antivirus.db");
		
		pm = getPackageManager();
		mSP = getSharedPreferences("config", MODE_PRIVATE);
		initView();
		scanVirus();
	}

	/**
	 * ɨ�財��
	 * */
	private void scanVirus() {
		flag = true;
		isStop = false;
		process = 0;
		mScanAppInfos.clear();
		new Thread() {

			public void run() {
				Message msg = Message.obtain();
				msg.what = SCAN_BENGIN;
				mHandler.sendMessage(msg);
				List<PackageInfo> installedPackages = pm
						.getInstalledPackages(0);
				total = installedPackages.size();
				for (PackageInfo info : installedPackages) {
					if (!flag) {
						isStop = true;
						return;
					}
					String apkpath = info.applicationInfo.sourceDir;
					// ����ȡ����ļ��� ������
					String md5info = MD5.getMd5(apkpath);
					String result = FindVirus.checkVirus(md5info);
					msg = Message.obtain();
					msg.what = SCANNING;
					ScanAppInfo scanInfo = new ScanAppInfo();
					if (result == null) {
						scanInfo.description = "ɨ�谲ȫ";
						scanInfo.isVirus = false;
					} else {
						scanInfo.description = result;
						scanInfo.isVirus = true;
					}
					process++;
					scanInfo.packagename = info.packageName;
					scanInfo.appName = info.applicationInfo.loadLabel(pm)
							.toString();
					scanInfo.appicon = info.applicationInfo.loadIcon(pm);
					msg.obj = scanInfo;
					msg.arg1 = process;
					mHandler.sendMessage(msg);

					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				msg = Message.obtain();
				msg.what = SCAN_FINISH;
				mHandler.sendMessage(msg);
			};
		}.start();
	}

	private void initView() {
		findViewById(R.id.rl_titlebar).setBackgroundColor(
				getResources().getColor(R.color.light_blue));
		ImageView mLeftImgv = (ImageView) findViewById(R.id.imgv_leftbtn);
		((TextView) findViewById(R.id.tv_title)).setText("������ɱ����");
		mLeftImgv.setOnClickListener((OnClickListener) this);
		mLeftImgv.setImageResource(R.drawable.back);
		mProcessTV = (TextView) findViewById(R.id.tv_scanprocess);
		mScanAppTV = (TextView) findViewById(R.id.tv_scansapp);
		mCancleBtn = (Button) findViewById(R.id.btn_canclescan);
		mCancleBtn.setOnClickListener((OnClickListener) this);
		mScanListView = (ListView) findViewById(R.id.lv_scanapps);
		adapter = new ScanVirusAdapter(mScanAppInfos, this);
		mScanListView.setAdapter(adapter);
		mScanningIcon = (ImageView) findViewById(R.id.imgv_scanningicon);
		startAnim();
	}

	private void startAnim() {
		if (rani == null) {
			rani = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF,
					0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		}
		rani.setRepeatCount(Animation.INFINITE);
		rani.setDuration(2000);
		mScanningIcon.startAnimation(rani);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imgv_leftbtn:
			finish();
			break;
		case R.id.btn_canclescan:
			if (process == total & process > 0) {
				// ɨ�������
				finish();
			} else if (process > 0 & process < total & isStop == false) {
				mScanningIcon.clearAnimation();
				// ȡ��ɨ��
				flag = false;
				// ��������ͼƬ
				mCancleBtn.setBackgroundResource(R.drawable.restart_scan_btn);
			} else if (isStop) {
				startAnim();
				// ����ɨ��
				scanVirus();
				// ��������ͼƬ
				mCancleBtn.setBackgroundResource(R.drawable.cancle_scan_btn_selector);
			}
			break;
		}
	}

	@Override
	protected void onDestroy() {
		flag = false;
		super.onDestroy();
	}
   
	
	
	
	private void copyDB(final String dbname){
		new Thread(){
			public void run(){
				try {
					File file=new File(getFilesDir(),dbname);
					if(file.exists()&&file.length()>0){
						Log.i("VirusScanActivity","���ݿ��Ѵ���");
						return;
					}
					InputStream infile=getAssets().open(dbname);
					FileOutputStream fcop=openFileOutput(dbname, MODE_PRIVATE);
					byte[] buffer=new byte[1024];
					int len=0;
					while ((len = infile.read(buffer)) != -1) {
						fcop.write(buffer, 0, len);
		                
		            }
					infile.close();
					fcop.close();
					
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			};
		}.start();
	}
	
	
	
}
