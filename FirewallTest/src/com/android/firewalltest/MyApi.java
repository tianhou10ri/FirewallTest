package com.android.firewalltest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Comparator;


import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;


public final class MyApi{
	
	private static boolean hasroot = true;
	
	private static final String SCRIPT_FILE = "firewall.sh";
	public static final String PREFS_NAME = "FireWallPrefs";
	
	/** special application UID used to indicate "any application" */
	public static final int SPECIAL_UID_ANY	= -10;

	/**
	 * Create the generic shell script header used to determine which iptables binary to use.
	 * @param ctx context
	 * @return script header
	 */
	private static String scriptHeader(Context ctx) {
		final String dir = ctx.getCacheDir().getAbsolutePath();
		return "" +
			"IPTABLES=iptables\n" +
			"BUSYBOX=busybox\n" +
			"GREP=grep\n" +
			"ECHO=echo\n" +
			"# Try to find busybox\n" +
			"if " + dir + "/busybox_g1 --help >/dev/null 2>/dev/null ; then\n" +
			"	BUSYBOX="+dir+"/busybox_g1\n" +
			"	GREP=\"$BUSYBOX grep\"\n" +
			"	ECHO=\"$BUSYBOX echo\"\n" +
			"elif busybox --help >/dev/null 2>/dev/null ; then\n" +
			"	BUSYBOX=busybox\n" +
			"elif /system/xbin/busybox --help >/dev/null 2>/dev/null ; then\n" +
			"	BUSYBOX=/system/xbin/busybox\n" +
			"elif /system/bin/busybox --help >/dev/null 2>/dev/null ; then\n" +
			"	BUSYBOX=/system/bin/busybox\n" +
			"fi\n" +
			"# Try to find grep\n" +
			"if ! $ECHO 1 | $GREP -q 1 >/dev/null 2>/dev/null ; then\n" +
			"	if $ECHO 1 | $BUSYBOX grep -q 1 >/dev/null 2>/dev/null ; then\n" +
			"		GREP=\"$BUSYBOX grep\"\n" +
			"	fi\n" +
			"	# Grep is absolutely required\n" +
			"	if ! $ECHO 1 | $GREP -q 1 >/dev/null 2>/dev/null ; then\n" +
			"		$ECHO The grep command is required. Firewall will not work.\n" +
			"		exit 1\n" +
			"	fi\n" +
			"fi\n" +
			"# Try to find iptables\n" +
			"if " + dir + "/iptables_g1 --version >/dev/null 2>/dev/null ; then\n" +
			"	IPTABLES="+dir+"/iptables_g1\n" +
			"elif " + dir + "/iptables_n1 --version >/dev/null 2>/dev/null ; then\n" +
			"	IPTABLES="+dir+"/iptables_n1\n" +
			"fi\n" +
			"";
	}
	
	
	
	/**
     * Purge and re-add all rules (internal implementation).
     * @param ctx application context (mandatory)
     * @param uidsWifi list of selected UIDs for WIFI to allow or disallow (depending on the working mode)
     * @param uids3g list of selected UIDs for 2G/3G to allow or disallow (depending on the working mode)
     * @param showErrors indicates if errors should be alerted
     */
	private static boolean applyIptablesRulesImpl(Context ctx, List<Integer> uidsWifi, List<Integer> uids3g, boolean showErrors) {
		if (ctx == null) {
			return false;
		}
		assertBinaries(ctx, showErrors);
		final String ITFS_WIFI[] = {"tiwlan+", "wlan+", "eth+"};
		final String ITFS_3G[] = {"rmnet+","pdp+","ppp+","uwbr+"};
		final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, 0);
		


    	final StringBuilder script = new StringBuilder();
		try {
			int code;
			script.append(scriptHeader(ctx));
			script.append("" +
				"$IPTABLES --version || exit 1\n" +
				"# Create the firewfirechains if necessary\n" +
				"$IPTABLES -L firedwall >/dev/null 2>/dev/null || $IPTABLES --new firewall || exit 2\n" +
				"$IPTABLES -L firedwall-3g >/dev/null 2>/dev/null || $IPTABLES --new firewall-3g || exit 3\n" +
				"$IPTABLES -L firedwall-wifi >/dev/null 2>/dev/null || $IPTABLES --new firewall-wifi || exit 4\n" +
				"$IPTABLES -L firewall-reject >/dev/null 2>/dev/null || $IPTABLES --new firewall-reject || exit 5\n" +
				"# Add firewall chain to OUTPUT chain if necessary\n" +
				"$IPTABLES -L OUTPUT | $GREP -q firewall || $IPTABLES -A OUTPUT -j firewall || exit 6\n" +
				"# Flush existing rules\n" +
				"$IPTABLES -F firewall || exit 7\n" +
				"$IPTABLES -F firewall-3g || exit 8\n" +
				"$IPTABLES -F firewall-wifi || exit 9\n" +
				"$IPTABLES -F firewall-reject || exit 10\n" +
			"");
			
			
			
			script.append("# Main rules (per interface)\n");
			for (final String itf : ITFS_3G) {
				script.append("$IPTABLES -A firewall -o ").append(itf).append(" -j firewall-3g || exit\n");
			}
			for (final String itf : ITFS_WIFI) {
				script.append("$IPTABLES -A firewall -o ").append(itf).append(" -j firewall-wifi || exit\n");
			}
			
			script.append("# Filtering rules\n");
			
			final boolean any_3g = uids3g.indexOf(SPECIAL_UID_ANY) >= 0;
			final boolean any_wifi = uidsWifi.indexOf(SPECIAL_UID_ANY) >= 0;
			final String targetRule ="firewall-reject";
			if (any_3g) {
				
					/* block any application on this interface */
					script.append("$IPTABLES -A firewall-3g -j ").append(targetRule).append(" || exit\n");
				
			} else {
				/* release/block individual applications on this interface */
				for (final Integer uid : uids3g) {
					script.append("$IPTABLES -A firewall-3g -m owner --uid-owner ").append(uid).append(" -j ").append(targetRule).append(" || exit\n");
				}
			}
			if (any_wifi) {
				
					/* block any application on this interface */
					script.append("$IPTABLES -A firewall-wifi -j ").append(targetRule).append(" || exit\n");
				
			} else {
				/* release/block individual applications on this interface */
				for (final Integer uid : uidsWifi) {
					script.append("$IPTABLES -A firewall-wifi -m owner --uid-owner ").append(uid).append(" -j ").append(targetRule).append(" || exit\n");
				}
			}
			
	    	final StringBuilder res = new StringBuilder();
			code = runScriptAsRoot(ctx, script.toString(), res);
			if (showErrors && code != 0) {
				String msg = res.toString();
				Log.e("fireWall", msg);
				// Remove unnecessary help message from output
				if (msg.indexOf("\nTry `iptables -h' or 'iptables --help' for more information.") != -1) {
					msg = msg.replace("\nTry `iptables -h' or 'iptables --help' for more information.", "");
				}
				alert(ctx, "Error applying iptables rules. Exit code: " + code + "\n\n" + msg.trim());
			} else {
				return true;
			}
		} catch (Exception e) {
			if (showErrors) alert(ctx, "error refreshing iptables: " + e);
		}
		return false;
    }
	
	/**
     * Purge and re-add all saved rules (not in-memory ones).
     * This is much faster than just calling "applyIptablesRules", since it don't need to read installed applications.
     * @param ctx application context (mandatory)
     * @param showErrors indicates if errors should be alerted
     */
	public static boolean applySavedIptablesRules(Context ctx, boolean showErrors) {
		if (ctx == null) {
			return false;
		}
		
		final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, 0);
		
		PackageManager pm =ctx.getPackageManager();
		List<PackageInfo> packs = pm.getInstalledPackages(0);
		
		final List<Integer> uids_wifi = new LinkedList<Integer>();
		final List<Integer> uids_gprs = new LinkedList<Integer>();
		
		for (PackageInfo p : packs) {
			if ((p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
					&& (p.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
				int appid = p.applicationInfo.uid;
				String Uids_gprs = prefs.getString(appid + "gprs", "false");
				String Uids_wifi = prefs.getString(appid + "wifi", "false");
				if(Uids_gprs.equals("ture")){
					uids_gprs.add(appid);
				}
				if(Uids_wifi.equals("ture")){
					uids_wifi.add(appid);
				}
				
			}
			
			
		}
		
		
		
		return applyIptablesRulesImpl(ctx, uids_wifi, uids_gprs, showErrors);
	}
	
	

	/**
     * Purge and re-add all rules.
     * @param ctx application context (mandatory)
     * @param showErrors indicates if errors should be alerted
     */
	public static boolean applyIptablesRules(Context ctx, boolean showErrors) {
		if (ctx == null) {
			return false;
		}
		
		return applySavedIptablesRules(ctx, showErrors);
    }
	
	
	
	
	
	/**
	 * Check if we have root access
	 * @param ctx mandatory context
     * @param showErrors indicates if errors should be alerted
	 * @return boolean true if we have root
	 */
	public static boolean hasRootAccess(Context ctx, boolean showErrors) {
		if (hasroot) return true;
		final StringBuilder res = new StringBuilder();
		try {
			// Run an empty script just to check root access
			if (runScriptAsRoot(ctx, "exit 0", res) == 0) {
				hasroot = true;
				return true;
			}
		} catch (Exception e) {
		}
		if (showErrors) {
			alert(ctx, "Could not acquire root access.\n" +
				"You need a rooted phone to run fireWall.\n\n" +
				"If this phone is already rooted, please make sure fireWall has enough permissions to execute the \"su\" command.\n" +
				"Error message: " + res.toString());
		}
		return false;
	}
	
	/**
     * Display a simple alert box
     * @param ctx context
     * @param msg message
     */
	public static void alert(Context ctx, CharSequence msg) {
    	if (ctx != null) {
        	new AlertDialog.Builder(ctx)
        	.setNeutralButton(android.R.string.ok, null)
        	.setMessage(msg)
        	.show();
    	}
    }
	
	/**
	 * Asserts that the binary files are installed in the cache directory.
	 * @param ctx context
     * @param showErrors indicates if errors should be alerted
	 * @return false if the binary files could not be installed
	 */
	public static boolean assertBinaries(Context ctx, boolean showErrors) {
		boolean changed = false;
		try {
			// Check iptables_g1
			File file = new File(ctx.getCacheDir(), "iptables_g1");
			if (!file.exists()) {
				copyRawFile(ctx, R.raw.iptables_g1, file, "755");
				changed = true;
			}
			// Check iptables_n1
			file = new File(ctx.getCacheDir(), "iptables_n1");
			if (!file.exists()) {
				copyRawFile(ctx, R.raw.iptables_n1, file, "755");
				changed = true;
			}
			// Check busybox
			file = new File(ctx.getCacheDir(), "busybox_g1");
			if (!file.exists()) {
				copyRawFile(ctx, R.raw.busybox_g1, file, "755");
				changed = true;
			}
			if (changed) {
				Toast.makeText(ctx, R.string.toast_bin_installed, Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			if (showErrors) alert(ctx, "Error installing binary files: " + e);
			return false;
		}
		return true;
	}
	/**
	 * Copies a raw resource file, given its ID to the given location
	 * @param ctx context
	 * @param resid resource id
	 * @param file destination file
	 * @param mode file permissions (E.g.: "755")
	 * @throws IOException on error
	 * @throws InterruptedException when interrupted
	 */
	private static void copyRawFile(Context ctx, int resid, File file, String mode) throws IOException, InterruptedException
	{
		final String abspath = file.getAbsolutePath();
		// Write the iptables binary
		final FileOutputStream out = new FileOutputStream(file);
		final InputStream is = ctx.getResources().openRawResource(resid);
		byte buf[] = new byte[1024];
		int len;
		while ((len = is.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.close();
		is.close();
		// Change the permissions
		Runtime.getRuntime().exec("chmod "+mode+" "+abspath).waitFor();
	}
	
	/**
     * Runs a script, wither as root or as a regular user (multiple commands separated by "\n").
	 * @param ctx mandatory context
     * @param script the script to be executed
     * @param res the script output response (stdout + stderr)
     * @param timeout timeout in milliseconds (-1 for none)
     * @return the script exit code
     */
	public static int runScript(Context ctx, String script, StringBuilder res, long timeout, boolean asroot) {
		final File file = new File(ctx.getCacheDir(), SCRIPT_FILE);
		final ScriptRunner runner = new ScriptRunner(file, script, res, asroot);
		runner.start();
		try {
			if (timeout > 0) {
				runner.join(timeout);
			} else {
				runner.join();
			}
			if (runner.isAlive()) {
				// Timed-out
				runner.interrupt();
				runner.join(150);
				runner.destroy();
				runner.join(50);
			}
		} catch (InterruptedException ex) {}
		return runner.exitcode;
	}
	
	 /**
     * Runs a script as root (multiple commands separated by "\n").
	 * @param ctx mandatory context
     * @param script the script to be executed
     * @param res the script output response (stdout + stderr)
     * @param timeout timeout in milliseconds (-1 for none)
     * @return the script exit code
     */
	public static int runScriptAsRoot(Context ctx, String script, StringBuilder res, long timeout) {
		return runScript(ctx, script, res, timeout, true);
    }
    /**
     * Runs a script as root (multiple commands separated by "\n") with a default timeout of 20 seconds.
	 * @param ctx mandatory context
     * @param script the script to be executed
     * @param res the script output response (stdout + stderr)
     * @param timeout timeout in milliseconds (-1 for none)
     * @return the script exit code
     * @throws IOException on any error executing the script, or writing it to disk
     */
	public static int runScriptAsRoot(Context ctx, String script, StringBuilder res) throws IOException {
		return runScriptAsRoot(ctx, script, res, 40000);
	}
	/**
	 * Internal thread used to execute scripts (as root or not).
	 */
	private static final class ScriptRunner extends Thread {
		private final File file;
		private final String script;
		private final StringBuilder res;
		private final boolean asroot;
		public int exitcode = -1;
		private Process exec;
		
		/**
		 * Creates a new script runner.
		 * @param file temporary script file
		 * @param script script to run
		 * @param res response output
		 * @param asroot if true, executes the script as root
		 */
		public ScriptRunner(File file, String script, StringBuilder res, boolean asroot) {
			this.file = file;
			this.script = script;
			this.res = res;
			this.asroot = asroot;
		}
		@Override
		public void run() {
			try {
				file.createNewFile();
				final String abspath = file.getAbsolutePath();
				// make sure we have execution permission on the script file
				Runtime.getRuntime().exec("chmod 777 "+abspath).waitFor();
				// Write the script to be executed
				final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file));
				out.write("#!/system/bin/sh\n");
				out.write(script);
				if (!script.endsWith("\n")) out.write("\n");
				out.write("exit\n");
				out.flush();
				out.close();
				if (this.asroot) {
					// Create the "su" request to run the script
					exec = Runtime.getRuntime().exec("su -c "+abspath);
				} else {
					// Create the "sh" request to run the script
					exec = Runtime.getRuntime().exec("sh "+abspath);
				}
				InputStreamReader r = new InputStreamReader(exec.getInputStream());
				final char buf[] = new char[1024];
				int read = 0;
				// Consume the "stdout"
				while ((read=r.read(buf)) != -1) {
					if (res != null) res.append(buf, 0, read);
				}
				// Consume the "stderr"
				r = new InputStreamReader(exec.getErrorStream());
				read=0;
				while ((read=r.read(buf)) != -1) {
					if (res != null) res.append(buf, 0, read);
				}
				// get the process exit code
				if (exec != null) this.exitcode = exec.waitFor();
			} catch (InterruptedException ex) {
				if (res != null) res.append("\nOperation timed-out");
			} catch (Exception ex) {
				if (res != null) res.append("\n" + ex);
			} finally {
				destroy();
			}
		}
		/**
		 * Destroy this script runner
		 */
		public synchronized void destroy() {
			if (exec != null) exec.destroy();
			exec = null;
		}
	}
}