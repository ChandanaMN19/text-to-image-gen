@echo off
echo ========================================================
echo   Launching AI Text-to-Image Generator (Java Desktop UI)
echo ========================================================
echo.
mvn compile exec:java -Dexec.mainClass="com.imageng.gui.ImageGeneratorSwingApp"
pause
