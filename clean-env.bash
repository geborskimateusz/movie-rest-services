echo "Stopping all running containers"
docker kill $(docker ps -a -q)

echo "Removing all existing containers"
docker rm $(docker ps -a -q)

echo "Removing all latest images"
docker rmi $(docker images | grep latest)

#echo "Building Maven project"
#mvn clean install
#
#echo "Build new image"
#docker-compose build
