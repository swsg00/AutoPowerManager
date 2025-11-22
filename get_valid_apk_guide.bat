@echo off
echo ====================================================
echo        如何解决 "APK文件不存在!" 错误
====================================================
echo.
echo 问题分析：
echo 您在运行build_and_sign.bat时遇到了"APK文件不存在!"的错误。
echo 这是因为脚本无法找到有效的AutoPowerManager APK文件。
echo.
echo ====================================================
echo        解决方案：提供有效的APK文件
====================================================
echo.
echo 方法1：使用预编译APK（推荐）
echo -------------------------------------
echo 1. 在项目目录创建prebuilt文件夹（如果不存在）
echo    mkdir prebuilt

echo 2. 放入真实的APK文件并重命名为：
echo    AutoPowerManager-unsigned.apk

echo 3. 您可以通过以下方式获取真实APK：
echo    - 从项目开发者处获取

echo    - 或从已安装该应用的设备中导出：
echo      adb shell pm list packages | findstr AutoPowerManager

echo      找到包名后，执行：
echo      adb shell pm path 包名

echo      然后pull到电脑：
echo      adb pull /data/app/包名/base.apk prebuilt\AutoPowerManager-unsigned.apk

echo    - 或使用功能兼容的替代APK（确保来源安全）
echo.
echo 方法2：从源代码构建
-------------------------------------
echo 1. 确保已安装Java JDK 8或更高版本
echo 2. 确保项目目录中有gradlew.bat文件
echo 3. 执行构建命令：
echo    gradlew assembleDebug
echo 4. 构建完成后，输出文件将位于：
echo    app\build\outputs\apk\debug\app-debug.apk

echo.
echo ====================================================
echo        临时解决方案（仅用于测试）
echo ====================================================
echo 警告：以下方法仅用于测试脚本流程，不会生成真正可用的APK
echo.
echo 1. 创建一个空的APK文件：
echo    type nul > prebuilt\AutoPowerManager-unsigned.apk

echo 2. 注意：使用此方法，脚本将继续执行，但最终生成的文件不会是有效的APK
echo.
echo ====================================================
echo 操作完成！请选择合适的方法提供有效的APK文件，
echo 然后重新运行build_and_sign.bat。
echo ====================================================

pause