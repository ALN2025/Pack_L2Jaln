#!/bin/bash
cd "$(dirname "$0")"
echo -e "\033[0;32m"
java -Xmx32m -cp ./libs/*:../../L2jaln_JAR/l2jaln.jar com.l2jaln.loginserver.L2LoginServer
echo -e "\033[0m"



