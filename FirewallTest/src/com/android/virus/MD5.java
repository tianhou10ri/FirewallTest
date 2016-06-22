package com.android.virus;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class MD5 {
	/*
	 * 获取文件MD5值
	 */

	public static String getMd5(String filepath) {
		try {
			// 获取到数字消息的摘要器
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
			// 将每个byte字节的数据转换成16进制的数据
			for (int i = 0; i < result.length; i++) {
				int number = result[i] & 0xff;// 加盐
				String str = Integer.toHexString(number);// 将十进制的number转换成十六进制数据
				if (str.length() == 1) {// 判断加密后的字符的长度，如果长度为1，则在该字符前面补0
					sb.append("0");
					sb.append(str);
				} else {
					sb.append(str);
				}
			}
			return sb.toString();// 将加密后的字符转成字符串返回

		} catch (Exception e) {
			e.printStackTrace();
			// CNA'T REACH;
			return null;
		}
	}
}
