#!/bin/bash

project_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
publisher="$project_root/input-cache/publisher.jar"
txoption=""

cd "$project_root"

# echo "Checking internet connection..."
# if ! curl -sSf https://tx.fhir.org >/dev/null; then
#   echo "Offline"
#   txoption="-tx n/a"
# else
#   echo "Online"
# fi

# Disable terminology by default
txoption="-tx n/a"


if [ ! -f "$publisher" ]; then
  echo "IG Publisher not found at input-cache/publisher.jar." >&2
  exit 1
fi

export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Dfile.encoding=UTF-8"

# Publisher only auto-runs SUSHI when input/fsh exists
# Run `sushi build .` manually after editing sushi-config.yaml
# if ! command -v sushi >/dev/null 2>&1; then
#   echo "SUSHI not found; install with: npm install -g fsh-sushi" >&2
#   exit 1
# fi
# sushi build . || exit 1

# IG Publisher generates files locally; this command does not upload or publish them.
java -jar "$publisher" -ig . $txoption "$@"