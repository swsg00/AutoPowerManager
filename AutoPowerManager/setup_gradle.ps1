# PowerShell script to download and set up Gradle wrapper

Write-Host "Setting up Gradle wrapper for AutoPowerManager..."

# Create temp directory for download
$tempDir = New-Item -ItemType Directory -Path "$env:TEMP\gradle_setup" -Force

# Download gradle-wrapper.jar
$jarUrl = "https://repo1.maven.org/maven2/org/gradle/wrapper/gradle-wrapper/1.0/gradle-wrapper-1.0.jar"
$jarPath = "$tempDir\gradle-wrapper.jar"

Write-Host "Downloading gradle-wrapper.jar..."
Invoke-WebRequest -Uri $jarUrl -OutFile $jarPath

# Copy to the project's gradle/wrapper directory
$projectJarPath = "$PSScriptRoot\gradle\wrapper\gradle-wrapper.jar"
Copy-Item -Path $jarPath -Destination $projectJarPath -Force

Write-Host "Gradle wrapper jar has been set up successfully!"
Write-Host "You can now run .\gradlew.bat assembleDebug"

# Clean up temp directory
Remove-Item -Path $tempDir -Recurse -Force