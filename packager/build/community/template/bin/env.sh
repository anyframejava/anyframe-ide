#!/bin/sh
if [ -z "$ANYFRAME_HOME" ]; then
	echo "ERROR: Environment variable ANYFRAME_HOME not set."
else 
	export ANT_HOME=$ANYFRAME_HOME/ide/ant
	export ANT_OPTS="-Xms512m -Xmx512m"
	export ANYFRAME_OPTS="-Xms512m -Xmx512m -XX:MaxPermSize=512m"
	export PATH=$ANT_HOME/bin:$ANYFRAME_HOME/bin:$PATH
	export CLASSPATH=$ANYFRAME_HOME/ide/ant/lib/ant-launcher.jar:$ANYFRAME_HOME/ide/cli/lib/anyframe-ide-command-cli-2.3.1-SNAPSHOT.jar:.
	export MAINCLASS=org.anyframe.ide.command.cli.CLIAntRunner
fi