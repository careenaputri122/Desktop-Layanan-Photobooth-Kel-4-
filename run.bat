@echo off
setlocal enabledelayedexpansion

cd /d "C:\Users\anuge\OneDrive\Documents\GitHub\Desktop-Layanan-Photobooth-Kel-4-"

REM Use the installed JavaFX SDK
set JAVAFX_HOME=C:\Program Files\Java\javafx-sdk-21.0.11

REM Run with correct module path and library path
java -Djava.library.path="%JAVAFX_HOME%\bin" ^
     -cp "bin;lib/*;%JAVAFX_HOME%\lib/*" ^
     --module-path "%JAVAFX_HOME%\lib" ^
     --add-modules javafx.controls,javafx.fxml ^
     MainApp

pause
