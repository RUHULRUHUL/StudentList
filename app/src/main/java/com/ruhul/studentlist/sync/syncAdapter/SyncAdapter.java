package com.ruhul.studentlist.sync.syncAdapter;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.ruhul.studentlist.FileUploadFourground;
import com.ruhul.studentlist.R;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 5;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 2;
    ContentResolver mContentResolver;
    private static String logDebug = "SyncAdapterDebugTest";

    private static Context context;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account,
                              Bundle bundle,
                              String s,
                              ContentProviderClient contentProviderClient,
                              SyncResult syncResult) {

        showNotification();

        //upload file in server
        Log.d(logDebug, "call -: onPerformSync started...");
        WorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(FileUploadFourground.class).build();
        WorkManager.getInstance(getContext()).enqueue(uploadWorkRequest);

    }

    public static Account getSyncAccount(Context context) {
        Log.d(logDebug, "call -: getSyncAccount started...");
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account("Ruhul", "com.ruhul.studentlist");

        if (null == accountManager.getPassword(newAccount)) {
            if (!accountManager.addAccountExplicitly(newAccount, null, null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        Log.d(logDebug, "call -: onAccountCreated started...");
        SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, "com.ruhul.studentlist.provider", true);
        //syncImmediately(context);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {

        Log.d(logDebug, "configurePeriodicSync- syncInterval: " + syncInterval);
        Log.d(logDebug, "configurePeriodicSync- flexTime: " + flexTime);

        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static void syncImmediately(Context context) {

        Log.d(logDebug, "call - syncImmediately: ");

        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static void initializeSyncAdapter(Context mContext) {
        Log.d(logDebug, "call - initializeSyncAdapter: ");
        context = mContext;
        getSyncAccount(context);
    }

    private void showNotification() {

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_baseline_cloud_upload_24)
                .setContentTitle("My notification")
                .setContentText("File upload ..")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line..."))
                .setAllowSystemGeneratedContextualActions(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(100, 1, false)
                .setContentText("Download completed").setProgress(0, 0, false);


        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification Channel";
            String description = "this is for Test Notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}
