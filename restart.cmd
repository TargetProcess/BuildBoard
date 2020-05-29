SET dest=\\srv-hv3
SET /p pid=<%dest%\BuildBoard\bin\RUNNING_PID
IF NOT ('%pid%' == '') (
	utils\pskill -t %dest% %pid%
)
DEL %dest%\BuildBoard\bin\RUNNING_PID
utils\psexec -s -d -w c:\BuildBoard\bin %dest% c:\BuildBoard\bin\buildboard.bat 