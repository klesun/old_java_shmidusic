@echo off
set myvar="the list: "
for /r %%i in (.\libs\*) DO call :concat %%i
echo %myvar%
goto :eof

:concat
set myvar=%myvar%%1:
goto :eof