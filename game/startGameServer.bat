
@echo off
title L2Jaln gameserver console

:: Configura a cor: fundo preto (0) e texto verde (A)
color 09
:start
REM -------------------------------------
REM Default parameters for a basic server.
java -Dfile.encoding=UTF8 -Xmx8G -cp ./libs/*;../../L2jaln_JAR/l2jaln.jar com.l2jaln.gameserver.GameServer
REM -------------------------------------
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin have restarted, please wait.
echo.
goto start
:error
echo.
echo Server have terminated abnormaly.
echo.
:end
echo.
echo Server terminated.
echo.
pause
