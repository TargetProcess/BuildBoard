@echo off
CALL grunt typescript
IF %errorlevel% NEQ 0 (
   ECHO Grunt can't transpile typescript, error %errorlevel%
   EXIT /b %errorlevel%
)

REM > node less-compile.js "path with space\\main.less" "path with space\\login.less" causes malformed quotes error!
REM https://blogs.oracle.com/thejavatutorials/jdk-7u25:-solutions-to-issues-caused-by-changes-to-runtimeexec
SET JAVA_TOOL_OPTIONS="-Djdk.lang.Process.allowAmbiguousCommands=true"
CALL build.bat --Dconfig.resource=prod.conf dist
IF %errorlevel% NEQ 0 (
   ECHO Can't build backend, error %errorlevel%
   EXIT /b %errorlevel%
)
