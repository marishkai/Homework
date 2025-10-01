@echo off
echo Building DungeonMini...
echo.

echo Creating bin directory...
if not exist bin mkdir bin

echo Compiling Java files...
javac -d bin -encoding UTF-8 ^
  src/com/example/dungeon/Main.java ^
  src/com/example/dungeon/core/Game.java ^
  src/com/example/dungeon/core/Command.java ^
  src/com/example/dungeon/core/InvalidCommandException.java ^
  src/com/example/dungeon/core/SaveLoad.java ^
  src/com/example/dungeon/core/WorldInfo.java ^
  src/com/example/dungeon/model/Entity.java ^
  src/com/example/dungeon/model/GameState.java ^
  src/com/example/dungeon/model/Item.java ^
  src/com/example/dungeon/model/Key.java ^
  src/com/example/dungeon/model/Monster.java ^
  src/com/example/dungeon/model/Player.java ^
  src/com/example/dungeon/model/Potion.java ^
  src/com/example/dungeon/model/Room.java ^
  src/com/example/dungeon/model/Weapon.java

if %errorlevel% equ 0 (
    echo.
    echo Build successful!
    echo.
) else (
    echo.
    echo Build failed!
    echo.
)

pause