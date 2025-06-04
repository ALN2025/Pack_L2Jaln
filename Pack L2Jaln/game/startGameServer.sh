#!/bin/bash
cd "$(dirname "$0")"
java -Dfile.encoding=UTF8 -Xmx8G -cp ./libs/*:../../L2jaln_JAR/l2jaln.jar com.l2jaln.gameserver.GameServer

