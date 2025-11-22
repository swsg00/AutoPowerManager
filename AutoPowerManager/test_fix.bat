@echo off
cls
echo 测试CheckApkFile函数修复...
echo.

set APK_FILE=%~dp0prebuilt\AutoPowerManager-unsigned.apk

rem 检查文件是否存在
if exist "%APK_FILE%" (
    echo 文件存在: %APK_FILE%
    
    rem 读取文件大小
    for %%A in ("%APK_FILE%") do (
        echo 文件大小: %%~zA 字节
    )
    
    rem 使用PowerShell读取前4个字节
    powershell -Command "$bytes = Get-Content -Encoding Byte -Path '%APK_FILE%' -TotalCount 4; echo '文件头部字节: ' $bytes -join ','"
    
    rem 显示文件前几行内容
    echo.
    echo 文件前几行内容:
    powershell -Command "Get-Content -Path '%APK_FILE%' -TotalCount 3"
) else (
    echo 错误: 文件不存在!
)

echo.
echo 测试完成。请按任意键退出...
pause >nul