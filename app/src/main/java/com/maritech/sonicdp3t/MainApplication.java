/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package com.maritech.sonicdp3t;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import dp3t.logger.LogLevel;
import dp3t.logger.Logger;
import dp3t.sdk.DP3T;
import dp3t.util.NotificationUtil;
import dp3t.util.PreferencesUtil;
import dp3t.util.ProcessUtil;

public class MainApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		if (ProcessUtil.isMainProcess(this)) {
			registerReceiver(sdkReceiver, DP3T.getUpdateIntentFilter());
			initDP3T(this);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationUtil.createNotificationChannel(this);
		}
		Logger.init(getApplicationContext(), LogLevel.DEBUG);
	}

	public static void initDP3T(Context context) {
		DP3T.init(context, "com.maritech.sonicdp3t", true);
	}

	@Override
	public void onTerminate() {
		if (ProcessUtil.isMainProcess(this)) {
			unregisterReceiver(sdkReceiver);
		}
		super.onTerminate();
	}

	private BroadcastReceiver sdkReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (DP3T.getStatus(context).getMatchedContacts().size() > 0 && !PreferencesUtil.isExposedNotificationShown(context)) {
				NotificationUtil.showNotification(context, R.string.push_exposed_title,
						R.string.push_exposed_text, R.drawable.ic_handshakes);
				PreferencesUtil.setExposedNotificationShown(context);
			}
		}
	};

}