package com.oppo.autopower;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class AutoPowerService extends Service {

    private static final String TAG = "AutoPowerManager";
    private static final long CHECK_INTERVAL = 60 * 1000; // 1分钟检查一次
    private static final long AUTO_SHUTDOWN_DELAY = 10 * 60 * 1000; // 10分钟
    private static final long SHUTDOWN_PROCESS_INTERVAL = 30 * 1000; // 关机进程检查间隔

    private PowerManager.WakeLock mWakeLock;
    private PowerManager.WakeLock mShutdownWakeLock; // 关机过程专用WakeLock
    private TimeRangeConfig mTimeRangeConfig;
    private NotificationManager mNotificationManager;
    private AntiUninstallManager mAntiUninstallManager;
    private Calendar mBootTime;
    private boolean mIsShuttingDown = false;
    private Handler mHandler;
    private Timer mCheckTimer;
    private Timer mShutdownTimer;
    private Timer mShutdownProcessTimer; // 用于监控和确保关机进程

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate");

        mHandler = new Handler();
        mCheckTimer = new Timer();
        mShutdownTimer = new Timer();
        mShutdownProcessTimer = new Timer();

        mTimeRangeConfig = new TimeRangeConfig(this);
        mNotificationManager = new NotificationManager(this);
        mAntiUninstallManager = new AntiUninstallManager(this);
        mBootTime = Calendar.getInstance();
        Log.d(TAG, "Service started, boot time: " + mBootTime.getTime());
        
        // 启动防卸载保护
        mAntiUninstallManager.startProtection();

        // 获取WakeLock以防止系统休眠
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AutoPowerManager:WakeLock");
        mWakeLock.acquire();
        
        // 获取更强的WakeLock用于关机过程
        mShutdownWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, 
                "AutoPowerManager:ShutdownWakeLock");

        // 设置为前台服务以提高优先级
        startForeground(1, createForegroundNotification());

        // 启动检查任务
        startChecking();
    }

    private void startChecking() {
        Log.d(TAG, "Starting time range checking");
        
        mCheckTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkTimeRangeAndShutdown();
            }
        }, 0, CHECK_INTERVAL);
    }

    private void checkTimeRangeAndShutdown() {
        if (!AutoPowerApplication.isServiceEnabled || mIsShuttingDown) {
            return;
        }

        Calendar currentTime = Calendar.getInstance();
        Log.d(TAG, "Checking time: " + currentTime.getTime());

        // 检查是否在限制时间范围内
        if (mTimeRangeConfig.isInRestrictedTimeRange(currentTime)) {
            Log.d(TAG, "Device is in restricted time range");
            
            // 检查开机时间是否超过10分钟
            long uptime = currentTime.getTimeInMillis() - mBootTime.getTimeInMillis();
            if (uptime >= AUTO_SHUTDOWN_DELAY) {
                Log.d(TAG, "Uptime exceeded 10 minutes, scheduling shutdown");
                scheduleShutdown(0); // 立即关机，因为已经过了10分钟
            } else {
                long remainingTime = AUTO_SHUTDOWN_DELAY - uptime;
                Log.d(TAG, "Uptime is " + (uptime / 1000 / 60) + " minutes, scheduling shutdown in " + 
                      (remainingTime / 1000 / 60) + " minutes");
                scheduleShutdown(remainingTime);
            }
        } else {
            Log.d(TAG, "Device is not in restricted time range");
            // 不在限制时间范围内，取消之前的关机计划
            mShutdownTimer.cancel();
            mShutdownTimer = new Timer();
            mNotificationManager.cancelPowerOffNotification();
        }
    }

    private void scheduleShutdown(long delayMillis) {
        Log.d(TAG, "Scheduling shutdown in " + (delayMillis / 1000 / 60) + " minutes");
        
        // 取消之前的关机计划
        mShutdownTimer.cancel();
        mShutdownTimer = new Timer();
        
        // 如果剩余时间大于等于10分钟，先安排通知
        if (delayMillis >= 10 * 60 * 1000) {
            long notificationDelay = delayMillis - (10 * 60 * 1000);
            mShutdownTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    showPowerOffNotification();
                }
            }, notificationDelay);
        } else if (delayMillis > 0) {
            // 如果小于10分钟，立即显示通知
            showPowerOffNotification();
        }
        
        // 安排关机
        mShutdownTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                performShutdown();
            }
        }, delayMillis);
    }

    private void showPowerOffNotification() {
        Log.d(TAG, "Showing power off notification (10 minutes remaining)");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mNotificationManager.showPowerOffNotification();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to show notification", e);
                }
            }
        });
    }

    private void performShutdown() {
        if (mIsShuttingDown) {
            return;
        }
        
        mIsShuttingDown = true;
        Log.d(TAG, "Performing system shutdown");
        
        // 获取关机专用WakeLock，确保设备不会在关机过程中休眠
        if (mShutdownWakeLock != null && !mShutdownWakeLock.isHeld()) {
            mShutdownWakeLock.acquire();
        }
        
        // 启动关机进程监控，确保即使主进程被杀死也能执行关机
        startShutdownProcessMonitor();
        
        // 立即执行一次关机尝试
        executeFinalShutdown();
    }
    
    private void startShutdownProcessMonitor() {
        Log.d(TAG, "Starting shutdown process monitor");
        
        // 创建一个独立的定时器来监控并确保关机进程执行
        mShutdownProcessTimer.cancel();
        mShutdownProcessTimer = new Timer();
        
        mShutdownProcessTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                executeFinalShutdown();
            }
        }, 0, SHUTDOWN_PROCESS_INTERVAL);
    }
    
    private void executeFinalShutdown() {
        Log.d(TAG, "Executing final shutdown command");
        
        try {
            // 尝试多种关机方法，确保至少有一种能成功
            shutdownWithMultipleMethods();
        } catch (Exception e) {
            Log.e(TAG, "Shutdown attempt failed, will retry", e);
        }
    }
    
    private void shutdownWithMultipleMethods() {
        // 方法1：使用系统Intent
        try {
            Intent shutdownIntent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
            shutdownIntent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
            shutdownIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(shutdownIntent);
            Log.d(TAG, "Shutdown intent sent");
        } catch (Exception e) {
            Log.e(TAG, "Shutdown with intent failed", e);
        }
        
        // 方法2：使用shell命令（多个变种）
        try {
            // 直接使用reboot命令
            Process process = Runtime.getRuntime().exec("reboot -p");
            process.waitFor(5000); // 等待5秒
        } catch (Exception e) {
            Log.e(TAG, "Shutdown with reboot command failed", e);
        }
        
        try {
            // 使用su权限执行reboot
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot -p"});
            process.waitFor(5000);
        } catch (Exception e) {
            Log.e(TAG, "Shutdown with su failed", e);
        }
        
        try {
            // 使用poweroff命令
            Process process = Runtime.getRuntime().exec("poweroff");
            process.waitFor(5000);
        } catch (Exception e) {
            Log.e(TAG, "Shutdown with poweroff command failed", e);
        }
        
        // 方法3：使用init.rc命令
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "setprop sys.powerctl shutdown"});
            process.waitFor(5000);
        } catch (Exception e) {
            Log.e(TAG, "Shutdown with sys.powerctl failed", e);
        }
    }

    private android.app.Notification createForegroundNotification() {
        // 创建前台服务通知
        android.app.Notification.Builder builder = new android.app.Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("自动关机服务正在运行")
                .setSmallIcon(android.R.drawable.ic_lock_power_off);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Android 8.0及以上需要通知渠道
            String channelId = "auto_power_service_channel";
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    channelId, "自动关机服务", android.app.NotificationManager.IMPORTANCE_LOW);
            android.app.NotificationManager notificationManager = 
                    (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }
        
        return builder.build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "AutoPowerService onStartCommand");
        return START_STICKY; // 确保服务被杀死后会自动重启
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service onDestroy");

        // 释放资源
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        
        if (mShutdownWakeLock != null && mShutdownWakeLock.isHeld() && !mIsShuttingDown) {
            // 只有在非关机状态下才释放关机WakeLock
            mShutdownWakeLock.release();
        }

        mCheckTimer.cancel();
        mShutdownTimer.cancel();
        
        // 即使在服务销毁时，如果正在关机，也要保持关机进程定时器
        if (!mIsShuttingDown) {
            mShutdownProcessTimer.cancel();
            mNotificationManager.cancelPowerOffNotification();
        } else {
            Log.d(TAG, "Service destroyed during shutdown, keeping shutdown process active");
        }
        
        // 停止保护机制
        if (mAntiUninstallManager != null) {
            mAntiUninstallManager.stopProtection();
        }

        // 尝试重启服务以防被意外杀死
        if (AutoPowerApplication.isServiceEnabled) {
            Log.d(TAG, "Attempting to restart service");
            Intent restartIntent = new Intent(this, AutoPowerService.class);
            startService(restartIntent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}