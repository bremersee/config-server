#!/usr/bin/env bash
VERSION="$1"
if [ -z "$VERSION" ]; then
    mvn -Ddockerfile.skip=false dockerfile:push
else
    mvn -Ddockerfile.skip=false -Ddockerfile.tag="$VERSION" dockerfile:push
fi
