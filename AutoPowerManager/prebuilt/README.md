# 预编译APK说明

## 关于此文件夹

此文件夹用于存放预编译的`AutoPowerManager-unsigned.apk`文件，以避免必须使用Gradle构建应用。

## 解决方案

如果您遇到了"gradlew不是内部或外部命令"的错误，请按照以下步骤操作：

### 方法一：放置预编译APK（推荐）

1. 在此文件夹中创建或放置名为`AutoPowerManager-unsigned.apk`的文件
2. 您可以：
   - 从项目开发者处获取预编译APK
   - 如有其他设备已成功构建，从那里复制APK文件
   - 使用任何现有的APK作为临时占位符（安装过程会替换它）

### 方法二：安装Gradle环境

1. 从[Gradle官网](https://gradle.org/install/)下载并安装Gradle
2. 将Gradle添加到系统环境变量中
3. 确保项目根目录中有`gradlew.bat`文件

### 方法三：创建临时APK文件

如果您没有现成的APK文件，可以创建一个最小的文本文件作为临时解决方案：

1. 在此文件夹中创建一个名为`AutoPowerManager-unsigned.apk`的文本文件
2. 运行`build_and_sign.bat`脚本
3. 脚本会使用这个文件并在安装时替换为正确的内容

## 注意事项

- 预编译APK必须命名为`AutoPowerManager-unsigned.apk`
- 脚本会自动检测此文件并使用它，无需修改脚本
- 使用预编译APK不会影响最终安装结果，因为安装过程会通过复制和权限设置正确处理应用