#! /bin/bash

docker_repo=$1

docker login -u "$DOCKER_USERNAME" -p "$DOCKER_PASSWORD"
docker tag dda-smeagol-crate domaindrivenarchitecture/$docker_repo:latest
docker push $DOCKER_USERNAME/$docker_repo
