/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package dp3t.internal;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import Jampack.Z;
import dp3t.backend.RxUtil;
import dp3t.backend.WebService;
import dp3t.database.Database;
import dp3t.logger.Logger;
import dp3t.models.ApplicationInfo;
import dp3t.models.PullData;

import static dp3t.crypto.CryptoModule.hexStringToByteArray;

public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";
    private static final String WORK_TAG = "dp3t.internal.SyncWorker";
    public static final long BATCH_LENGTH = 21 * 24 * 60 * 60 * 1000L;

    public static void startSyncWorker(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueueUniquePeriodicWork(WORK_TAG, ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest);
    }

    public static void stopSyncWorker(Context context) {
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.cancelAllWorkByTag(WORK_TAG);
    }

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        long scanInterval = AppConfigManager.getInstance(getApplicationContext()).getScanInterval();
        TracingService.scheduleNextClientRestart(context, scanInterval);
        TracingService.scheduleNextServerRestart(context);
        try {
            doSync(context);
            Logger.i(TAG, "synced");
            AppConfigManager.getInstance(context).setLastSyncNetworkSuccess(true);
        } catch (IOException e) {
            Logger.e(TAG, e);
            AppConfigManager.getInstance(context).setLastSyncNetworkSuccess(false);
            return Result.retry();
        }

        return Result.success();
    }

    public static void doSync(Context context) throws IOException {
        AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
        appConfigManager.updateFromDiscoverySynchronous();
        Database database = new Database(context);
        database.generateContactsFromHandshakes(context);
        long lastLoadedBatchReleaseTime = appConfigManager.getLastLoadedBatchReleaseTime();
        long nextBatchReleaseTime;
        if (lastLoadedBatchReleaseTime <= 0 || lastLoadedBatchReleaseTime % BATCH_LENGTH != 0) {
            long now = System.currentTimeMillis();
            nextBatchReleaseTime = now - (now % BATCH_LENGTH);
        } else {
            nextBatchReleaseTime = lastLoadedBatchReleaseTime + BATCH_LENGTH;
        }
        for (long batchReleaseTime = nextBatchReleaseTime;
             batchReleaseTime < System.currentTimeMillis();
             batchReleaseTime += BATCH_LENGTH) {
            long syncDate = batchReleaseTime;
            long startDate = batchReleaseTime/1000;
            long endDate = System.currentTimeMillis()/1000;
            Log.d(TAG, "doSync: finalBatchReleaseTime " + startDate);
            Log.d(TAG, "doSync: nowTime " + endDate);
            RxUtil.networkConsumer(WebService.service.getCases(startDate, endDate),
                    pullData -> {
                        Log.d(TAG, "doSync: " + true);
                        for (PullData data: pullData){
                            if (data.getToken().length()==32){
                                database.addKnownCase(
                                        context,
                                        hexStringToByteArray(data.getToken()),
                                        data.getCreateDate(),
                                        syncDate
                                );
                            }
                        }
                        appConfigManager.setLastLoadedBatchReleaseTime(syncDate);

                    }, throwable -> {
                        Log.d(TAG, "doSync: " + false);
                    });
        }

        database.removeOldKnownCases();

        appConfigManager.setLastSyncDate(System.currentTimeMillis());

        BroadcastHelper.sendUpdateBroadcast(context);
    }

}
