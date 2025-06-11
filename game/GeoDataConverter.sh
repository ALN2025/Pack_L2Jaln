#!/bin/bash
echo -e "\033[0;32m"
cd "$(dirname "$0")"
java -cp ./libs/*:../../L2jaln_JAR/l2jaln.jar com.l2jaln.gameserver.geoengine.GeoDataConverter
echo -e "\033[0m"

