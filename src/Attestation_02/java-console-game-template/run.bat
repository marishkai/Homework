@echo off
chcp 65001 >nul
echo ========================================
echo    Запуск игры DungeonMini
echo ========================================
echo.

echo Проверяем наличие скомпилированных файлов...
if not exist bin (
    echo Папка 'bin' не найдена!
    echo Запустите сначала build.bat
    echo.
    pause
    exit /b 1
)

echo Запускаем игру...
echo.
java -Dfile.encoding=UTF-8 -cp bin com.example.dungeon.Main

echo.
pause