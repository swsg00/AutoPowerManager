@echo off

rem 导入CheckApkFile函数进行测试
call :CheckApkFile "%~dp0prebuilt\AutoPowerManager-unsigned.apk"

pause
goto :eof

rem 检查APK文件是否为文本占位符的函数
:CheckApkFile
set APK_FILE=%1
set IS_VALID_APK=false
set HEADER_BYTES=not_read
set FILE_SIZE=unknown

rem 读取文件的前4个字节，检查是否为ZIP格式(APK文件使用ZIP格式)
if exist "%APK_FILE%" (
    for /f "tokens=* usebackq" %%A in (`powershell -Command "if (Test-Path '%APK_FILE%') { $bytes = Get-Content -Encoding Byte -Path '%APK_FILE%' -TotalCount 4; $bytes -join ',' } else { 'file_not_found' }"`) do (
        set HEADER_BYTES=%%A
    )
    
    rem 检查文件大小
    for %%A in ("%APK_FILE%") do set FILE_SIZE=%%~zA
) else (
    echo 错误: 文件 '%APK_FILE%' 不存在!
    set IS_VALID_APK=false
    exit /b 0
)

rem ZIP文件头魔术数字: 80,75,3,4 (对应ASCII的PK\x03\x04)
if "%HEADER_BYTES%" equ "80,75,3,4" (
    set IS_VALID_APK=true
) else (
    echo 警告: 检测到的文件可能是文本占位符，不是有效的APK文件
    echo 文件头部字节: %HEADER_BYTES%
    echo 文件大小: %FILE_SIZE% 字节
    
    rem 尝试显示文件内容的前几行，以便更好地诊断
    if exist "%APK_FILE%" (
        echo 文件前几行内容:
        powershell -Command "if (Test-Path '%APK_FILE%') { Get-Content -Path '%APK_FILE%' -TotalCount 3 } else { '无法读取文件内容' }"
    )
)

echo 检查结果: APK有效=%IS_VALID_APK%
exit /b 0