# 如何获取有效的AutoPowerManager APK文件

本文档提供了解决"APK文件不存在!"错误的详细指南。当您运行`build_and_sign.bat`脚本时遇到此错误，这表明脚本无法找到有效的AutoPowerManager APK文件。

## 问题分析

`build_and_sign.bat`脚本在执行时会尝试以下两种方式获取APK文件：

1. 首先检查`prebuilt/AutoPowerManager-unsigned.apk`是否存在且有效
2. 如果预编译APK不存在或无效，则尝试从源代码构建

如果两种方式都失败，脚本将显示"APK文件不存在!"错误。

## 解决方案

### 方法1：使用预编译APK（推荐）

1. **检查prebuilt目录**：确保项目目录中有`prebuilt`文件夹
   ```
   mkdir prebuilt 2>nul
   ```

2. **提供真实的APK文件**：将真实的AutoPowerManager APK文件放入该文件夹，并确保文件名为`AutoPowerManager-unsigned.apk`

3. **获取真实APK的途径**：

   - **从开发者处获取**：联系项目开发者获取官方APK文件

   - **从设备导出**：如果您有已安装该应用的Android设备，可以使用以下ADB命令导出：
     ```bash
     # 查找包名
     adb shell pm list packages | findstr AutoPowerManager
     
     # 找到包名后，查看APK路径
     adb shell pm path 包名
     
     # 导出APK文件
     adb pull /data/app/包名/base.apk prebuilt\AutoPowerManager-unsigned.apk
     ```

   - **使用兼容替代**：如果无法获取原始APK，可以使用功能兼容的替代APK（确保来源安全）

### 方法2：从源代码构建

如果您有完整的项目源代码，可以直接构建APK：

1. **安装JDK**：确保已安装Java JDK 8或更高版本

2. **安装Gradle**：确保已安装Gradle或项目目录中包含`gradlew.bat`文件

3. **准备gradlew.bat**：如果项目目录中没有`gradlew.bat`文件，可以从以下位置获取：
   - 从Android Studio项目模板中复制
   - 从其他Gradle项目中复制
   - 或直接下载：`https://services.gradle.org/distributions/`

4. **执行构建**：
   ```bash
   # 使用gradlew构建
   gradlew assembleDebug
   
   # 或使用系统安装的gradle
   gradle assembleDebug
   ```

5. **构建完成后**，APK文件将位于：
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

6. **复制到预编译目录**：
   ```bash
   mkdir prebuilt 2>nul
   copy app\build\outputs\apk\debug\app-debug.apk prebuilt\AutoPowerManager-unsigned.apk
   ```

### 方法3：临时解决方案（仅用于测试）

**警告：以下方法仅用于测试脚本流程，不会生成真正可用的APK**

如果您只需要测试脚本的其他部分功能，可以创建一个空文件作为占位符：

```bash
mkdir prebuilt 2>nul
type nul > prebuilt\AutoPowerManager-unsigned.apk
```

注意：使用此方法，脚本将继续执行，但最终生成的文件不会是有效的APK，无法正常安装和使用。

## 常见问题排查

1. **文件权限问题**：确保您对项目目录有读写权限

2. **文件名拼写错误**：确保文件名完全正确：`AutoPowerManager-unsigned.apk`（区分大小写）

3. **APK文件无效**：如果APK文件存在但脚本仍报错，可能是因为APK文件无效
   - 检查文件大小，有效APK通常大于1MB
   - 使用文件管理器查看文件属性，确保不是文本文件

4. **构建环境问题**：如果选择从源代码构建，请确保：
   - Java环境配置正确：`java -version`显示正确版本
   - Gradle配置正确：`gradlew --version`显示正确版本
   - 项目依赖项已安装

## 后续步骤

获取有效的APK文件后，您可以重新运行`build_and_sign.bat`脚本。脚本将：

1. 验证APK文件的有效性
2. 复制到输出目录
3. 提供签名和安装选项

祝您使用愉快！