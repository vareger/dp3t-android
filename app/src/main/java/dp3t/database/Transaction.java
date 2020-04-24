/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package dp3t.database;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

public class Transaction implements Runnable {

	private SQLiteDatabase db;
	private String[] queries;

	Transaction(@NonNull SQLiteDatabase db, String... queries) {
		this.db = db;
		this.queries = queries;
	}

	@Override
	public void run() {
		db.beginTransaction();
		try {
			for (String query : queries) {
				db.execSQL(query);
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			// anything
		} finally {
			db.endTransaction();
		}
	}

}
