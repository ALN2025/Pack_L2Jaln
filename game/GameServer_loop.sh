#!/bin/bash
cd "$(dirname "$0")"

# exit codes of GameServer:
#  0 normal shutdown
#  2 reboot attempt

echo -e "\033[0;32m"

while :; do
	[ -f log/java0.log.0 ] && mv log/java0.log.0 "log/`date +%Y-%m-%d_%H-%M-%S`_java.log"
	[ -f log/stdout.log ] && mv log/stdout.log "log/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
	java -Dfile.encoding=UTF8 -Xmx8G -cp ./libs/*:../../L2jaln_JAR/l2jaln.jar com.l2jaln.gameserver.GameServer > log/stdout.log 2>&1
	[ $? -ne 2 ] && break
#	/etc/init.d/mysql restart
	sleep 10
done

echo -e "\033[0m"
