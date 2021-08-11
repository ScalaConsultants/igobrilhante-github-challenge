# Github Challenge

## Description

## Solution

## Executing the project

### Running with sbt

To run the project using sbt, we can simply execute the helper script `run-with-sbt.sh` as follows.

```bash
# provide the access token to GitHub API
GH_TOKEN="...."

# run the script
./run-with-sbt.sh
```

This script will run the project and, if necessary, it will build the project before starting the application.

## How to

```bash
sbt stage
```

```bash
target/universal/stage/bin/github-challenge -DGH_TOKEN=$GH_TOKEN
```