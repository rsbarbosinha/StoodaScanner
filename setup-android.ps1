$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"
$sdkDir = "$env:LOCALAPPDATA\Android\Sdk"
Write-Host "Creating Android SDK directory at $sdkDir..."
New-Item -ItemType Directory -Force -Path "$sdkDir\cmdline-tools\latest" | Out-Null

$zipPath = "$env:TEMP\cmdline-tools.zip"
Write-Host "Downloading Command Line Tools..."
Invoke-WebRequest -Uri "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip" -OutFile $zipPath

Write-Host "Extracting Command Line Tools..."
$extractPath = "$env:TEMP\cmdline-tools-extracted"
if (Test-Path $extractPath) { Remove-Item -Recurse -Force $extractPath }
Expand-Archive -Path $zipPath -DestinationPath $extractPath -Force

Write-Host "Moving to SDK folder..."
Move-Item -Path "$extractPath\cmdline-tools\*" -Destination "$sdkDir\cmdline-tools\latest" -Force

$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.10.7-hotspot"
$env:ANDROID_HOME = $sdkDir

Write-Host "Accepting licenses..."
$sdkmanager = "$sdkDir\cmdline-tools\latest\bin\sdkmanager.bat"
$yes = "y`n" * 10
$yes | & $sdkmanager --licenses | Out-Null

Write-Host "Updating local.properties in the project..."
$projectDir = "c:\Users\fly\Documents\trabalho facul\StoodaScanner"
$localPropertiesPath = "$projectDir\local.properties"
$sdkDirEscaped = $sdkDir -replace '\\', '\\' -replace ':', '\:'
Set-Content -Path $localPropertiesPath -Value "sdk.dir=$sdkDirEscaped"

Write-Host "Installing basic platform..."
& $sdkmanager "platforms;android-36" "platform-tools"

Write-Host "Done!"
