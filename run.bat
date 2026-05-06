@echo off
setlocal

cd /d "C:\Users\Acer\Downloads\Desktop-Layanan-Photobooth-Kel-4-"

set JAVAFX_HOME=C:\Users\Acer\Downloads\openjfx-21.0.10_windows-x64_bin-sdk\javafx-sdk-21.0.10

java -Djava.library.path="%JAVAFX_HOME%\bin" ^
     --module-path "%JAVAFX_HOME%\lib" ^
     --add-modules javafx.controls,javafx.fxml ^
     -cp "target\classes;lib\*" ^
     MainApp

pause

REM jalankan di terminal dengan perintah: .\run.bat