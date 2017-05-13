#!/usr/bin/env bash

gradle clean jar sourcesJar javadocJar pom

if [ -d mavenBuild ]; then
  rm -r ./mavenBuild
fi

mkdir mavenBuild

mv ./build/libs/*.* ./mavenBuild/

FILES=./mavenBuild/*
for f in ${FILES}
do
  echo "Processing $f file..."
  gpg2 -ab ${f}
done