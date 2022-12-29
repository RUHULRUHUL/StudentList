package com.ruhul.studentlist;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ruhul.studentlist.model.post.Post;
import com.ruhul.studentlist.model.post.PostResponse;
import com.ruhul.studentlist.network.RetrofitClient;
import com.ruhul.studentlist.room.StudentDB;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileUploadFourground extends Worker {

    private static final String CHANNEL_ID = "File Upload";
    private NotificationManager notificationManager;
    private final Context context;
    private int fileUpload = 0;
    String logDebug = "FileUploadForeground";
    private final List<Student> studentList = new ArrayList<>();
    private StudentDB studentDB;


    public FileUploadFourground(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        notificationManager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
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
        return Result.success();
    }


    @NonNull
    private ForegroundInfo createForegroundInfo(int progress) {
        Context context = getApplicationContext();
        String id = "My Notification Channel Id_1";
        String title = "Sync File Upload...";
        String cancel = "Cancel";
        PendingIntent intent = WorkManager.getInstance(context)
                .createCancelPendingIntent(getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title + " Upload - " + progress + " %")
                .setTicker(title)
                .setSmallIcon(R.drawable.ic_baseline_cloud_upload_24)
                .setOngoing(true)
                .setProgress(100, progress, false)
                .addAction(android.R.drawable.ic_delete, cancel, intent)
                .build();

        return new ForegroundInfo(100, notification);
    }

    private void uploadData(List<Student> students) {

        Log.d(logDebug, "call -: StartNotification ...");
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(context, "CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_baseline_cloud_upload_24)
                .setContentTitle("Data Uploading")
                .setContentText("upload ..")
                .setAllowSystemGeneratedContextualActions(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            Post post = new Post(student.getId(), student.getName());
            int finalI = i;
            RetrofitClient.getApiServices()
                    .postData(post)
                    .enqueue(new Callback<PostResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<PostResponse> call,
                                               @NonNull Response<PostResponse> response) {
                            if (response.isSuccessful()) {
                                Log.d(logDebug, "call -: RetrofitClient onResponse ...");
                                builder.setProgress(students.size(), finalI, false);
                                notificationManager.notify(2, builder.build());
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


    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification Channel";
            String description = "this is for Test Notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
