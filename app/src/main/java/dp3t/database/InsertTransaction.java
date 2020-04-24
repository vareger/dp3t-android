/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package dp3t.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

public class InsertTransaction implements Runnable {

	private SQLiteDatabase db;
	private String tableName;
	private ContentValues values;

	InsertTransaction(@NonNull SQLiteDatabase db, @NonNull String tableName, @NonNull ContentValues values) {
		this.db = db;
		this.tableName = tableName;
		this.values = values;
	}

	@Override
	public void run() {
		db.beginTransaction();
		try {
			db.insert(tableName, null, values);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			// anything
		} finally {
			db.endTransaction();
		}
	}

}
