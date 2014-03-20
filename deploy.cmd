call dist.cmd
if ERRORLEVEL 1 goto :eof
set /p pid=<\\srv5\BuildBoard\bin\RUNNING_PID
if NOT ('%pid%' == '') (
	utils\pskill -t \\srv5 %pid%
)

pushd target\universal
rmdir /S /Q BuildBoard-1.0
jar xf BuildBoard-1.0.zip
xcopy /E /Y BuildBoard-1.0\* \\srv5\BuildBoard\
del \\srv5\BuildBoard\bin\RUNNING_PID
popd
utils\psexec -d -w c:\BuildBoard\bin \\srv5 c:\BuildBoard\bin\buildboard.bat 
