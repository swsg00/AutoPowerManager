@echo off
echo ====================================================
echo        AutoPowerManager 构建与安装工具 - 简化版
====================================================
echo.

set PROJECT_DIR=%~dp0
set OUTPUT_DIR=%PROJECT_DIR%output

rem 创建输出目录
mkdir %OUTPUT_DIR% 2>nul

rem 检查预编译APK是否存在
echo [步骤1] 检查预编译APK...
if exist "%PROJECT_DIR%prebuilt\AutoPowerManager-unsigned.apk" (
    echo 发现预编译APK文件
    
    rem 检查文件大小
    for %%A in ("%PROJECT_DIR%prebuilt\AutoPowerManager-unsigned.apk") do set "FILE_SIZE=%%~zA"
    echo 文件大小: %FILE_SIZE% 字节
    
    if %FILE_SIZE% leq 1024 (
        echo 警告: APK文件大小过小，可能是文本占位符
        echo 请按照以下步骤提供真实的APK文件:
        echo 1. 从开发者处获取真实的APK文件
        echo 2. 或从已安装该应用的设备中导出
        echo 3. 替换 %PROJECT_DIR%prebuilt\AutoPowerManager-unsigned.apk
        goto :provide_apk_guide
    )
    
    rem 复制预编译APK
    echo 复制预编译APK...
    copy "%PROJECT_DIR%prebuilt\AutoPowerManager-unsigned.apk" "%OUTPUT_DIR%\AutoPowerManager-unsigned.apk" /y
    if %errorlevel% neq 0 (
        echo 错误: 复制APK失败!
        pause
        exit /b 1
    )
    echo 预编译APK准备完成
) else (
    echo 未找到预编译APK文件
    
    rem 检查是否可以构建
    echo [步骤2] 尝试从源代码构建...
    
    rem 检查Java环境
    java -version >nul 2>&1
    if %errorlevel% neq 0 (
        echo 错误: 未找到Java环境!
        goto :setup_guide
    )
    
    rem 检查gradlew.bat
    if exist "%PROJECT_DIR%gradlew.bat" (
        echo 发现gradlew.bat，开始构建...
        call "%PROJECT_DIR%gradlew.bat" assembleDebug
        
        if %errorlevel% neq 0 (
            echo 构建失败!
            goto :build_failed
        )
        
        rem 检查构建结果
        if exist "%PROJECT_DIR%app\build\outputs\apk\debug\app-debug.apk" (
            echo 构建成功! 复制APK...
            copy "%PROJECT_DIR%app\build\outputs\apk\debug\app-debug.apk" "%OUTPUT_DIR%\AutoPowerManager-unsigned.apk" /y
            echo APK准备完成
        ) else (
            echo 错误: 构建成功但未找到输出文件!
            goto :build_failed
        )
    ) else (
        echo 未找到gradlew.bat文件
        goto :setup_guide
    )
)

echo.
echo ====================================================
echo              APK准备完成!
echo ====================================================
echo 输出文件: %OUTPUT_DIR%\AutoPowerManager-unsigned.apk

goto :end

:provide_apk_guide
echo.
echo ====================================================
echo              如何提供真实的APK文件
====================================================
echo 方法1: 从开发者处获取
   - 联系项目开发者获取AutoPowerManager的预编译APK

方法2: 从设备导出
   - 在已安装该应用的设备上使用ADB导出:
   - adb shell pm list packages | findstr AutoPowerManager
   - adb shell pm path 包名
   - adb pull 路径 %PROJECT_DIR%prebuilt\AutoPowerManager-unsigned.apk

echo.
pause
exit /b 1

:setup_guide
echo.
echo ====================================================
echo                设置开发环境指南
====================================================
echo 要构建AutoPowerManager，您需要:

1. 安装Java JDK 8或更高版本
   - 下载: https://www.oracle.com/java/technologies/javase-jdk11-downloads.html
   - 安装后确保java命令在环境变量PATH中

2. 安装Gradle或使用gradlew
   - 方法A: 下载Gradle: https://gradle.org/install/
   - 方法B: 复制gradlew.bat和gradlew到项目根目录

3. 或提供预编译APK
   - 在项目目录中创建prebuilt文件夹
   - 将真实的APK文件重命名为AutoPowerManager-unsigned.apk放入该文件夹

echo.
pause
exit /b 1

:build_failed
echo.
echo ====================================================
echo                  构建失败
====================================================
echo 建议的解决方案:

1. 提供预编译APK文件 (推荐)
   - 在项目目录创建prebuilt文件夹
   - 放入真实的AutoPowerManager-unsigned.apk文件

2. 修复构建环境
   - 检查Java和Gradle安装是否正确
   - 确保项目结构完整
   - 检查依赖项是否已安装

echo.
pause
exit /b 1

:end
echo.
echo ====================================================
echo             接下来您可以:
====================================================
echo 1. 使用原始的build_and_sign.bat进行签名和安装
   - 确保已准备好platform.pk8和platform.x509.pem签名文件 (如需要)

2. 手动将APK安装到设备
   - adb install %OUTPUT_DIR%\AutoPowerManager-unsigned.apk
   (注意: 普通安装可能需要设备允许未知来源安装)

echo.
echo 操作完成! 按任意键退出...
pause >nul