package com.pixandroid.utilbox;


import static android.content.Context.BATTERY_SERVICE;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.DrawableCompat;

import org.joda.time.Days;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/********************************************
 *     Created by DailyCoding on 27-Feb-23.  *
 ********************************************/

public class UtilBox {

    public static void vibrate(Context context) {
        //ADD VIBRATE PERMISSION IN YOUR MANIFEST
        //<uses-permission android:name="android.permission.VIBRATE" />

        long ms = 32L;
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(ms);
        }
    }

    public static void copyText(Context context, String text) {
        if (!text.equals("")) {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Note", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Text Copied!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Empty Text cannot be copied!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void pasteText(Context context, TextView textView, EditText editText) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        try {
            CharSequence textToPaste = clipboard.getPrimaryClip().getItemAt(0).getText();
            if (textView != null)
                textView.setText(textToPaste);

            if (editText != null) {
                String data = editText.getText().toString() + " " +textToPaste;
                editText.setText(data.trim());
            }

        } catch (Exception e) {
            return;
        }
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

    public static void findRouteOnGoogleMap(Context context, String from, String to) {
        Uri mapIntentUri = Uri.parse("google.navigation:q=" + from
                + "," + to);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        context.startActivity(mapIntent);
    }

    public static void openAppSetting (Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }


    public static void launchUrl(Context context, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
    }


    public static int getDeviceScreenWidth(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        return width;
    }

    public static int getDeviceScreenHeight(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        return height;
    }


    //BATTERY
    public static int getBatteryLevel(Context context) {

        if (Build.VERSION.SDK_INT >= 21) {

            BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        } else {

            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, iFilter);

            int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
            int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

            double batteryPct = level / (double) scale;

            return (int) (batteryPct * 100);
        }
    }
    public static boolean isDeviceCharging(Context context) {
        boolean isPlugged= false;
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        isPlugged = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            isPlugged = isPlugged || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
        }
        return isPlugged;
    }

    public static void hideKeyboard(Activity activity) {
        if (activity == null)
            return;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void getAppFeedback(Activity activity) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity.getPackageName())));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String getAppVersion(Activity activity) {
        String versionName = "";

        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return String.format("%s", versionName);
    }

    public static String getAppInstallDateTime(Activity activity) {
        long installDate;
        String formattedDate = "";
        try {
            installDate = activity.getPackageManager()
                    .getPackageInfo(activity.getPackageName(), 0)
                    .firstInstallTime;
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            formattedDate=dateFormat.format(installDate);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "Installed Date: " + formattedDate;
    }




    //TODO 01 - ViewUtil
    public static class ViewUtil {

        public static boolean isViewVisible(View view) {
            return view.getVisibility() == View.VISIBLE;
        }


        public static void setViewWidth(View view, int size) {
            view.getLayoutParams().width = size;
        }

        public static void setViewHeight(View view, int size) {
            view.getLayoutParams().height = size;
        }

        public static void setViewWidthMatchParent(Activity activity, View view) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            view.getLayoutParams().width = width;
        }


    }

    //TODO 02 - TimeX
    public static class TimeUtil {

        public static String greeting() {
            Calendar c = Calendar.getInstance();
            int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

            String greeting = "";
            if(timeOfDay >= 0 && timeOfDay < 12){
                greeting = "Good Morning";
            }else if(timeOfDay >= 12 && timeOfDay < 16){
                greeting  = "Good Afternoon";
            }else if(timeOfDay >= 16 && timeOfDay < 21){
                greeting = "Good Evening";
            }else if(timeOfDay >= 21 && timeOfDay < 24){
                greeting = "Good Night";
            }

            return greeting;
        }

        public static String addMinutes(String date_time, int minutesToAdd) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Calendar mTime = Calendar.getInstance();

            try {
                mTime.setTime((sdf.parse(date_time)));
                mTime.add(Calendar.MINUTE, minutesToAdd);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            int hour = mTime.get(Calendar.HOUR_OF_DAY);
            int minute = mTime.get(Calendar.MINUTE);
            int seconds = mTime.get(Calendar.SECOND);

            int year = mTime.get(Calendar.YEAR);
            int month = mTime.get(Calendar.MONTH);
            int day = mTime.get(Calendar.DAY_OF_MONTH);

            month++;
            return String.format("%d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, seconds);
        }

        public static long convertStringDateTimeToMS(String dateTimeString) {
            //"2023-03-21 12:12:01
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = null;
            try {
                date = sdf.parse(dateTimeString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return date.getTime();
        }

        public static String convertMilliSecondsToStringTime(long milliSeconds, String dateFormat) //"dd/MM/yyyy hh:mm:ss aa"
        {
            // Create a DateFormatter object for displaying date in specified format.
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

            // Create a calendar object that will convert the date and time value in milliseconds to date.
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(milliSeconds);
            return formatter.format(calendar.getTime());
        }

        public static String convertMilliSecondsToStringDate(long milliSeconds, String dateFormat) //"dd/MM/yyyy hh:mm:ss aa"
        {
            // Create a DateFormatter object for displaying date in specified format.
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

            // Create a calendar object that will convert the date and time value in milliseconds to date.
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(milliSeconds);
            return formatter.format(calendar.getTime());
        }

        public static String timeNow() {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);
            int seconds = currentTime.get(Calendar.SECOND);

            int year = currentTime.get(Calendar.YEAR);
            int month = currentTime.get(Calendar.MONTH);
            int day = currentTime.get(Calendar.DAY_OF_MONTH);

            month++;
            return String.format("%d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, seconds);
        }

        public static String timeNowHHmm() {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            return String.format("%02d:%02d", hour, minute);
        }

        public static boolean isTimeFuture(String time) {

            String pattern = "HH:mm";
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);

            try {
                Date date1 = sdf.parse(time);
                Date currentDate = sdf.parse(timeNowHHmm());

                if(date1.after(currentDate)) {
                    return true;
                } else {

                    return false;
                }
            } catch (ParseException e){
                e.printStackTrace();
            }
            return false;
        }
        public static boolean isTimePast(String time) {

            String pattern = "HH:mm";
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);

            try {
                Date date1 = sdf.parse(time);
                Date currentDate = sdf.parse(timeNowHHmm());

                if(date1.before(currentDate)) {
                    return true;
                } else {

                    return false;
                }
            } catch (ParseException e){
                e.printStackTrace();
            }
            return false;
        }


        public static String howManyDaysLeft(String expiry_date){
            Calendar mCalendar = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            //Log.e(TAG, "onBindViewHolder: TO_DAY_DATE: "+df.format(mCalendar.getTime()));
            org.joda.time.LocalDate dateBefore = org.joda.time.LocalDate.parse(df.format(mCalendar.getTime()));
            org.joda.time.LocalDate dateAfter = org.joda.time.LocalDate.parse(expiry_date);
            long daysDiff = Math.abs(Days.daysBetween(dateBefore, dateAfter).getDays());
            String date = daysDiff+" days left";
            return date;
        }

        public static String formatLongTimeToHMS(long ms) {
            int sec = (int) (ms/1000)%60;
            int min= (int) (((ms/1000) / 60));
            int hrs = (int) ((((ms/1000)/60)/60));

            if(ms >= 3600000) {
                //Hours
                min= (int) (((ms/1000) / 60)%60);
                return String.format(Locale.getDefault(), "%02dh %02dm %02ds", hrs, min, sec);
            }

            return String.format(Locale.getDefault(), "%02dm %02ds", min, sec);
        }

        public static String formatTimeToHMS(long ms) {
            long oneHour = 3600000;

            int sec = (int) (ms/1000)%60;
            int min= (int) (((ms/1000) / 60));
            int hrs = (int) ((((ms/1000)/60)/60));

            if (ms >= oneHour){
                min= (int) (((ms/1000) / 60)%60);
                return String.format("%02d:%02d:%02d", hrs, min, sec);
            }

            return String.format("%s:%s:%s", hrs, min, sec);
        }

    }

    //TODO 03 - ImageUtil
    public static class ImageUtil {

        public static void changeIconColor(Context context, int icon, int color, ImageView imageView) {
            Drawable unwrappedDrawable = AppCompatResources.getDrawable(context, icon);
            Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
            DrawableCompat.setTint(wrappedDrawable, color);
            imageView.setImageDrawable(wrappedDrawable);
        }

        public static Bitmap getCircleBitmap(Bitmap bitmap) {
            final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(output);

            final int color = Color.RED;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(rect);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawOval(rectF, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            bitmap.recycle();

            return output;
        }

        public static String bitmapToBase64 (Bitmap bitmap) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
            return encoded;
        }

        public static Bitmap view2bitmap(View v) {
            Bitmap b = Bitmap.createBitmap( v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
            v.draw(c);
            return b;
        }

        public static Bitmap ViewToBitmapImage(View view) {
            //Define a bitmap with the same size as the view
            Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            //Bind a canvas to it
            Canvas canvas = new Canvas(returnedBitmap);
            //Get the view's background
            Drawable bgDrawable = view.getBackground();

            if (bgDrawable != null)
                //has background drawable, then draw it on the canvas
                bgDrawable.draw(canvas);
            else
                //does not have background drawable, then draw white background on the canvas
                canvas.drawColor(Color.WHITE);

            // draw the view on the canvas
            view.draw(canvas);
            //return the bitmap
            return returnedBitmap;
        }

        public static String saveBitmapToCache(Context context, Bitmap finalBitmap) {

            File myDir =  new File(context.getCacheDir().getPath());

            String fname = "Card-" + System.currentTimeMillis() + ".png";
            File file = new File(myDir, fname);
            try {
                FileOutputStream out = new FileOutputStream(file);
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            } catch (FileNotFoundException ignored){} catch (Exception e) {
                e.printStackTrace();
            }

            String PHOTO_URI = file.getAbsolutePath();

            return PHOTO_URI;

        }

        public static void applyIconColor(Context context, ImageView icon, int resColor) {
            DrawableCompat.setTint(
                    DrawableCompat.wrap(icon.getDrawable()),
                    ContextCompat.getColor(context, resColor)
            );
        }

        public static Bitmap takeScreenShot(Activity activity) {
            View view = activity.getWindow().getDecorView();
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            Bitmap b1 = view.getDrawingCache();
            Rect frame = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            int statusBarHeight = frame.top;
            int width = activity.getWindowManager().getDefaultDisplay().getWidth();
            int height = activity.getWindowManager().getDefaultDisplay().getHeight();

            Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height  - statusBarHeight);
            view.destroyDrawingCache();
            return b;
        }


        public static Bitmap blurBitmap(Bitmap sentBitmap, int radius) {
            Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

            if (radius < 1) {
                return (null);
            }

            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            int[] pix = new int[w * h];
            Log.e("pix", w + " " + h + " " + pix.length);
            bitmap.getPixels(pix, 0, w, 0, 0, w, h);

            int wm = w - 1;
            int hm = h - 1;
            int wh = w * h;
            int div = radius + radius + 1;

            int r[] = new int[wh];
            int g[] = new int[wh];
            int b[] = new int[wh];
            int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
            int vmin[] = new int[Math.max(w, h)];

            int divsum = (div + 1) >> 1;
            divsum *= divsum;
            int dv[] = new int[256 * divsum];
            for (i = 0; i < 256 * divsum; i++) {
                dv[i] = (i / divsum);
            }

            yw = yi = 0;

            int[][] stack = new int[div][3];
            int stackpointer;
            int stackstart;
            int[] sir;
            int rbs;
            int r1 = radius + 1;
            int routsum, goutsum, boutsum;
            int rinsum, ginsum, binsum;

            for (y = 0; y < h; y++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                for (i = -radius; i <= radius; i++) {
                    p = pix[yi + Math.min(wm, Math.max(i, 0))];
                    sir = stack[i + radius];
                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);
                    rbs = r1 - Math.abs(i);
                    rsum += sir[0] * rbs;
                    gsum += sir[1] * rbs;
                    bsum += sir[2] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                }
                stackpointer = radius;

                for (x = 0; x < w; x++) {

                    r[yi] = dv[rsum];
                    g[yi] = dv[gsum];
                    b[yi] = dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (y == 0) {
                        vmin[x] = Math.min(x + radius + 1, wm);
                    }
                    p = pix[yw + vmin[x]];

                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[(stackpointer) % div];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi++;
                }
                yw += w;
            }
            for (x = 0; x < w; x++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                yp = -radius * w;
                for (i = -radius; i <= radius; i++) {
                    yi = Math.max(0, yp) + x;

                    sir = stack[i + radius];

                    sir[0] = r[yi];
                    sir[1] = g[yi];
                    sir[2] = b[yi];

                    rbs = r1 - Math.abs(i);

                    rsum += r[yi] * rbs;
                    gsum += g[yi] * rbs;
                    bsum += b[yi] * rbs;

                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }

                    if (i < hm) {
                        yp += w;
                    }
                }
                yi = x;
                stackpointer = radius;
                for (y = 0; y < h; y++) {
                    // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                    pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * w;
                    }
                    p = x + vmin[y];

                    sir[0] = r[p];
                    sir[1] = g[p];
                    sir[2] = b[p];

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi += w;
                }
            }


            Log.e("pix", w + " " + h + " " + pix.length);
            bitmap.setPixels(pix, 0, w, 0, 0, w, h);

            return (bitmap);
        }

        private Bitmap getBitmapFromView(View view) {
            //Define a bitmap with the same size as the view
            Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            //Bind a canvas to it
            Canvas canvas = new Canvas(returnedBitmap);
            //Get the view's background
            Drawable bgDrawable = view.getBackground();
            if (bgDrawable != null)
                //has background drawable, then draw it on the canvas
                bgDrawable.draw(canvas);
            else
                //does not have background drawable, then draw white background on the canvas
                canvas.drawColor(Color.WHITE);
            // draw the view on the canvas
            view.draw(canvas);
            //return the bitmap
            return returnedBitmap;
        }

    }


    //TODO 04 - TextUtil
    public static class TextUtil {
        public static String getRoundNumber(String num) {
            String n = num;
            if (!num.isEmpty()) {
                if (num.contains(",")) {
                    n = num.replaceAll(",", "");
                }
            }

            return String.format(Locale.US, "%.1f", Double.parseDouble(n));
        }

        public static String getRoundedNumber(String val) {
            if (val.equals("")) return "0";

            String n = val;
            if (!val.isEmpty()) {
                if (val.contains(",")) {
                    n = val.replaceAll(",", "");
                }
            }

            BigDecimal bill = new BigDecimal(n);
            BigDecimal roundedBill = bill.setScale(0, RoundingMode.HALF_UP);

            return String.valueOf(roundedBill);
        }

        public static boolean isValidEmail(CharSequence target) {
            return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
        }
    }


    //TODO - ShareUtil
    public static class ShareUtil {

        public static void shareText(Context context, String text) {
            if (!text.equals("")) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "EXTRA_SUBJECT");
                shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                context.startActivity(Intent.createChooser(shareIntent, "Share Text to"));
            } else {
                Toast.makeText(context, "Nothing to Share !!!", Toast.LENGTH_SHORT).show();
            }
        }

        private void shareImageBitmap(Activity activity, Uri bitmapUri) {
            Intent share = new Intent(Intent.ACTION_SEND);
            //share.setType("palin/text");
            share.setType("image/*");
            //share.putExtra(Intent.EXTRA_SUBJECT, "Voice Typing Master");
            //share.putExtra(Intent.EXTRA_TEXT,  "http://play.google.com/store/apps/details?id=" + activity.getPackageName());
            share.putExtra(Intent.EXTRA_STREAM, bitmapUri);
            activity.startActivity(Intent.createChooser(share,"Share over .."));
        }

        public static void sendOverSMS(Context context, String text) {
            if (!text.equals("")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("smsto:"));
                //intent.setDataAndType(Uri.parse("smsto:"), "vnd.android-dir/mms-sms");
                intent.setType("vnd.android-dir/mms-sms");
                intent.putExtra("address", new String (""));
                intent.putExtra("sms_body", text);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Nothing to Share !!!", Toast.LENGTH_SHORT).show();
            }

        }

        public static void sendOverWhatsApp(Context context, String text) {
            if (!text.equals("")) {
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("text/plain");
                whatsappIntent.setPackage("com.whatsapp");
                whatsappIntent.putExtra(Intent.EXTRA_TEXT, text);
                try {
                    context.startActivity(whatsappIntent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(context, "WhatsApp has not been installed.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Nothing to Share !!!", Toast.LENGTH_SHORT).show();
            }

        }

        public static void shareFile(Context context, String filePath) {
            //BuildConfig.APPLICATION_ID
            File shareFile = new File(filePath);

            if (!shareFile.isDirectory()) {
                Uri fileUri = Uri.fromFile(shareFile);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", shareFile);
                }

                Intent share = new Intent(Intent.ACTION_SEND);
//                share.setType("palin/text");
                share.setType("image/*");
//                share.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.app_name));
//                share.putExtra(Intent.EXTRA_TEXT, "Download Link : " + "http://play.google.com/store/apps/details?id=" + context.getPackageName());
                share.putExtra(Intent.EXTRA_STREAM, fileUri);
                context.startActivity(Intent.createChooser(share,"Share over .."));
            } else {
                Toast.makeText(context, "Folder can't be shared!", Toast.LENGTH_SHORT).show();
            }
        }

        public static void shareListOfFile(Context context, List<String> filePaths) {

            List<Uri> uriList = new ArrayList<>();
            for (String path: filePaths) {
                File shareFile = new File(path);

                if (shareFile.isFile()) {
                    Uri fileUri = Uri.fromFile(shareFile);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", shareFile);
                    }
                    uriList.add(fileUri);
                }
            }

            if (uriList.size() > 0) {
                Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);
//                share.setType("palin/text");
//                share.setType("txt/*");
//                share.setType("pdf/*");
//                share.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.app_name));
//                share.putExtra(Intent.EXTRA_TEXT, "Download Link : " + "http://play.google.com/store/apps/details?id=" + context.getPackageName());
                share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, (ArrayList<? extends Parcelable>) uriList);
                context.startActivity(Intent.createChooser(share,"Share over .."));
            } else {
                Toast.makeText(context, "Folder can't be shared!", Toast.LENGTH_SHORT).show();
            }

        }
    }


}
