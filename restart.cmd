SET dest=\\srv-hv3
set /p pid=<%dest%\BuildBoard\bin\RUNNING_PID
if NOT ('%pid%' == '') (
	utils\pskill -t %dest% %pid%
)
del %dest%\BuildBoard\bin\RUNNING_PID
utils\psexec -d -w c:\BuildBoard\bin %dest% c:\BuildBoard\bin\buildboard.bat 