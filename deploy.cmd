SET dest=\\srv-hv3
call dist.cmd
if ERRORLEVEL 1 goto :eof
set /p pid=<%dest%\BuildBoard\bin\RUNNING_PID
if NOT ('%pid%' == '') (
	utils\pskill -t %dest% %pid%
)

pushd target\universal
rmdir /S /Q BuildBoard-1.0
jar xf BuildBoard-1.0.zip
xcopy /E /Y BuildBoard-1.0\* %dest%\BuildBoard\
del %dest%\BuildBoard\bin\RUNNING_PID
popd
xcopy /E /Y conf\BUILDBOARD_config.txt %dest%\BuildBoard\  
utils\psexec -d -w c:\BuildBoard\bin %dest% c:\BuildBoard\bin\buildboard.bat 
