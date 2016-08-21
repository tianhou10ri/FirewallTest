package com.android.virus;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class MD5 {
	/*
	 * ��ȡ�ļ�MD5ֵ
	 */

	public static String getMd5(String filepath) {
		try {
			// ��ȡ��������Ϣ��ժҪ��
			MessageDigest digest = MessageDigest.getInstance("MD5");
			File file = new File(filepath);
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			int len = -1;
			while ((len = fis.read(buffer)) != -1) {
				digest.update(buffer, 0, len);
			}

			byte[] result = digest.digest();
			StringBuilder sb = new StringBuilder();
			// ��ÿ��byte�ֽڵ�����ת����16���Ƶ�����
			for (int i = 0; i < result.length; i++) {
				int number = result[i] & 0xff;// ����
				String str = Integer.toHexString(number);// ��ʮ���Ƶ�numberת����ʮ����������
				if (str.length() == 1) {// �жϼ��ܺ���ַ��ĳ��ȣ��������Ϊ1�����ڸ��ַ�ǰ�油0
					sb.append("0");
					sb.append(str);
				} else {
					sb.append(str);
				}
			}
			return sb.toString();// �����ܺ���ַ�ת���ַ�������

		} catch (Exception e) {
			e.printStackTrace();
			// CNA'T REACH;
			return null;
		}
	}
}
