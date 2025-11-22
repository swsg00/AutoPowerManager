# PowerShell script to download Gradle 4.4 and set up wrapper

Write-Host "Downloading Gradle 4.4 binary..."

# Create temp directory
$tempDir = New-Item -ItemType Directory -Path "$env:TEMP\gradle44" -Force

# Download Gradle 4.4
$gradleUrl = "https://services.gradle.org/distributions/gradle-4.4-bin.zip"
$zipPath = "$tempDir\gradle-4.4-bin.zip"

Invoke-WebRequest -Uri $gradleUrl -OutFile $zipPath

Write-Host "Extracting Gradle 4.4..."
# Extract the zip file
Expand-Archive -Path $zipPath -DestinationPath $tempDir -Force

# Get the path to the gradle binary
$gradleBinDir = "$tempDir\gradle-4.4\bin"
$gradleExe = "$gradleBinDir\gradle.bat"

Write-Host "Setting up Gradle wrapper using downloaded Gradle 4.4..."
# Run gradle wrapper command
Set-Location -Path "$PSScriptRoot"
& "$gradleExe" wrapper --gradle-version 4.4

Write-Host "Cleaning up..."
# Clean up temp directory
Remove-Item -Path $tempDir -Recurse -Force

Write-Host "Gradle wrapper setup completed successfully!"
Write-Host "You can now run: .\gradlew.bat assembleDebug"