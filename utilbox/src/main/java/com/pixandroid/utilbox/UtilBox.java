package com.pixandroid.utilbox;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.Locale;

/********************************************
 *     Created by DailyCoding on 27-Feb-23.  *
 ********************************************/

public class UtilBox {
    public static String getRoundNumber(String num) {
        String n = num;
        if (!num.isEmpty()) {
            if (num.contains(",")) {
                n = num.replaceAll(",", "");
            }
        }

        return String.format(Locale.US, "%.1f", Double.parseDouble(n));
    }

    public static boolean isMyServiceRunning(Activity activity, Class<?> serviceClass) {
        if (activity == null) return false;
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public static void makeCall(Context context, String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
        context.startActivity(intent);
    }

    public static void launchGoogleMap(Context context, String from, String to) {
        Uri mapIntentUri = Uri.parse("google.navigation:q=" + from
                + "," + to);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        context.startActivity(mapIntent);
    }

}
