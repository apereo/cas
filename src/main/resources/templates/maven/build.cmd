@echo off

@rem Check for mvn in path, use it if found, otherwise use maven wrapper
@set MAVEN_CMD=mvn
@WHERE /Q mvn
@IF %ERRORLEVEL% NEQ 0 set MAVEN_CMD=.\mvnw.bat

@if "%1" == "" call:help
@if "%1" == "copy" call:copy
@if "%1" == "clean" call:clean %2 %3 %4
@if "%1" == "package" call:package %2 %3 %4
@if "%1" == "bootrun"  call:bootrun %2 %3 %4
@if "%1" == "debug" call:debug %2 %3 %4
@if "%1" == "run" call:run %2 %3 %4
@if "%1" == "help" call:help

@rem function section starts here
@goto:eof

:copy
    set CONFIG_DIR=\etc\cas\config
    @echo "Creating configuration directory under %CONFIG_DIR%"
    if not exist %CONFIG_DIR% mkdir %CONFIG_DIR%

    @echo "Copying configuration files from etc/cas to /etc/cas"
    xcopy /S /Y etc\cas\* \etc\cas
@goto:eof

:help
    @echo "Usage: build.bat [copy|clean|package|run|debug|bootrun] [optional extra args for maven]"
@goto:eof

:clean
    call %MAVEN_CMD% clean %1 %2 %3
    exit /B %ERRORLEVEL%
@goto:eof

:package
    call %MAVEN_CMD% clean package -T 5 %1 %2 %3
    exit /B %ERRORLEVEL%
@goto:eof

:bootrun
    call %MAVEN_CMD% clean package spring-boot:run -T 5 %1 %2 %3
    exit /B %ERRORLEVEL%
@goto:eof

:debug
    call:package %1 %2 %3 & java -Xdebug -Xrunjdwp:transport=dt_socket,address=5000,server=y,suspend=n -jar target/cas.war
@goto:eof

:run
    call:package %1 %2 %3 & java -jar target/cas.war
@goto:eof
