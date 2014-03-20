call dist.cmd
if ERRORLEVEL 1 goto :eof
set /p pid=<\\srv5\BuildBoard\bin\RUNNING_PID
if NOT ('%pid%' == '') (
	utils\pskill -t \\srv5 %pid%
)

pushd target\universal
rmdir /S /Q BuildBoard
jar xf BuildBoard.zip
xcopy /E /Y BuildBoard\* \\srv5\BuildBoard\
del \\srv5\BuildBoard\bin\RUNNING_PID
popd
utils\psexec -d -w c:\BuildBoard\bin \\srv5 c:\BuildBoard\bin\buildboard.bat 
