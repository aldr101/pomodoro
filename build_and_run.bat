@echo off
:: Compile the Java file
javac -cp "lib\jlayer-1.0.1.jar" -d out src\PomodoroTimer.java

:: Run the program with all required classpaths (compiled classes, lib, and resources)
java -cp "out;lib\jlayer-1.0.1.jar;resources" PomodoroTimer

pause