package com.android.firewalltest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ItemDataActivity extends Activity {

	private Button applyrule;

	private ListView showListview;
	public List<HashMap<String, Object>> itemdata;

	// Cached applications


	
	/** special application UID used to indicate "any application" */
	public static final int SPECIAL_UID_ANY = -10;
	/** root script filename */
	private static final String SCRIPT_FILE = "firewall.sh";

	// Preferences
	public static final String PREFS_NAME = "FireWallPrefs";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_data);

		showListview = (ListView) findViewById(R.id.example_data_listView);

		show_data_onlistviw();

		applyrule = (Button) findViewById(R.id.use_flowset_button);
		applyrule.setOnClickListener(useandsaverule);
	}

	private void show_data_onlistviw() {

		itemdata = getData();
		AdaptforApps adapter = new AdaptforApps(this, itemdata);

		showListview.setAdapter(adapter);

	}

	private List<HashMap<String, Object>> getData() {

		PackageManager pckMan = getPackageManager();
		List<PackageInfo> packs = pckMan.getInstalledPackages(0);
		ArrayList<HashMap<String, Object>> item = new ArrayList<HashMap<String, Object>>();

		for (PackageInfo p : packs) {
			if ((p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
					&& (p.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
				int appid = p.applicationInfo.uid;
				long rxdata = TrafficStats.getUidRxBytes(appid);
				rxdata = rxdata / 1024;
				long txdata = TrafficStats.getUidTxBytes(appid);
				txdata = txdata / 1024;
				long data_total = rxdata + txdata;

				final SharedPreferences prefs = getSharedPreferences(
						PREFS_NAME, 0);
				//将各个应用的网络连接设置按照Uid存储到SharedPreferences中

				String Uids_gprs = prefs.getString(appid + "gprs", "false");
				String Uids_wifi = prefs.getString(appid + "wifi", "false");

				HashMap<String, Object> items = new HashMap<String, Object>();
				Drawable drawable = p.applicationInfo
						.loadIcon(getPackageManager());

				items.put("uid", appid);
				items.put("appsname",
						p.applicationInfo.loadLabel(getPackageManager())
								.toString());
				items.put("appsdata", data_total + "");

				items.put("app_gprs", Uids_gprs);
				items.put("app_wifi", Uids_wifi);
				item.add(items);

			}
		}
		return item;
	}

	public class AdaptforApps extends BaseAdapter {
		private Context context;
		private List<HashMap<String, Object>> item;
		private boolean t1 = true, r1 = false;

		public AdaptforApps(Context context, List<HashMap<String, Object>> item) {
			this.context = context;
			this.item = item;
		}

		@Override
		public int getCount() {
			return item.size();
		}

		@Override
		public Object getItem(int position) {
			return item.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Datalist data = new Datalist();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.list_app_item, null);

			data.appname = (TextView) convertView
					.findViewById(R.id.item_name_textView);
			data.appdata = (TextView) convertView
					.findViewById(R.id.item_data_textView);
			data.checkgprs = (CheckBox) convertView
					.findViewById(R.id.itemcheckbox_gprs);
			data.checkwifi = (CheckBox) convertView
					.findViewById(R.id.itemcheckbox_wifi);

			data.appsuid=item.get(position).get("uid").toString();
			data.appname.setText(item.get(position).get("appsname").toString());
			data.appdata.setText(item.get(position).get("appsdata").toString()+"MB");

			data.checkgprs.setChecked(item.get(position).get("app_gprs")
					.equals("true"));
			data.checkwifi.setChecked(item.get(position).get("app_wifi")
					.equals("true"));

			data.checkgprs
					.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton arg0,
								boolean arg1) {
							// TODO Auto-generated method stub
							final SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
							Editor edit = prefs.edit();
							if (arg1) {
								
								edit.putString(data.appsuid + "gprs", "true");
								edit.commit();
							}else{
								edit.putString(data.appsuid + "gprs", "false");
								edit.commit();
							}
							itemdata = getData();
						}

					});

			data.checkwifi
					.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton arg0,
								boolean arg1) {
							// TODO Auto-generated method stub
							final SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
							Editor edit = prefs.edit();
							if (arg1) {
								
								edit.putString(data.appsuid + "wifi", "true");
								edit.commit();

							}else{
								edit.putString(data.appsuid + "wifi", "false");
								edit.commit();
							}
							itemdata = getData();
						}

					});

			return convertView;

		}

		private class Datalist {
			public ImageView mimage;
			public String appsuid;
			public TextView appname, appdata;
			public CheckBox checkgprs, checkwifi;

		}
	}

	private OnClickListener useandsaverule = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			if (!MyApi.hasRootAccess(ItemDataActivity.this, true)) return;
			MyApi.purgeIptables(ItemDataActivity.this, true);
			if(MyApi.applyIptablesRules(ItemDataActivity.this, true)){
				Toast.makeText(ItemDataActivity.this, "Rules applied with success", Toast.LENGTH_SHORT).show();
			}
			
		}
	};
}
