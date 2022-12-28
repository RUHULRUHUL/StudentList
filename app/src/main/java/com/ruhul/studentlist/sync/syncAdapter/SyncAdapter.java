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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.ruhul.studentlist.R;
import com.ruhul.studentlist.Student;
import com.ruhul.studentlist.model.post.Post;
import com.ruhul.studentlist.model.post.PostResponse;
import com.ruhul.studentlist.network.RetrofitClient;
import com.ruhul.studentlist.room.StudentDB;
import com.ruhul.studentlist.signup.RegistrationResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final long SYNC_INTERVAL = 60 * 16;
    public static final long SYNC_FLEXTIME = SYNC_INTERVAL / 2;
    ContentResolver mContentResolver;
    private static final String logDebug = "SyncAdapterDebugTest";

    @SuppressLint("StaticFieldLeak")
    private final Context context;
    private List<Student> studentList = new ArrayList<>();

    private StudentDB studentDB;


    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.context = context;
        mContentResolver = context.getContentResolver();
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

        studentList.clear();
        try {
            studentDB = StudentDB.Companion.getInstance(context);
            studentList.addAll(studentDB.studentDao().getLocalStudents());
            if (studentList.size() > 0) {
                Log.d(logDebug, "call - Observer :getLocalStudents List size: " + studentList.size());
                uploadData(studentList);
            }
        } catch (Exception e) {
            Log.d(logDebug, "call -: Exception " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void uploadData(List<Student> students) {

        Log.d(logDebug, "call -: StartNotification ...");

        NotificationManager notificationManager = (NotificationManager) getContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(getContext(), "CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_baseline_cloud_upload_24)
                .setContentTitle("Data Uploading")
                .setContentText("upload ..")
                .setAllowSystemGeneratedContextualActions(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            Post post = new Post(student.getId(), student.getName());

            builder.setProgress(students.size(), i, false);
            notificationManager.notify(2, builder.build());

            int finalI = i;
            RetrofitClient.getApiServices()
                    .postData(post)
                    .enqueue(new Callback<PostResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<PostResponse> call,
                                               @NonNull Response<PostResponse> response) {
                            if (response.isSuccessful()) {
                                Log.d(logDebug, "call -: RetrofitClient onResponse ...");
                                studentDB.studentDao().deleteStudent(students.get(finalI));
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<PostResponse> call, @NonNull Throwable t) {
                            Log.d(logDebug, "call -: RetrofitClient onFailure ...");
                        }
                    });
        }

        builder.setProgress(0, 0, false);
        builder.clearActions();
        builder.setAutoCancel(true);
        builder.setContentTitle("sync adapter");
        builder.setContentText("file upload completed");
        builder.setContentIntent(null);
        notificationManager.notify(2, builder.build());
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