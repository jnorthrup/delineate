#!/bin/sh

exec "${JAVA_HOME}/bin/java" -jar delineate.jar ./settings/parameters-autotrace.xml ./settings/parameters-potrace.xml
