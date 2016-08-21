package com.android.virus;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class FindVirus{
	/*
	 * 检查某个md5是否在病毒库中
	 */
	public static String checkVirus(String md5){
		String desc = null;
		String path = "/data/data/com.example.mobilesafe/files/antivirus.db";  
        //打开数据库  
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        if(db.isOpen()){  
            //执行查询操作，返回一个结果集  
            Cursor cursor = db.rawQuery("select desc from datable where md5=?", new String[]{md5});  
            if(cursor.moveToFirst()){  
            	desc = cursor.getString(0);  
            }  
            //必须关闭系统的游标，如果没有关闭，即使关闭了数据库，也容易报出内存泄漏的异常信息  
            cursor.close();  
            db.close();  
        }  
        return desc;  
        
	}
}