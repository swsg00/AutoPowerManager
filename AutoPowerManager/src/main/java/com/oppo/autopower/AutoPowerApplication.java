package com.oppo.autopower;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class AutoPowerApplication extends Application {

    private static final String TAG = "AutoPowerManager";
    public static boolean isServiceEnabled = true; // 服务默认开启
    private AntiUninstallManager mAntiUninstallManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application onCreate");
        
        // 初始化防卸载管理器
        mAntiUninstallManager = new AntiUninstallManager(this);
        
        // 启动保护机制
        mAntiUninstallManager.startProtection();
        mAntiUninstallManager.createSecurityFiles();

        // 启动核心服务
        Intent serviceIntent = new Intent(this, AutoPowerService.class);
        startService(serviceIntent);
    }
}