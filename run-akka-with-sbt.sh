#!/bin/bash

DIR="akka-project/target/universal/stage/bin"
EXEC="${DIR}/githubChallengeAkka"

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
    sbt clean akkaProject/stage
    if [[ $? -ne 0 ]]
    then
        echo "Fail to build project"
        exit 1
    fi
fi

# then execute the script
echo "Running the application ..."
$EXEC -DGH_TOKEN=$GH_TOKEN
