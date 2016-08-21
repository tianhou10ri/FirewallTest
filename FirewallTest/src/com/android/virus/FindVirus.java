package com.android.virus;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class FindVirus{
	/*
	 * ���ĳ��md5�Ƿ��ڲ�������
	 */
	public static String checkVirus(String md5){
		String desc = null;
		String path = "/data/data/com.example.mobilesafe/files/antivirus.db";  
        //�����ݿ�  
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        if(db.isOpen()){  
            //ִ�в�ѯ����������һ�������  
            Cursor cursor = db.rawQuery("select desc from datable where md5=?", new String[]{md5});  
            if(cursor.moveToFirst()){  
            	desc = cursor.getString(0);  
            }  
            //����ر�ϵͳ���α꣬���û�йرգ���ʹ�ر������ݿ⣬Ҳ���ױ����ڴ�й©���쳣��Ϣ  
            cursor.close();  
            db.close();  
        }  
        return desc;  
        
	}
}