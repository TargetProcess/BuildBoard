call grunt typescript
call dist.cmd
if ERRORLEVEL 1 goto :eof
set /p pid=<\\srv5\BuildBoard-1.0-SNAPSHOT\bin\RUNNING_PID
if NOT ('%pid%' == '') (
	utils\pskill -t \\srv5 %pid%
)

pushd target\universal
rmdir /S /Q BuildBoard-1.0-SNAPSHOT
jar xf BuildBoard-1.0-SNAPSHOT.zip
xcopy /E /Y BuildBoard-1.0-SNAPSHOT\* \\srv5\BuildBoard-1.0-SNAPSHOT\
del \\srv5\BuildBoard-1.0-SNAPSHOT\bin\RUNNING_PID
popd
utils\psexec -d -w c:\BuildBoard-1.0-SNAPSHOT\bin \\srv5 c:\BuildBoard-1.0-SNAPSHOT\bin\buildboard.bat 
