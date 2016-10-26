@echo off
if "%ANYFRAME_HOME%" == "" goto noAnyframeHome

set ANT_HOME=%ANYFRAME_HOME%\ide\ant
set ANT_OPTS=-Xms512m -Xmx512m
set ANYFRAME_OPTS=-Xms512m -Xmx512m -XX:MaxPermSize=512m
set PATH=%ANT_HOME%\bin;%ANYFRAME_HOME%\bin;%PATH%
set CLASSPATH=%ANYFRAME_HOME%\ide\ant\lib\ant-launcher.jar;%ANYFRAME_HOME%\ide\cli\lib\anyframe-ide-command-cli-2.3.1-SNAPSHOT.jar;
set MAINCLASS=org.anyframe.ide.command.cli.CLIAntRunner
goto end

:noAnyframeHome
echo ERROR: Environment variable ANYFRAME_HOME not set.
goto end

:end

