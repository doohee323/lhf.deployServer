rem @echo off

rem ###########################################################
rem # Batch Script
rem # usage : execRemote.bat execRemote all
rem ###########################################################

set arg1=%1
set arg2=%2

if "%arg1%" equ "" (set arg1="execRemote")
if "%arg2%" equ "" (set arg2="all")
echo %arg1% %arg2%

set BASE_DIR=%cd%
rem set JAVA_HOME=C:\LHF_IDE\tools\jdk1.7.0_21
set JAVA_HOME=C:\LHF_IDE\tools\jdk1.7.0_21
rem set JAVA_HOME=C:\LHF_IDE\tools\jdk1.6.0_30_x64\bin\javac.exe
set HOME_DIR=C:\LHF_IDE\workspace\lhf.launcher
set WORK_DIR=%HOME_DIR%\src

set GROOVY_HOME=C:\groovy-2.1.3
set ADDED_LIB=%HOME_DIR%\lib

set GROOVY_SHELL=groovy
set MAIN_CLASS=lhf\launcher\cmd\LaunchCmd.groovy
set PATH=%PATH%;%GROOVY_HOME%\bin

rem ======= working dir =================
cd %WORK_DIR%

rem ======= CLASSPATH =================

set CLASSPATH=.
FOR %%F IN (%GROOVY_HOME%\lib\*.jar) DO call :addcp %%F
FOR %%F IN (%ADDED_LIB%\*.jar) DO call :addcp %%F
goto extlibe

:addcp
set CLASSPATH=%CLASSPATH%;%1
goto :eof

:extlibe
set CLASSPATH=%CLASSPATH%;%HOME_DIR%\bin

rem ======= =================
rem echo %GROOVY_SHELL% %MAIN_CLASS% %arg1% %arg2% %3 %4 %5
%GROOVY_SHELL% %MAIN_CLASS% %arg1% %arg2% %3 %4 %5

cd %BASE_DIR%
