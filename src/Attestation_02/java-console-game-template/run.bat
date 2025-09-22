@echo off
chcp 65001 > nul
echo Запуск игры DungeonMini...
echo.

if not exist bin (
    echo Папка bin не найдена. Запустите сначала build.bat
    pause
    exit /b 1
)

java -Dfile.encoding=UTF-8 -cp bin com.example.dungeon.core.Game

echo.
pause