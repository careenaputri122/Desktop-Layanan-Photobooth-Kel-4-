@echo off
setlocal enabledelayedexpansion

REM "SESUAIKAN_DENGAN_PATH_PROYEKMU"
cd /d "C:\Users\Acer\Downloads\Desktop-Layanan-Photobooth-Kel-4-"

REM Use the installed JavaFX SDK, Sesuaikan path JavaFX dibawah dengan komputer kamu!
set JAVAFX_HOME=C:\Program Files\Java\javafx-sdk-21.0.10

REM Run with correct module path and library path
java -Djava.library.path="%JAVAFX_HOME%\bin" ^
     -cp "bin;lib/*;%JAVAFX_HOME%\lib/*" ^
     --module-path "%JAVAFX_HOME%\lib" ^
     --add-modules javafx.controls,javafx.fxml ^
     MainApp

pause

REM jalankan di terminal dengan perintah: .\run.bat