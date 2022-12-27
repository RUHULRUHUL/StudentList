package com.ruhul.studentlist.sync.syncAdapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.ruhul.studentlist.R;
import com.ruhul.studentlist.Student;
import com.ruhul.studentlist.room.StudentDB;

import java.util.List;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final long SYNC_INTERVAL = 10000L;
    public static final long SYNC_FLEXTIME = SYNC_INTERVAL / 2;
    ContentResolver mContentResolver;
    private static final String logDebug = "SyncAdapterDebugTest";

    @SuppressLint("StaticFieldLeak")
    private final Context context;

    private StudentDB studentDB;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.context = context;
        mContentResolver = context.getContentResolver();
        studentDB = StudentDB.Companion.getInstance(context);
    }

    public void initializeSyncAdapter() {
        Log.d(logDebug, "call - initializeSyncAdapter: ");
        getSyncAccount();
    }

    @Override
    public void onPerformSync(Account account,
                              Bundle bundle,
                              String s,
                              ContentProviderClient contentProviderClient,
                              SyncResult syncResult) {

        Log.d(logDebug, "call -: onPerformSync started...");

/*        studentDB.studentDao().getLocalStudents()
                .observe((LifecycleOwner) this, new Observer<List<Student>>() {
                    @Override
                    public void onChanged(List<Student> students) {
                        Log.d(logDebug, "call -: onPerformSync started..." + students.size());
                        showNotification(students);

                    }
                });*/
/*      WorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(FileUploadFourground.class).build();
        WorkManager.getInstance(context).enqueue(uploadWorkRequest);*/

    }

    public Account getSyncAccount() {
        Log.d(logDebug, "call -: getSyncAccount started...");
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account("Ruhul", context.getString(R.string.account_type));

        if (null == accountManager.getPassword(newAccount)) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount);
        }
        return newAccount;
    }

    private void onAccountCreated(Account newAccount) {
        Log.d(logDebug, "call -: onAccountCreated started...");
        configurePeriodicSync();
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately();
    }

    public void configurePeriodicSync() {
        Log.d(logDebug, "configurePeriodicSync- syncInterval: " + SYNC_INTERVAL);
        Log.d(logDebug, "configurePeriodicSync- flexTime: " + SYNC_FLEXTIME);
        Account account = getSyncAccount();
        String authority = context.getString(R.string.content_authority);
        ContentResolver.addPeriodicSync(
                account,
                authority,
                Bundle.EMPTY,
                SYNC_INTERVAL);
    }

    public void syncImmediately() {
        Log.d(logDebug, "call - syncImmediately: ");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(),
                context.getString(R.string.content_authority), bundle);
    }


    private void showNotification(List<Student> students) {

        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_baseline_cloud_upload_24)
                .setContentTitle("sync adapter")
                .setContentText("file upload ..")
                .setAllowSystemGeneratedContextualActions(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        int totalItem = students.size();

        if (!students.isEmpty()) {
            for (int i = 0; i < students.size(); i++) {
                int Position = i;
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        studentDB.studentDao().deleteStudent(students.get(Position));
                    }
                };
                thread.start();
                builder.setProgress(totalItem, i, false);
                notificationManager.notify(2, builder.build());

            }
        }


/*        for (int i = 1; i <= 100; i++) {
            //Room Item Delete
            try {
                Thread.sleep(200);
                builder.setProgress(100, i, false);
                notificationManager.notify(2, builder.build());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/

        builder.setProgress(0, 0, false);
        builder.clearActions();
        builder.setContentIntent(null);
        notificationManager.notify(2, builder.build());

        //  notificationManager.notify(2, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification Channel";
            String description = "this is for Test Notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}