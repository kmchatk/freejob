#!/bin/bash
mvn clean install -q
cp -v */target/*.jar ../FreeJob/bundle
if [[ -d ../FreeJob/felix-cache ]]
then
	echo "Deleting felix cache..."
	rm -rf ../FreeJob/felix-cache
fi
