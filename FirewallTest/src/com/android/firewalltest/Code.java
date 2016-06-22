package com.android.firewalltest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import android.R.bool;
import android.os.Environment;

/**
 * DES加密类
 * 
 * 
 * 
 */
public class Code {

	String SDPath = Environment.getExternalStorageDirectory().getPath();
	String KEY = "12345678";

	/*
	 * 加密文件
	 */

	public String encode(String srcPath) {

		File dir = new File(SDPath + File.separator + "DrawEncode");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		String random = new Random().nextLong()+"";

		File fileTemp = new File(srcPath);
		File filecodeTemp = new File(dir, getnamelimit(fileTemp.getName())+random
				+ ".jpg");

		try {
			filecodeTemp.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] enby = loadFile2Bytes(fileTemp.getPath());

		enby = desCrypto(enby, KEY);

		saveFileByBytes(enby, filecodeTemp.getPath());

		return filecodeTemp.getPath();
	}
	
	

	/*
	 * 解密文件
	 */

	public String decode(String srcPath) {

		File dir = new File(SDPath + File.separator + "DrawDecode");
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File fileTemp = new File(srcPath);
		File filedecodeTemp = new File(dir, "decode"
				+ getnamelimit(fileTemp.getName()) + ".jpg");

		byte[] deby = loadFile2Bytes(fileTemp.getPath());

		try {
			deby = decrypt(deby, KEY);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		saveFileByBytes(deby, filedecodeTemp.getPath());

		return filedecodeTemp.getPath();
	}

	/**
	 * 将字节流存为文件
	 * 
	 * @param bytes
	 * @param fileName
	 *            文件地址及文件名。例："/sdcard/a.txt”
	 */

	public static void saveFileByBytes(byte[] bytes, String fileName) {
		try {
			File saveFile = new File(fileName);
			FileOutputStream outStream = new FileOutputStream(saveFile);
			outStream.write(bytes);
			outStream.close();
		} catch (FileNotFoundException e) {
			return;
		} catch (IOException e) {
			return;
		}
	}

	/**
	 * 以字节流方式读取文件
	 * 
	 * @param fileName
	 *            文件地址及文件名。例："/sdcard/a.txt”
	 * @return
	 */
	public static byte[] loadFile2Bytes(String fileName) {
		try {

			File file = new File(fileName);
			FileInputStream inStream = new FileInputStream(file);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			long l = file.length();// 最大值2147483647
			byte[] buffer = new byte[(int) l];
			int length = -1;
			while ((length = inStream.read(buffer)) != -1) {
				stream.write(buffer, 0, length);
			}
			stream.close();
			inStream.close();
			return buffer;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * desCrypto DES加密
	 * 
	 * @param datasource
	 * @param password
	 *            秘钥为8-56位字符串
	 * @return
	 */
	public static byte[] desCrypto(byte[] datasource, String password) {
		try {
			SecureRandom random = new SecureRandom();
			DESKeySpec desKey = new DESKeySpec(password.getBytes());
			// 创建一个密匙工厂，然后用它把DESKeySpec转换成
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey securekey = keyFactory.generateSecret(desKey);
			// Cipher对象实际完成加密操作
			Cipher cipher = Cipher.getInstance("DES");
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
			// 现在，获取数据并加密
			// 正式执行加密操作
			return cipher.doFinal(datasource);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * decrypt DES解密
	 * 
	 * @param src
	 * @param password
	 *            秘钥为8-56位字符串
	 */
	public static byte[] decrypt(byte[] src, String password) throws Exception {

		// DES算法要求有一个可信任的随机数源
		SecureRandom random = new SecureRandom();
		// 创建一个DESKeySpec对象
		DESKeySpec desKey = new DESKeySpec(password.getBytes());
		// 创建一个密匙工厂
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		// 将DESKeySpec对象转换成SecretKey对象
		SecretKey securekey = keyFactory.generateSecret(desKey);
		// Cipher对象实际完成解密操作
		Cipher cipher = Cipher.getInstance("DES");
		// 用密匙初始化Cipher对象
		cipher.init(Cipher.DECRYPT_MODE, securekey, random);
		// 真正开始解密操作
		return cipher.doFinal(src);
	}

	/*
	 * 获取文件名
	 */
	public String getnamelimit(String filename) {
		String limitname = filename.substring(0, filename.lastIndexOf("."));
		return limitname;
	}

}