package com.oppo.autopower;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class PhoneCallReceiver extends BroadcastReceiver {

    private static final String TAG = "AutoPowerManager";
    private static final String TRIGGER_CODE = "*#8495162#*";
    private static final String TIME_RANGE_PATTERN = "^\*#8495162#\*(\d{2})(\d{2})-(\d{2})(\d{2})\*$";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "PhoneCallReceiver received action: " + action);
        
        if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action)) {
            // 获取拨出的电话号码
            String phoneNumber = getResultData();
            Log.d(TAG, "Outgoing call number: " + phoneNumber);
            
            // 拦截这个拨号请求
            setResultData(null);
            
            // 检查是否匹配时间范围模式
            if (phoneNumber.matches(TIME_RANGE_PATTERN)) {
                Log.d(TAG, "Time range pattern detected: " + phoneNumber);
                handleTimeRangeChange(context, phoneNumber);
            } 
            // 检查是否匹配触发代码（仅开关服务）
            else if (TRIGGER_CODE.equals(phoneNumber)) {
                Log.d(TAG, "Trigger code detected: " + TRIGGER_CODE);
                toggleServiceStatus(context);
            }
        }
    }
    
    private void handleTimeRangeChange(Context context, String phoneNumber) {
        try {
            // 解析时间范围格式：*#8495162#*HHMM-HHMM*
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(TIME_RANGE_PATTERN);
            java.util.regex.Matcher matcher = pattern.matcher(phoneNumber);
            
            if (matcher.matches()) {
                // 解析开始时间和结束时间
                int startHour = Integer.parseInt(matcher.group(1));
                int startMinute = Integer.parseInt(matcher.group(2));
                int endHour = Integer.parseInt(matcher.group(3));
                int endMinute = Integer.parseInt(matcher.group(4));
                
                // 验证时间范围的有效性
                if (isValidTimeRange(startHour, startMinute, endHour, endMinute)) {
                    // 创建时间范围对象
                    TimeRangeConfig.TimeRange timeRange = new TimeRangeConfig.TimeRange(
                            startHour, startMinute, endHour, endMinute);
                    
                    // 保存时间范围配置
                    TimeRangeConfig config = new TimeRangeConfig(context);
                    config.saveTimeRange(timeRange);
                    
                    String message = "时间范围已更新: " + formatTime(startHour, startMinute) + "-" + formatTime(endHour, endMinute);
                    Log.d(TAG, message);
                    showToast(context, message);
                    
                    // 重启服务以应用新的时间范围
                    Intent serviceIntent = new Intent(context, AutoPowerService.class);
                    context.stopService(serviceIntent);
                    context.startService(serviceIntent);
                } else {
                    showToast(context, "无效的时间范围，请检查输入格式");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling time range change", e);
            showToast(context, "设置时间范围失败");
        }
    }
    
    private boolean isValidTimeRange(int startHour, int startMinute, int endHour, int endMinute) {
        // 验证时间范围的有效性
        if (startHour < 0 || startHour > 23 || endHour < 0 || endHour > 23) {
            return false;
        }
        if (startMinute < 0 || startMinute > 59 || endMinute < 0 || endMinute > 59) {
            return false;
        }
        
        // 时间范围可以跨天
        return true;
    }
    
    private String formatTime(int hour, int minute) {
        return String.format("%02d:%02d", hour, minute);
    }
    
    private void toggleServiceStatus(final Context context) {
        // 切换服务状态
        boolean newStatus = !AutoPowerApplication.isServiceEnabled;
        AutoPowerApplication.isServiceEnabled = newStatus;
        
        Log.d(TAG, "Service status toggled to: " + newStatus);
        
        // 保存状态到配置
        saveServiceStatus(context, newStatus);
        
        // 重新启动服务以应用新状态
        Intent serviceIntent = new Intent(context, AutoPowerService.class);
        context.stopService(serviceIntent);
        context.startService(serviceIntent);
        
        // 显示操作结果提示
        final String message = newStatus ? "自动关机服务已开启" : "自动关机服务已关闭";
        showToast(context, message);
    }
    
    private void saveServiceStatus(Context context, boolean enabled) {
        // 保存服务状态到配置文件
        try {
            // 使用与TimeRangeConfig相同的机制保存状态
            String statusConfigFile = "/data/system/autopower_service_status.prop";
            java.io.File file = new java.io.File(statusConfigFile);
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write("service_enabled=" + enabled);
            writer.close();
            
            // 设置权限
            Runtime.getRuntime().exec(new String[]{"chmod", "600", statusConfigFile});
            Runtime.getRuntime().exec(new String[]{"chown", "system:system", statusConfigFile});
            
            Log.d(TAG, "Service status saved to " + statusConfigFile);
        } catch (Exception e) {
            Log.e(TAG, "Error saving service status", e);
        }
    }
    
    private void loadServiceStatus(Context context) {
        // 从配置文件加载服务状态
        try {
            String statusConfigFile = "/data/system/autopower_service_status.prop";
            java.io.File file = new java.io.File(statusConfigFile);
            if (file.exists() && file.canRead()) {
                java.util.Properties props = new java.util.Properties();
                props.load(new java.io.FileInputStream(file));
                String status = props.getProperty("service_enabled", "true");
                AutoPowerApplication.isServiceEnabled = Boolean.parseBoolean(status);
                Log.d(TAG, "Service status loaded: " + AutoPowerApplication.isServiceEnabled);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading service status", e);
        }
    }
    
    private void showToast(final Context context, final String message) {
        // 在主线程显示Toast
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}