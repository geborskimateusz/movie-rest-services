#!/usr/bin/env bash

echo "Clean Project Docker Environment Start"

echo "Moving into project root directory"
cd ../
echo "Current directory is: $(pwd)"

echo "Stopping already running microserices container"
docker kill $(docker ps -a -q)

echo "Removing all microservices containers"
docker rm $(docker ps -a -q)

echo "Removing latest microservices images"
docker rmi $(docker images | grep latest)

echo "Removing dangling images"
docker rmi $(docker images -f "dangling=true" -q)

echo "Microservice enviroment succesfully cleaned"

echo "Clean Project Docker Environment Stop"
