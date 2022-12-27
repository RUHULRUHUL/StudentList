package com.ruhul.studentlist.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.ruhul.studentlist.sync.syncAdapter.SyncAdapter;

public class SyncService extends Service {
    private static SyncAdapter syncAdapter = null;
    private static final Object syncAdapterLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (syncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
