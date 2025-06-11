#!/bin/bash
cd "$(dirname "$0")"
echo -e "\033[0;32m"
while true
do
	[ -f log/java0.log.0 ] && mv log/java0.log.0 "log/`date +%Y-%m-%d_%H-%M-%S`_java.log"
	[ -f log/stdout.log ] && mv log/stdout.log "log/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
	nice -n -2 java -Xmx32m -cp ./libs/*:../../L2jaln_JAR/l2jaln.jar com.l2jaln.loginserver.L2LoginServer > log/stdout.log 2>&1
	err=$?
#	/etc/init.d/mysql restart
	sleep 10;
done
echo -e "\033[0m"
