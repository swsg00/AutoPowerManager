@echo off
rem 启用调试模式
echo 脚本开始执行，当前目录: %CD%

set PROJECT_DIR=%~dp0
set OUTPUT_DIR=%PROJECT_DIR%output
set GRADLEW_PATH=%PROJECT_DIR%gradlew.bat

echo 项目目录: %PROJECT_DIR%
echo 输出目录: %OUTPUT_DIR%

rem 创建输出目录
mkdir %OUTPUT_DIR% 2>nul
echo 输出目录创建完成

rem 检查Java环境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境!
    echo 请先安装Java环境
    pause
    exit /b 1
) else (
    echo Java环境检查通过
)

rem 移除cls命令，保留调试输出
echo ====================================================
echo        AutoPowerManager 构建与安装工具
echo ====================================================
echo.
echo 此脚本用于构建并安装AutoPowerManager应用。
echo.

rem 添加预编译APK检查前的调试信息
echo 检查是否存在预编译APK...
if exist "%PROJECT_DIR%prebuilt\AutoPowerManager-unsigned.apk" (
    echo 发现预编译APK文件: %PROJECT_DIR%prebuilt\AutoPowerManager-unsigned.apk
    
    rem 检查预编译APK是否为有效文件
    echo 调用CheckApkFile函数检查预编译APK...
    call :CheckApkFile "%PROJECT_DIR%prebuilt\AutoPowerManager-unsigned.apk"
    echo CheckApkFile函数返回完成，错误级别: %errorlevel%
    
    if "%IS_VALID_APK%" equ "true" (
        echo 预编译APK有效，准备复制...
        copy "%PROJECT_DIR%prebuilt\AutoPowerManager-unsigned.apk" "%OUTPUT_DIR%\AutoPowerManager-unsigned.apk" /y
        if %errorlevel% neq 0 (
            echo 错误: 复制预编译APK失败!
            pause
            exit /b 1
        )
        echo 预编译APK复制成功。
    ) else (
        echo 警告: 预编译APK文件似乎是一个文本占位符，不是有效的APK文件!
        echo 请提供真实的APK文件或使用构建功能。
        pause
        exit /b 1
    )
) else (
    echo 未找到预编译APK，准备构建应用...
    
    rem 构建应用
    echo 正在构建应用...
    
    rem 尝试使用相对路径调用gradlew
    if exist "%GRADLEW_PATH%" (
        echo 使用gradlew.bat构建: %GRADLEW_PATH%
        call "%GRADLEW_PATH%" assembleDebug
    ) else (
        echo 警告: 未找到gradlew.bat文件，尝试直接调用gradlew...
        call gradlew assembleDebug
    )

    if %errorlevel% neq 0 (
        echo 构建失败!
        echo 请确保已安装Gradle或在项目目录中添加gradlew.bat文件
        echo 或者可以在项目目录中创建prebuilt文件夹并放入真实的APK文件
        pause
        exit /b 1
    )
    
    echo 构建成功!
    
    rem 复制构建结果
    echo 复制构建结果...
    copy "%PROJECT_DIR%app\build\outputs\apk\debug\app-debug.apk" "%OUTPUT_DIR%\AutoPowerManager-unsigned.apk" /y
    if %errorlevel% neq 0 (
        echo 错误: 复制构建结果失败!
        pause
        exit /b 1
    )
    echo 构建结果复制成功。
)

echo 继续执行脚本...

rem 显示菜单
echo.
echo ====================================================
echo             请选择操作方式
echo ====================================================
echo 1. 使用系统签名（需要platform.pk8和platform.x509.pem）
echo 2. 安装到root设备（不需要系统签名文件）
echo 3. 退出
set /p choice="请选择 (1-3): "

if "%choice%" equ "1" (
    echo 选择了使用系统签名...
    rem 其余代码保持不变
)

rem 检查签名文件
    if not exist "%PROJECT_DIR%signapk.jar" (
        echo 错误: 未找到signapk.jar文件!
        echo 请将signapk.jar复制到项目根目录
        pause
        exit /b 1
    )

    if not exist "%PROJECT_DIR%platform.pk8" (
        echo 错误: 未找到platform.pk8文件!
        echo 请将platform.pk8复制到项目根目录
        pause
        exit /b 1
    )

    if not exist "%PROJECT_DIR%platform.x509.pem" (
        echo 错误: 未找到platform.x509.pem文件!
        echo 请将platform.x509.pem复制到项目根目录
        pause
        exit /b 1
    )

    echo 签名文件检查通过
    echo.
    echo 开始签名应用...
    java -jar "%PROJECT_DIR%signapk.jar" "%PROJECT_DIR%platform.x509.pem" "%PROJECT_DIR%platform.pk8" "%OUTPUT_DIR%\AutoPowerManager-unsigned.apk" "%OUTPUT_DIR%\AutoPowerManager.apk"

    if %errorlevel% neq 0 (
        echo 签名失败!
        pause
        exit /b 1
    )

    echo 签名成功!
    echo.
    echo ====================================================
    echo 签名完成!
    echo 生成文件:
    echo - 未签名APK: %OUTPUT_DIR%\AutoPowerManager-unsigned.apk
    echo - 已签名APK: %OUTPUT_DIR%\AutoPowerManager.apk
    echo ====================================================
    echo.
    echo 请按照README.md中的说明将应用加入到ROM中。
    echo.
    pause

) else if "%choice%" equ "2" (
    echo 正在安装到root设备...
    
    rem 检查设备连接
    echo 检查设备连接...
    adb devices ^| findstr /i "device" >nul
    if %errorlevel% neq 0 (
        echo 错误: 未找到已连接的设备!
        echo 请确保设备已通过USB连接并开启调试模式!
        pause
        exit /b 1
    )
    
    rem 检查root权限 - 更严格的检查方式
    echo 检查设备root权限...
    rem 首先检查su命令是否存在
    adb shell "command -v su >/dev/null 2>&1 || which su >/dev/null 2>&1 || type su >/dev/null 2>&1"
    if %errorlevel% neq 0 (
        echo 错误: 设备上未找到su命令!
        echo 请确保设备已正确root或安装了su命令。
        pause
        exit /b 1
    )
    
    rem 然后检查是否可以使用su命令执行命令
    adb shell "su -c 'echo root_check'" >nul 2>&1
    if %errorlevel% neq 0 (
        echo 错误: 设备没有root权限或未授权ADB root权限!
        echo 请确保设备已root并为ADB授权root权限。
        pause
        exit /b 1
    )
    
    rem 安装APK到系统目录
    echo 正在安装APK到系统目录...
    
    rem 推送APK到设备临时目录
    echo 正在推送APK到设备临时目录...
    adb push "%OUTPUT_DIR%\AutoPowerManager-unsigned.apk" /data/local/tmp/AutoPowerManager.apk
    if %errorlevel% neq 0 (
        echo 错误: APK推送失败!
        pause
        exit /b 1
    )
    echo APK推送成功。
    
    rem 检查并重新挂载系统分区为可写 - 使用更可靠的方式
    echo 检查系统分区权限...
    rem 使用多种可能的重新挂载命令尝试
    adb shell "su -c 'mount -o remount,rw /system 2>/dev/null || mount -o remount,rw / 2>/dev/null || mount -o remount,rw /system_root 2>/dev/null'"
    if %errorlevel% neq 0 (
        echo 错误: 无法将系统分区重新挂载为可写!
        echo 系统分区可能受到保护或设备限制。
        echo 解决方案:
        echo 1. 确保您的设备已正确root
        echo 2. 某些设备可能需要特殊的重新挂载命令
        echo 3. 尝试使用Magisk Manager的Systemless模式安装
        echo 4. 考虑在Recovery模式下安装
        pause
        exit /b 1
    )
    
    rem 测试系统分区是否真的可写
    adb shell "su -c 'touch /system/test_write_permission 2>/dev/null && rm /system/test_write_permission 2>/dev/null'"
    if %errorlevel% neq 0 (
        echo 错误: 系统分区仍然是只读的!
        echo 虽然重新挂载命令执行成功，但实际上无法写入系统分区。
        echo 这可能是由于设备限制或文件系统保护机制导致的。
        echo 请尝试其他安装方法，如Magisk的Systemless模式或Recovery模式。
        pause
        exit /b 1
    )
    echo 系统分区已成功设置为可写。
    
    rem 使用root权限创建目标目录
    echo 正在创建安装目录...
    adb shell "su -c 'mkdir -p /system/priv-app/AutoPowerManager 2>/dev/null'"
    if %errorlevel% neq 0 (
        echo 错误: 创建安装目录失败!
        echo 系统分区可能仍然是只读的或权限不足。
        pause
        exit /b 1
    )
    
    rem 验证目录是否创建成功 - 使用更严格的验证
    adb shell "su -c 'test -d /system/priv-app/AutoPowerManager && echo Directory exists || echo Directory does not exist'" >nul 2>&1
    if %errorlevel% neq 0 (
        echo 错误: 验证目录创建失败!
        echo 无法确认安装目录是否成功创建。
        pause
        exit /b 1
    )
    echo 安装目录创建成功。
    
    rem 复制APK到系统目录
    echo 正在复制APK到系统目录...
    adb shell "su -c 'cp /data/local/tmp/AutoPowerManager.apk /system/priv-app/AutoPowerManager/AutoPowerManager.apk 2>/dev/null'"
    if %errorlevel% neq 0 (
        echo 错误: 复制APK到系统目录失败!
        echo 检查错误原因: 可能是存储空间不足或文件系统错误。
        pause
        exit /b 1
    )
    
    rem 验证APK是否成功复制
    adb shell "su -c 'test -f /system/priv-app/AutoPowerManager/AutoPowerManager.apk && echo File exists || echo File does not exist'" >nul 2>&1
    if %errorlevel% neq 0 (
        echo 错误: APK文件复制失败!
        echo 虽然复制命令执行完成，但目标文件不存在。
        pause
        exit /b 1
    )
    echo APK复制成功。
    
    rem 设置APK文件权限
    echo 正在设置文件权限...
    adb shell "su -c 'chmod 644 /system/priv-app/AutoPowerManager/AutoPowerManager.apk 2>/dev/null'"
    if %errorlevel% neq 0 (
        echo 错误: 设置文件权限失败!
        pause
        exit /b 1
    )
    
    rem 设置文件所有者
    adb shell "su -c 'chown root:root /system/priv-app/AutoPowerManager/AutoPowerManager.apk 2>/dev/null'"
    if %errorlevel% neq 0 (
        echo 错误: 设置文件所有者失败!
        pause
        exit /b 1
    )
    
    rem 设置目录权限
    adb shell "su -c 'chmod 755 /system/priv-app/AutoPowerManager 2>/dev/null'"
    if %errorlevel% neq 0 (
        echo 错误: 设置目录权限失败!
        pause
        exit /b 1
    )
    echo 权限设置成功。
    
    rem 最终验证安装结果 - 最关键的验证步骤
    echo 正在验证安装结果...
    adb shell "su -c 'ls -la /system/priv-app/AutoPowerManager/AutoPowerManager.apk'" >nul 2>&1
    if %errorlevel% neq 0 (
        echo 错误: 安装验证失败!
        echo APK文件似乎未成功安装到目标位置。
        echo 请使用以下命令手动检查:
        echo   adb shell "su -c 'ls -la /system/priv-app/'"
        pause
        exit /b 1
    )
    
    rem 验证文件大小和权限
    adb shell "su -c 'stat -c "%s" /system/priv-app/AutoPowerManager/AutoPowerManager.apk'" >nul 2>&1
    if %errorlevel% neq 0 (
        echo 警告: 无法验证APK文件大小，但文件似乎存在。
    )
    
    echo 安装成功!
    echo.
    echo ====================================================
    echo 应用已成功安装到系统目录!
    echo 安装路径: /system/priv-app/AutoPowerManager/
    echo ====================================================
    echo.
    echo 请重启设备以确保应用正常运行。
    pause
    
) else if "%choice%" equ "3" (
    echo 退出脚本...
    exit /b 0
) else (
    echo 无效的选择，请输入1、2或3。
    pause
    exit /b 1
)

rem 检查APK文件是否为文本占位符的函数
:CheckApkFile
set APK_FILE=%1
set IS_VALID_APK=false

echo 进入CheckApkFile函数，检查文件: %APK_FILE%

rem 首先检查文件是否存在
if not exist "%APK_FILE%" (
    echo 错误: APK文件不存在!
    exit /b 1
) else (
    echo 文件存在: %APK_FILE%
)

rem 获取文件大小（确保变量初始化为空）
set "FILE_SIZE="
for %%A in ("%APK_FILE%") do set "FILE_SIZE=%%~zA"
echo 文件大小: %FILE_SIZE% 字节

rem 简单检查文件大小是否为0或过小
if not defined FILE_SIZE (
    echo 警告: 无法获取文件大小
) else if %FILE_SIZE% leq 0 (
    echo 警告: 文件大小为0字节，这不是有效的APK文件
    exit /b 1
) else (
    echo 文件大小检查通过
)

rem 简化的ZIP头部检查 - 使用更直接的PowerShell命令并修复魔术数字检查
echo 执行ZIP头部检查...
powershell -Command "$path='%APK_FILE%'; $bytes=[System.IO.File]::ReadAllBytes($path); if ($bytes.Length -ge 4) { $header = [System.BitConverter]::ToString($bytes[0..3]).Replace('-',','); Write-Host '文件头部字节: ' $header; if ($header -eq '50,4B,03,04') { Write-Host 'APK文件有效'; exit 0 } else { Write-Host 'APK文件无效 - 不是有效的ZIP格式'; exit 1 } } else { Write-Host '文件太小，不是有效的APK文件'; exit 1 }"

if %errorlevel% equ 0 (
    set IS_VALID_APK=true
    echo ZIP头部检查通过，APK文件有效
) else (
    echo 警告: 检测到的文件可能是文本占位符，不是有效的APK文件
    echo 文件大小: %FILE_SIZE% 字节
    
    rem 尝试显示文件内容的前几行进行诊断
    echo 尝试读取文件内容...
    type "%APK_FILE%" | findstr /n . | findstr "^1: ^2: ^3:"
)

rem 移除之前的exit /b 0命令，让函数正确返回状态
if "%IS_VALID_APK%" equ "true" (
    exit /b 0
) else (
    exit /b 1
)