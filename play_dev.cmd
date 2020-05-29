CALL build.bat debug -Dconfig.resource=dev.conf
IF %errorlevel% NEQ 0 (
   ECHO Can't build backend, error %errorlevel%
   EXIT /b %errorlevel%
)