package com.oppo.autopower;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class NotificationManager {

    private static final String TAG = "AutoPowerManager";
    private static final String CHANNEL_ID = "auto_power_notification_channel";
    private static final int NOTIFICATION_ID = 1001;

    private Context mContext;
    private android.app.NotificationManager mNotificationManager;

    public NotificationManager(Context context) {
        this.mContext = context;
        this.mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Android 8.0及以上需要创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "自动关机通知",
                    android.app.NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("用于显示自动关机倒计时提醒");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 250, 250, 250});
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    public void showPowerOffNotification() {
        try {
            Log.d(TAG, "显示关机前10分钟通知");

            // 创建通知Intent（不会实际打开，只是为了符合通知要求）
            Intent intent = new Intent(mContext, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    mContext,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            // 构建通知
            Notification.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder = new Notification.Builder(mContext, CHANNEL_ID);
            } else {
                builder = new Notification.Builder(mContext);
                builder.setPriority(Notification.PRIORITY_HIGH);
            }

            builder.setSmallIcon(android.R.drawable.ic_lock_power_off)
                    .setContentTitle(mContext.getString(R.string.notification_title))
                    .setContentText(mContext.getString(R.string.notification_content))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true) // 可以取消
                    // 移除setOngoing(true)使其可以删除
                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
            
            // 确保Android 7.1及以下版本也保持高优先级
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                builder.setPriority(Notification.PRIORITY_HIGH);
            }

            // 显示通知
            Notification notification = builder.build();
            mNotificationManager.notify(NOTIFICATION_ID, notification);
            
            Log.d(TAG, "关机通知已显示");
        } catch (Exception e) {
            Log.e(TAG, "显示关机通知失败", e);
        }
    }

    public void cancelPowerOffNotification() {
        try {
            mNotificationManager.cancel(NOTIFICATION_ID);
            Log.d(TAG, "关机通知已取消");
        } catch (Exception e) {
            Log.e(TAG, "取消关机通知失败", e);
        }
    }
}