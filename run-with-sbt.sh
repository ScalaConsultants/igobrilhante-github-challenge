#!/bin/bash

DIR="target/universal/stage/bin"
EXEC="${DIR}/github-challenge"

which sbt >/dev/null || (echo "sbt not found." && exit 1)

source .env

if [[ -z "$GH_TOKEN" ]]
then
    echo "No token found"
else
    echo "Token found"
fi

# first build the project

if [[ ! -d "$DIR" ]]
then
    sbt clean stage
    if [[ $? -ne 0 ]]
    then
        echo "Fail to build project"
        exit 1
    fi
fi

# then execute the script
echo "Running the application ..."
$EXEC -DGH_TOKEN=$GH_TOKEN
