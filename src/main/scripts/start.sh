#!/bin/bash

# Define the path to th json generate by groovy script
REGISTRY_URLS="https://index.docker.io"

echo "Starting sample web application"
java -jar ${project.build.finalName}.${project.packaging} -p 80 -r $REGISTRY_URLS
echo "Application started"