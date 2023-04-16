start cmd /k "cd Core && mvn compile"
timeout /t 3
start cmd /k "cd Core && mvn install"
timeout /t 10
start cmd /k "cd Orchestrator && mvn package"
start cmd /k "cd ..\ServiceNode && mvn package"
timeout /t 5
start cmd /k "taskkill /f /im cmd.exe"