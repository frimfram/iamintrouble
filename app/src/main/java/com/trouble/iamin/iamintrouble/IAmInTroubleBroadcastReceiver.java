package com.trouble.iamin.iamintrouble;

import android.os.Bundle;
import android.util.Log;

import com.facebook.FacebookBroadcastReceiver;

/**
 * Created by jeanro on 5/14/16.
 */
public class IAmInTroubleBroadcastReceiver extends FacebookBroadcastReceiver {

    @Override
    protected void onSuccessfulAppCall(String appCallId, String action, Bundle extras) {
        // A real app could update UI or notify the user that their photo was uploaded.
        Log.d("HelloFacebook", String.format("Photo uploaded by call " + appCallId + " succeeded."));
    }

    @Override
    protected void onFailedAppCall(String appCallId, String action, Bundle extras) {
        // A real app could update UI or notify the user that their photo was not uploaded.
        Log.d("HelloFacebook", String.format("Photo uploaded by call " + appCallId + " failed."));
    }
}
