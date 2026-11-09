$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$nodeCmd = Get-Command node -ErrorAction SilentlyContinue
if (!$nodeCmd) { throw 'Node.js 22.12 oder neuer installieren.' }
if (!$env:JAVA_HOME -and (Test-Path 'C:/Program Files/Java/jdk-24')) { $env:JAVA_HOME = 'C:/Program Files/Java/jdk-24' }
$mavenCmd = Get-Command mvn -ErrorAction SilentlyContinue
$mavenPath = if ($mavenCmd) { $mavenCmd.Source } else { Get-ChildItem "$env:USERPROFILE/.m2/wrapper/dists" -Filter mvn.cmd -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty FullName }
if (!$mavenPath) { throw 'Maven 3.6.3 oder neuer installieren und mvn zum PATH hinzufügen.' }
if (!(Test-Path 'frontend/node_modules/vite/bin/vite.js')) { throw 'Zuerst im Ordner frontend: npm install oder pnpm install ausführen.' }
if (Test-NetConnection localhost -Port 8081 -InformationLevel Quiet -WarningAction SilentlyContinue) { throw 'Port 8081 ist belegt. Falls Connect4 bereits läuft, öffne http://localhost:5174.' }
$backendProcess = Start-Process -FilePath $mavenPath -ArgumentList 'spring-boot:run' -WorkingDirectory "$PSScriptRoot/backend" -WindowStyle Hidden -RedirectStandardOutput "$PSScriptRoot/backend.log" -RedirectStandardError "$PSScriptRoot/backend-error.log" -PassThru
try {
 Write-Host 'Connect4 startet: http://localhost:5174'
 Write-Host 'Mit Strg+C beenden. Backend-Ausgaben stehen in backend.log.'
 Set-Location "$PSScriptRoot/frontend"
 & $nodeCmd.Source node_modules/vite/bin/vite.js --host 0.0.0.0
} finally {
 # Stop only the process tree created by this launcher.
 if (!$backendProcess.HasExited) { & taskkill /PID $backendProcess.Id /T /F | Out-Null }
}
