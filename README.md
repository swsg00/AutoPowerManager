# AutoPowerManager - 系统级自动关机服务

## 功能概述

AutoPowerManager是一个系统级应用，用于在指定时间范围内自动关闭设备，具有以下功能：

1. 在配置的时间范围内自动检查并关机
2. 在时间范围内开机10分钟后自动关机
3. 关机前10分钟显示通知提醒（可关闭）
4. 通过拨号盘输入`*#8495162#*`开关服务
5. 通过拨号盘输入`*#8495162#*HHMM-HHMM*`修改关机时间范围
6. 强大的防卸载和持久化机制
7. 隐藏应用图标，避免被用户发现
8. 确保关机进程不可终止，即使通知被关闭

## 安装方式

### 关于APK文件

**重要说明：** 当前目录中的 `AutoPowerManager-unsigned.apk` 文件是一个文本占位符文件，不是有效的APK文件。build_and_sign.bat脚本现在已经增加了APK有效性检查，会自动识别并阻止使用占位符文件进行安装。

## 获取有效的AutoPowerManager APK文件

您可以通过以下方法获取有效的APK文件：

### 方法一：使用预编译APK（推荐）

1. 确保项目根目录中存在 `prebuilt` 文件夹
2. 将有效的APK文件放入该文件夹，并命名为 `AutoPowerManager-unsigned.apk`
3. 有效APK的获取途径：
   - 从项目开发者处直接获取
   - 如果您有其他设备已成功构建此应用，可从该设备导出APK
   - 使用类似功能的兼容APK作为替代（请注意兼容性问题）
4. 运行 build_and_sign.bat 脚本，它会自动检测并使用prebuilt目录中的APK

### 方法二：从源代码构建APK

如果您需要从头构建应用，请按照以下步骤操作：

1. **安装必要环境**：
   - 安装Java开发环境(JDK) 1.8或更高版本
   - 从 [Gradle官网](https://gradle.org/install/) 下载并安装Gradle
   - 将Gradle添加到系统环境变量中

2. **准备构建环境**：
   - 确保项目根目录中有 `gradlew.bat` 文件
   - 如果没有，您可以从其他Android项目复制或下载

3. **执行构建**：
   - 运行 build_and_sign.bat 脚本
   - 脚本会自动尝试使用Gradle构建APK
   - 构建成功后会生成有效APK文件并存储在output目录中

### 方法三：临时解决方案（不推荐）

**注意：这不是真实的APK，仅用于测试安装流程**

如果您只是想测试安装流程而不需要实际功能，可以：
1. 在prebuilt目录中创建一个最小的文本文件作为临时解决方案
2. 但请记住，这不会提供任何实际功能，应用将无法正常运行

## 安装方法

### 方式二：Root设备一键安装（推荐，无需系统签名）

**重要提示**：此方法是在电脑上运行bat脚本，不是在手机上运行！脚本会自动处理所有安装步骤。

#### 准备工作

1. 确保您的电脑已安装：
   - Java运行环境（JRE）
   - Android SDK Platform Tools（包含adb工具）
   - Gradle（可选，如果没有安装或找不到gradlew，可以使用预编译APK）

2. 可选：预编译APK（避免构建问题）
   - 如果您遇到"gradlew不是内部或外部命令"的错误，可以使用预编译APK
   - 脚本会自动检测`prebuilt\AutoPowerManager-unsigned.apk`文件
   - 我们已经在prebuilt目录提供了临时占位文件，让您可以立即运行脚本

3. 确保您的手机：
   - 已Root并安装了Magisk
   - 已连接到电脑
   - 已启用USB调试
   - 已授权ADB获取Root权限

#### 安装步骤

1. 在电脑上找到`build_and_sign.bat`文件（位于项目根目录）
2. 双击运行该批处理脚本
3. 等待脚本自动编译应用
4. 在脚本显示的菜单中，输入`2`选择"安装到已root设备（无需系统签名文件）"
5. 按照脚本提示完成操作：
   - 脚本会自动检查设备连接状态
   - 脚本会自动检查设备Root权限
   - 脚本会自动检查系统分区是否可写并尝试自动挂载为可写
   - 脚本会自动将应用安装到系统目录
   - 脚本会自动设置正确的文件权限
   - 脚本会自动复制启动脚本到系统目录
   - 脚本会自动验证安装结果

**重要提示**：
- 修改后的脚本包含**增强的错误检查机制**，会对每个安装步骤进行严格验证
- 脚本现在可以**准确检测并报告安装失败**，避免显示"安装成功"但实际失败的情况
- 系统分区可写性检查更加可靠，通过实际写入测试来确认挂载状态
- 每个关键操作（目录创建、文件复制、权限设置等）后都有**明确的成功/失败反馈**
- 如果出现安装失败，脚本会显示详细的错误原因和针对性的解决建议

**安装前注意事项**：
1. 确保设备已正确root，并且ADB可以获取root权限
2. 某些设备可能需要特殊的系统分区挂载命令或使用Magisk模块
3. 如果遇到系统分区只读问题，请尝试以下方法：
   - 尝试使用Magisk Manager的Systemless模式安装
   - 在Recovery模式下执行安装
   - 使用MT管理器手动复制APK文件
   - 检查设备是否支持系统分区写入（某些厂商可能锁定了系统分区）

#### 工作原理

1. 脚本先在电脑上编译应用程序
2. 将编译好的APK推送到手机的临时目录
3. 使用Root权限将APK移动到`/system/priv-app`系统目录
4. 设置正确的文件权限，使应用获得系统级权限
5. 复制启动脚本确保服务随系统启动

**为什么不需要系统签名**：通过Root权限直接将应用安装到系统目录，应用会自动获得系统级权限，无需使用系统签名文件。

## 集成到ROM

### 方法一：直接放置到系统目录

将签名后的APK复制到system/priv-app目录并设置正确权限（参考方式一的安装步骤）。

### 方法二：集成到ROM构建系统

按照README中的说明，将项目集成到ROM源码树中。

## 使用说明

### 开关服务

在拨号盘输入以下代码来开关服务：
```
*#8495162#*
```

### 修改时间范围

在拨号盘输入以下格式来修改关机时间范围：
```
*#8495162#*HHMM-HHMM*
```
其中：
- HHMM：24小时制时间格式
- 例如：`*#8495162#*2200-0700*` 表示时间范围为晚上10点到早上7点

### 默认时间范围

默认的限制时间范围是：22:00 - 07:00

### 配置文件位置

- 时间范围配置文件：`/data/system/autopower_timer_config.prop`
- 服务状态配置文件：`/data/system/autopower_service_status.prop`

## 验证安装成功

**重要提示**：不要仅依赖脚本的成功提示，请始终按照以下步骤验证实际安装结果！

安装完成后，您必须通过以下方法验证AutoPowerManager是否真的成功安装：

### 1. 检查安装目录

**这是最基本且最关键的检查步骤**：
```bash
adb shell ls -la /system/priv-app/AutoPowerManager/
```

如果命令返回错误或找不到目录，则表示安装失败，即使脚本显示"安装成功"。

### 2. 检查APK文件和权限

```bash
# 检查APK文件是否存在
adb shell ls -la /system/priv-app/AutoPowerManager/AutoPowerManager.apk

# 验证文件权限
adb shell stat /system/priv-app/AutoPowerManager/AutoPowerManager.apk | grep Mode
```

确保：
- APK文件确实存在
- 文件权限为644
- 目录权限为755
- 文件所有者为root:root

### 3. 功能验证

- 重启设备后，使用拨号盘输入`*#8495162#*`，检查是否可以开关服务
- 使用拨号盘输入`*#8495162#*HHMM-HHMM*`，尝试修改关机时间范围
- 查看是否能收到应用通知

### 4. 配置文件检查

```bash
# 检查配置文件是否已创建
adb shell ls -la /data/system/AutoPowerManager.xml

# 查看配置文件内容
adb shell cat /data/system/AutoPowerManager.xml
```

### 5. 进程检查

```bash
# 检查应用进程是否在运行
adb shell ps | grep com.autopower.manager
```

如果看不到进程，请重启设备后再检查。如果重启后仍然看不到进程，可能是安装失败或配置问题。

## 注意事项

1. 此应用需要系统权限，必须安装在`/system/priv-app`目录
2. 应用图标默认隐藏，无法从应用抽屉中找到
3. 重启设备后服务会自动启动
4. 关机前的通知可以关闭，但关机进程不会被终止
5. 为了确保稳定性，建议不要轻易修改核心代码

## 故障排除

如果遇到任何问题，请按照以下步骤进行排查和解决：

### 1. 安装问题排查

#### 系统分区只读错误
- **最常见症状**：
  - 脚本显示"安装成功"，但实际目录不存在
  - 出现"Read-only file system"错误信息
  - 目录创建失败但脚本仍显示成功
  - 复制APK失败但脚本仍显示成功
  - 通过`adb shell ls /system/priv-app/AutoPowerManager/`命令找不到目录

- **解决方法**：
  1. **手动重新挂载系统分区**（基础方法）：
     ```bash
     adb shell su -c "mount -o remount,rw /system"
     # 或尝试以下替代命令
     adb shell su -c "mount -o remount,rw /"
     adb shell su -c "mount -o rw,remount -t auto /system"
     ```

  2. **使用Magisk Manager的Systemless模式**（推荐给现代设备）：
     - 安装并打开Magisk Manager
     - 进入"模块"部分，点击"安装模块"
     - 搜索并安装"Systemless Hosts"或类似模块
     - 重启设备
     - 尝试创建一个Magisk模块来安装应用

  3. **Recovery模式安装**（适用于系统分区完全锁定的设备）：
     - 进入设备的Recovery模式
     - 连接设备到电脑
     - 使用Recovery的ADB功能执行安装命令：
       ```bash
       # 在Recovery中挂载系统分区
       adb shell mount /system
       # 创建目录并复制APK
       adb shell mkdir -p /system/priv-app/AutoPowerManager/
       adb push AutoPowerManager-unsigned.apk /system/priv-app/AutoPowerManager/AutoPowerManager.apk
       adb shell chmod 644 /system/priv-app/AutoPowerManager/AutoPowerManager.apk
       adb shell chown root:root /system/priv-app/AutoPowerManager/AutoPowerManager.apk
       adb shell chmod 755 /system/priv-app/AutoPowerManager
       ```

  4. **使用MT管理器手动安装**（适用于不想使用电脑的情况）：
     - 复制APK文件到手机内部存储
     - 安装MT管理器（需Root权限）
     - 打开MT管理器，导航到APK所在位置
     - 复制APK到`/system/priv-app/AutoPowerManager/`（需要先创建目录）
     - 长按APK文件，选择"权限"，设置为644
     - 长按目录，选择"权限"，设置为755
     - 设置所有者为root:root

  5. **检查设备厂商限制**：
     - 某些厂商（如小米、华为、OPPO等）可能限制了系统分区的写入
     - 请搜索特定设备型号的系统分区解锁方法
     - 某些设备可能需要特定的ROM或内核才能支持系统分区写入

#### 权限问题
- **症状**：
  - 无法获取Root权限
  - 权限被拒绝错误
  - 脚本无法执行su命令

- **解决方法**：
  1. 确认设备已正确Root（推荐使用Magisk）
  2. 检查并确保在设备上为ADB授予了Root权限
  3. 尝试在设备上手动运行su命令并授权
  4. 重新安装Magisk并确保正确配置
  5. 某些设备可能需要特定的Root方法或解锁引导加载程序

### 2. 安装验证步骤

如果脚本显示安装成功但实际检查发现目录不存在，请执行以下验证步骤：

```bash
# 1. 确认系统分区是否真的可写
adb shell "su -c 'touch /system/test_write && rm /system/test_write || echo "系统分区仍然是只读的"'"

# 2. 手动检查安装目录是否存在
adb shell "su -c 'ls -la /system/priv-app/AutoPowerManager/'"

# 3. 如果目录不存在，手动创建并验证
adb shell "su -c 'mkdir -p /system/priv-app/AutoPowerManager/'"
adb shell "su -c 'ls -la /system/priv-app/AutoPowerManager/'"

# 4. 手动复制APK并设置权限
adb push AutoPowerManager-unsigned.apk /data/local/tmp/
adb shell "su -c 'cp /data/local/tmp/AutoPowerManager-unsigned.apk /system/priv-app/AutoPowerManager/AutoPowerManager.apk'"
adb shell "su -c 'chmod 644 /system/priv-app/AutoPowerManager/AutoPowerManager.apk'"
adb shell "su -c 'chown root:root /system/priv-app/AutoPowerManager/AutoPowerManager.apk'"
adb shell "su -c 'chmod 755 /system/priv-app/AutoPowerManager'"

# 5. 最终验证
adb shell "su -c 'ls -la /system/priv-app/AutoPowerManager/'"
```

### 3. 应用功能问题排查

如果应用已成功安装但功能不正常，请检查：

```bash
# 查看应用日志
adb logcat | grep -i "AutoPowerManager\|autopower"

# 检查应用是否在运行
adb shell ps | grep -i "autopower"

# 检查配置文件
adb shell ls -la /data/system/AutoPowerManager.xml
adb shell cat /data/system/AutoPowerManager.xml

# 检查服务状态
adb shell dumpsys activity services | grep -i "AutoPowerManager"

# 尝试手动启动服务
adb shell am startservice -n com.autopower.manager/.AutoShutdownService
```

### 4. 特殊情况处理

#### 脚本显示成功但实际失败
- 这是最常见的问题！
- 不要仅依赖脚本的成功提示，**始终**按照验证安装成功部分的步骤进行检查
- 如果目录不存在，尝试使用上述手动安装方法
- 在某些设备上，`/system`可能实际上是一个符号链接或使用不同的挂载点

#### 设备重启后应用丢失
- 这通常表明系统分区是临时挂载为可写的，重启后恢复为只读
- 考虑使用Magisk的Systemless模式或Recovery模式进行安装
- 某些设备使用"A/B分区"，可能需要特殊的安装方法

### 5. 高级用户解决方案

对于高级用户，可以尝试创建一个简单的Magisk模块：

1. 创建以下目录结构：
   ```
   AutoPowerManager_Module/
   ├── META-INF/
   │   └── com/
   │       └── google/
   │           └── android/
   │               ├── update-binary
   │               └── updater-script
   └── system/
       └── priv-app/
           └── AutoPowerManager/
               └── AutoPowerManager.apk
   ```

2. 复制APK到相应位置
3. 创建简单的updater-script文件
4. 将模块打包为ZIP文件
5. 通过Magisk Manager安装此模块

这种方法可以绕过系统分区只读问题，因为Magisk会在系统启动时通过覆盖挂载来添加文件。

## 开发团队

## 版权和许可证

### 版权声明
AutoPowerManager 应用及相关代码、文档版权归开发团队所有。

### 许可证信息
本项目采用开源许可证发布，具体许可证类型可在项目的 LICENSE 文件中查看。

### 使用条款
- 本软件仅供个人使用，未经授权不得用于商业目的
- 请遵守当地法律法规，合理使用本软件
- 软件使用过程中如有任何问题，请通过指定渠道反馈

## 免责声明
使用本软件可能会对您的设备产生影响，包括但不限于系统稳定性问题、数据丢失等。请确保在使用前备份重要数据，并自行承担使用风险。

开发团队不对因使用本软件造成的任何损失负责。

## 更新日志

### 最新版本
- 优化系统分区只读问题的解决方案
- 完善安装验证步骤
- 更新故障排除指南
- 改进脚本错误处理机制

- 开发者：AutoPower Team
- 版本：1.1.0
- 支持系统：Android 8.1 (ColorOS 5.2.1)
- 最后更新：添加Root设备一键安装功能和拨号盘修改时间范围功能