#!/usr/bin/env bash

echo "Preparing to restart docker enviroment for movie-rest-services."

echo "Executing docker environment purge"
./clear-proj-docker-env.bash

echo "Moving into project root directory"
cd ../
echo "Current directory is: $(pwd)"

echo "Creating new enviroment"

echo "Cleaning and installing Maven project"
mvn clean install

echo "Building docker image"
docker-compose build



