#!/bin/bash
mvn clean install -q
DATE=`date +%Y%m%d`
UPDATE=update-$DATE.zip
echo ${UPDATE}
zip ${UPDATE} -j */target/*.jar
