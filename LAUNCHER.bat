@echo off
color b
cls

title Server Starter
echo ############################  Launcher Initiated  ##############################
start /b launch_world.bat
echo                            Loading World Server...

ping localhost -w 10000 >nul
ping localhost -w 10000 >nul
start /b launch_login.bat
echo                             Loading Login Server...
echo ################################################################################
echo ############################    Configuration  #################################
ping localhost -w 10000 >nul
ping localhost -w 10000 >nul
start /b launch_channel.bat
echo ################################################################################
echo                            Loading Channel Server...
echo ############################   Channel List   ##################################

