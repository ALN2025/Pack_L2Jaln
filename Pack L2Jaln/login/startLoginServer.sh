#!/bin/bash
cd "$(dirname "$0")"
java -Xmx32m -cp ./libs/*:../../L2jaln_JAR/l2jaln.jar com.l2jaln.loginserver.L2LoginServer



