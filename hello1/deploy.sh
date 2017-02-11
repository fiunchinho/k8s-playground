#!/usr/bin/env bash

set -e

if [ -z "$1" ]
  then
    echo "No project name supplied"
    exit 1
fi

project=$1
version=${2:-latest}
registry=${REGISTRY:="registry.spain.schibsted.io"}
image=${registry}/${project}
versioned_image="${image}:${version}"

docker build -t ${versioned_image} .
docker push ${versioned_image}

sed -e "s@$image@$versioned_image@g" deployment.yml > deployment-rendered.yml
kubectl apply -f service.yml
kubectl apply -f deployment-rendered.yml --record
rm deployment-rendered.yml