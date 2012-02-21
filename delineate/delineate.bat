@echo off
start "%JAVA_HOME%/bin/javaw" -jar delineate.jar ./settings/parameters-autotrace.xml ./settings/parameters-potrace.xml
