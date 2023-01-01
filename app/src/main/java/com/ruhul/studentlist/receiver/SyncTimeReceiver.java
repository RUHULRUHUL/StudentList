package com.ruhul.studentlist.receiver;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SyncTimeReceiver extends BroadcastReceiver {

    private Account account;

    private final String  logDebug = "SyncAdapterDebugTest";


    @Override
    public void onReceive(Context context, Intent intent) {
        account = createSyncAccount(context);

    }


    private Account createSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        String ACCOUNT = "ruhul@gmail.com";
        String ACCOUNT_TYPE = "com.ruhul.studentlist";
        account = new Account(ACCOUNT, ACCOUNT_TYPE);

        try {
            if (accountManager.addAccountExplicitly(account, null, null)) {

                Log.d(logDebug, "call -: addAccountExplicitly started. account create successfully..");
                return null;
            } else {
                syncImmediately(account);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return account;
    }

    private void syncImmediately(Account account) {
        Log.d(logDebug, "call - syncImmediately: ");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        String AUTHORITY = "com.ruhul.studentlist.provider";
        ContentResolver.requestSync(
                account,
                AUTHORITY,
                bundle
        );
    }
}
