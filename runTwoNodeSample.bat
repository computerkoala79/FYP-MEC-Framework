start cmd /k "java -jar Orchestrator\target\Orchestrator-0.8.0-jar-with-dependencies.jar 8080"
timeout /t 4
start cmd /k "java -jar ServiceNode\target\ServiceNode-1.0-jar-with-dependencies.jar ServiceNode\target\perfectEdgeNode.json"
timeout /t 10
start cmd /k "java -jar ServiceNode\target\ServiceNode-1.0-jar-with-dependencies.jar ServiceNode\target\everythingBadEdgeNode.json"
rem echo.
rem echo Press any key to terminate all command prompts and processes...
rem pause > nul
rem taskkill /f /im cmd.exe > nul
