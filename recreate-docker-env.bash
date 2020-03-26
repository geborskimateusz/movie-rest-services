#!/usr/bin/env bash

echo "Preparing to restart docker enviroment for movie-rest-services."

echo "Stopping already running microserices container"
docker kill $(docker ps -a -q)

echo "Removing all microservices containers"
docker rm $(docker ps -a -q)

echo "Removing latest microservices images"
docker rmi $(docker images | grep latest)

echo "Microservice enviroment succesfully cleaned"

echo "Creating new enviroment"

echo "Cleaning and installing Maven project"
mvn clean install

echo "Building docker image"
docker-compose build



