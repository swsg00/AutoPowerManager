package com.oppo.autopower;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class TimeRangeConfig {

    private static final String TAG = "AutoPowerManager";
    private static final String CONFIG_FILE = "/data/system/autopower_config.prop";
    private static final String KEY_START_HOUR = "start_hour";
    private static final String KEY_START_MINUTE = "start_minute";
    private static final String KEY_END_HOUR = "end_hour";
    private static final String KEY_END_MINUTE = "end_minute";
    
    // 默认时间范围：晚上22:00到早上7:00
    private static final int DEFAULT_START_HOUR = 22;
    private static final int DEFAULT_START_MINUTE = 0;
    private static final int DEFAULT_END_HOUR = 7;
    private static final int DEFAULT_END_MINUTE = 0;
    
    private int startHour = DEFAULT_START_HOUR;
    private int startMinute = DEFAULT_START_MINUTE;
    private int endHour = DEFAULT_END_HOUR;
    private int endMinute = DEFAULT_END_MINUTE;
    
    private static TimeRangeConfig instance;
    private Context context;
    private boolean configLoaded = false;
    
    private TimeRangeConfig(Context context) {
        this.context = context.getApplicationContext();
        loadConfig();
    }
    
    public static synchronized TimeRangeConfig getInstance(Context context) {
        if (instance == null) {
            instance = new TimeRangeConfig(context);
        }
        return instance;
    }
    
    private void loadConfig() {
        Properties properties = new Properties();
        File configFile = new File(CONFIG_FILE);
        
        try {
            if (configFile.exists() && configFile.canRead()) {
                FileInputStream fis = new FileInputStream(configFile);
                properties.load(fis);
                fis.close();
                
                // 读取配置值
                startHour = Integer.parseInt(properties.getProperty(KEY_START_HOUR, String.valueOf(DEFAULT_START_HOUR)));
                startMinute = Integer.parseInt(properties.getProperty(KEY_START_MINUTE, String.valueOf(DEFAULT_START_MINUTE)));
                endHour = Integer.parseInt(properties.getProperty(KEY_END_HOUR, String.valueOf(DEFAULT_END_HOUR)));
                endMinute = Integer.parseInt(properties.getProperty(KEY_END_MINUTE, String.valueOf(DEFAULT_END_MINUTE)));
                
                configLoaded = true;
                Log.d(TAG, "Config loaded successfully from " + CONFIG_FILE);
            } else {
                // 配置文件不存在，使用默认值并尝试创建配置文件
                Log.d(TAG, "Config file not found, using default values");
                saveConfig();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading config", e);
            // 使用默认值
            setDefaultValues();
        }
    }
    
    public void saveConfig() {
        Properties properties = new Properties();
        properties.setProperty(KEY_START_HOUR, String.valueOf(startHour));
        properties.setProperty(KEY_START_MINUTE, String.valueOf(startMinute));
        properties.setProperty(KEY_END_HOUR, String.valueOf(endHour));
        properties.setProperty(KEY_END_MINUTE, String.valueOf(endMinute));
        
        File configFile = new File(CONFIG_FILE);
        File configDir = configFile.getParentFile();
        
        try {
            // 确保目录存在
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            
            // 保存配置文件
            FileOutputStream fos = new FileOutputStream(configFile);
            properties.store(fos, "Auto Power Manager Configuration");
            fos.close();
            
            // 设置文件权限，确保只有系统用户可读写
            Runtime.getRuntime().exec(new String[]{"chmod", "600", CONFIG_FILE});
            Runtime.getRuntime().exec(new String[]{"chown", "system:system", CONFIG_FILE});
            
            Log.d(TAG, "Config saved successfully to " + CONFIG_FILE);
        } catch (Exception e) {
            Log.e(TAG, "Error saving config", e);
            
            // 备选方案：尝试使用应用内部存储
            try {
                File internalConfig = new File(context.getFilesDir(), "autopower_config.prop");
                FileOutputStream fos = new FileOutputStream(internalConfig);
                properties.store(fos, "Auto Power Manager Configuration");
                fos.close();
                Log.d(TAG, "Config saved to internal storage as fallback");
            } catch (Exception ex) {
                Log.e(TAG, "Fallback config save failed", ex);
            }
        }
    }
    
    private void setDefaultValues() {
        startHour = DEFAULT_START_HOUR;
        startMinute = DEFAULT_START_MINUTE;
        endHour = DEFAULT_END_HOUR;
        endMinute = DEFAULT_END_MINUTE;
        Log.d(TAG, "Using default time range values");
    }
    
    public int getStartHour() {
        return startHour;
    }
    
    public int getStartMinute() {
        return startMinute;
    }
    
    public int getEndHour() {
        return endHour;
    }
    
    public int getEndMinute() {
        return endMinute;
    }
    
    public void setTimeRange(int startHour, int startMinute, int endHour, int endMinute) {
        // 验证时间值的有效性
        if (startHour >= 0 && startHour < 24 && startMinute >= 0 && startMinute < 60 &&
            endHour >= 0 && endHour < 24 && endMinute >= 0 && endMinute < 60) {
            
            this.startHour = startHour;
            this.startMinute = startMinute;
            this.endHour = endHour;
            this.endMinute = endMinute;
            
            saveConfig();
            Log.d(TAG, "Time range updated: " + startHour + ":" + startMinute + " - " + endHour + ":" + endMinute);
        } else {
            Log.e(TAG, "Invalid time range values");
        }
    }
    
    public boolean isConfigLoaded() {
        return configLoaded;
    }
}