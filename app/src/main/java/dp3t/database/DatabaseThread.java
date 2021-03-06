/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package dp3t.database;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import androidx.annotation.NonNull;

class DatabaseThread extends HandlerThread {

	private static DatabaseThread instance;

	private Looper looper;
	private Handler handler;
	private Handler mainHandler;

	static DatabaseThread getInstance(@NonNull Context context) {
		if (instance == null) {
			instance = new DatabaseThread(context);
		}
		return instance;
	}

	private DatabaseThread(Context context) {
		super("DatabaseThread");
		start();

		looper = getLooper();
		handler = new Handler(looper);
		mainHandler = new Handler(context.getMainLooper());
	}

	void post(@NonNull Runnable runnable) {
		handler.post(runnable);
	}

	void onResult(@NonNull Runnable runnable) {
		mainHandler.post(runnable);
	}

}
