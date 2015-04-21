@echo off
set CLASSPATH=.;dist\Leaderms.jar;dist\exttools.jar;mina-core.jar;slf4j-api.jar;slf4j-jdk14.jar;mysql-connector-java-bin.jar;jpcap.jar
java -Drecvops=recvops.properties -Dsendops=sendops.properties -Dwzpath=wz\ tools.MonsterDropCreator false
pause