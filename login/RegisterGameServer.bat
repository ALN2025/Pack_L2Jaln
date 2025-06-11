
@echo off
:: Configura a cor: fundo preto (0) e texto verde (A)
color 09
title L2Jaln Register GameServer
java -cp ./libs/*;../../L2jaln_JAR/l2jaln.jar com.l2jaln.gsregistering.GameServerRegister
pause
