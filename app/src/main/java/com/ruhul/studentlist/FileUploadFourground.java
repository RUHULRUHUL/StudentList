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

public class FileUploadFourground extends Worker {

    private static final String CHANNEL_ID = "File Upload";
    private NotificationManager notificationManager;

    private final Context context;

    private int fileUpload = 0;

    String logDebug = "FileUploadForeground";

    public FileUploadFourground(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        notificationManager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        setForegroundAsync(createForegroundInfo(0));

        for (int i = 1; i <= 100; i++) {

            fileUpload = i;
            Log.d(logDebug, "call -: doWork started...");

            new CountDownTimer(20000, 1000) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    Log.d(logDebug, "call -: onFinish ...");
                    setForegroundAsync(createForegroundInfo(fileUpload));
                }

            }.start();
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
