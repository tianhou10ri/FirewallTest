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
 * DES������
 * 
 * 
 * 
 */
public class Code {

	String SDPath = Environment.getExternalStorageDirectory().getPath();
	String KEY = "12345678";

	/*
	 * �����ļ�
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
	 * �����ļ�
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
	 * ���ֽ�����Ϊ�ļ�
	 * 
	 * @param bytes
	 * @param fileName
	 *            �ļ���ַ���ļ���������"/sdcard/a.txt��
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
	 * ���ֽ�����ʽ��ȡ�ļ�
	 * 
	 * @param fileName
	 *            �ļ���ַ���ļ���������"/sdcard/a.txt��
	 * @return
	 */
	public static byte[] loadFile2Bytes(String fileName) {
		try {

			File file = new File(fileName);
			FileInputStream inStream = new FileInputStream(file);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			long l = file.length();// ���ֵ2147483647
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
	 * desCrypto DES����
	 * 
	 * @param datasource
	 * @param password
	 *            ��ԿΪ8-56λ�ַ���
	 * @return
	 */
	public static byte[] desCrypto(byte[] datasource, String password) {
		try {
			SecureRandom random = new SecureRandom();
			DESKeySpec desKey = new DESKeySpec(password.getBytes());
			// ����һ���ܳ׹�����Ȼ��������DESKeySpecת����
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey securekey = keyFactory.generateSecret(desKey);
			// Cipher����ʵ����ɼ��ܲ���
			Cipher cipher = Cipher.getInstance("DES");
			// ���ܳ׳�ʼ��Cipher����
			cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
			// ���ڣ���ȡ���ݲ�����
			// ��ʽִ�м��ܲ���
			return cipher.doFinal(datasource);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * decrypt DES����
	 * 
	 * @param src
	 * @param password
	 *            ��ԿΪ8-56λ�ַ���
	 */
	public static byte[] decrypt(byte[] src, String password) throws Exception {

		// DES�㷨Ҫ����һ�������ε������Դ
		SecureRandom random = new SecureRandom();
		// ����һ��DESKeySpec����
		DESKeySpec desKey = new DESKeySpec(password.getBytes());
		// ����һ���ܳ׹���
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		// ��DESKeySpec����ת����SecretKey����
		SecretKey securekey = keyFactory.generateSecret(desKey);
		// Cipher����ʵ����ɽ��ܲ���
		Cipher cipher = Cipher.getInstance("DES");
		// ���ܳ׳�ʼ��Cipher����
		cipher.init(Cipher.DECRYPT_MODE, securekey, random);
		// ������ʼ���ܲ���
		return cipher.doFinal(src);
	}

	/*
	 * ��ȡ�ļ���
	 */
	public String getnamelimit(String filename) {
		String limitname = filename.substring(0, filename.lastIndexOf("."));
		return limitname;
	}

}