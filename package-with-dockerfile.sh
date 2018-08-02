#!/usr/bin/env bash
VERSION="$1"
if [ -z "$VERSION" ]; then
    mvn -Ddockerfile.skip=false clean package
else
    mvn -Ddockerfile.skip=false -Ddockerfile.tag="$VERSION" clean package
fi
