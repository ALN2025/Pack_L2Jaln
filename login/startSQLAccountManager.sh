#!/bin/bash
cd "$(dirname "$0")"
echo -e "\033[0;32m"
java -cp ./libs/*:../../L2jaln_JAR/l2jaln.jar com.l2jaln.loginserver.SQLAccountManager
echo -e "\033[0m"
