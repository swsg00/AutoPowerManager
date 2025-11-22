package com.oppo.autopower;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "AutoPowerManager"; 

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "BootReceiver received action: " + action);
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || 
            "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            
            // 启动核心服务
            Intent serviceIntent = new Intent(context, AutoPowerService.class);
            context.startService(serviceIntent);
            
            // 确保服务被设置为前台服务以提高优先级
            if (AutoPowerApplication.isServiceEnabled) {
                Log.d(TAG, "Starting service after boot");
            }
        }
    }
}