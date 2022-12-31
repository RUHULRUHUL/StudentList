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
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.ruhul.studentlist.R;
import com.ruhul.studentlist.Student;
import com.ruhul.studentlist.model.post.Post;
import com.ruhul.studentlist.model.post.PostResponse;
import com.ruhul.studentlist.network.RetrofitClient;
import com.ruhul.studentlist.room.StudentDB;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String logDebug = "SyncAdapterDebugTest";
    private final Context context;
    private final List<Student> studentList = new ArrayList<>();
    private StudentDB studentDB;


    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.context = context;
        Log.d(logDebug, "call - initializeSyncAdapter: ");
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
                .setContentTitle("Upload..")
                .setAllowSystemGeneratedContextualActions(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            Post post = new Post(student.getId(), student.getName());
            builder.setProgress(students.size(), i, false);
            notificationManager.notify(2, builder.build());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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
        builder.setContentIntent(null);
        notificationManager.notify(2, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification Channel";
            String description = "this is for Test Notification";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}