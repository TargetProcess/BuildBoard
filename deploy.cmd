SET dest=\\srv-hv3
CALL dist.cmd
IF ERRORLEVEL 1 goto :eof
SET /P pid=<%dest%\BuildBoard\bin\RUNNING_PID
IF NOT ('%pid%' == '') (
	utils\pskill -t %dest% %pid%
)

PUSHD target\universal
RMDIR /S /Q BuildBoard-1.0
jar xf BuildBoard-1.0.zip
XCOPY /E /Y BuildBoard-1.0\* %dest%\BuildBoard\

DEL %dest%\BuildBoard\bin\RUNNING_PID
POPD

XCOPY /E /Y conf\BUILDBOARD_config.txt %dest%\BuildBoard\  
utils\psexec -s -d -w c:\BuildBoard\bin %dest% c:\BuildBoard\bin\buildboard.bat 
