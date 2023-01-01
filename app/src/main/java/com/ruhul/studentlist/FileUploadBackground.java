package com.ruhul.studentlist;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
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

public class FileUploadBackground extends Worker {

    private static final String CHANNEL_ID = "File Upload";
    String logDebug = "FileUploadForeground";

    private final Context context;
    private final List<Student> studentList = new ArrayList<>();
    private StudentDB studentDB;


    public FileUploadBackground(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(logDebug, "Call : doWork : ");
        studentList.clear();
        try {
            studentDB = StudentDB.Companion.getInstance(context);
            studentList.addAll(studentDB.studentDao().getLocalStudents());
            if (studentList.size() > 0) {
                Log.d(logDebug, "call - Observer :getLocalStudents List size: " + studentList.size());

                for (int i = 0; i < studentList.size(); i++) {
                    Student student = studentList.get(i);
                    Post post = new Post(student.getId(), student.getName());
                    Log.d(logDebug, "Progress  : " + i);

                    setForegroundAsync(upload(i));

                    //testing purpose for progress show
                    Thread.sleep(1500);

                    int finalI = i;
                    RetrofitClient.getApiServices()
                            .postData(post)
                            .enqueue(new Callback<PostResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<PostResponse> call,
                                                       @NonNull Response<PostResponse> response) {
                                    if (response.isSuccessful()) {
                                        Log.d(logDebug, "call -: RetrofitClient onResponse ...");
                                        studentDB.studentDao().deleteStudent(studentList.get(finalI));
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<PostResponse> call, @NonNull Throwable t) {
                                    Log.d(logDebug, "call -: RetrofitClient onFailure ...");
                                }
                            });
                }
            }
        } catch (Exception e) {
            Log.d(logDebug, "call -: Exception " + e.getMessage());
            e.printStackTrace();
        }
        return Result.success();
    }


    @NonNull
    private ForegroundInfo upload(int i) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

        PendingIntent intent = WorkManager.getInstance(context)
                .createCancelPendingIntent(getId());

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Uploading " + i + " %")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_baseline_cloud_upload_24)
                .setSilent(true)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_delete, "cancel", intent)
                .setProgress(studentList.size(), i, false)
                .build();

        return new ForegroundInfo(1, notification);
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
