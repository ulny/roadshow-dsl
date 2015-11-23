#!/usr/bin/env sh

# fail at the first error
set -e

# Get a repo root
ROOTDIR=$(git rev-parse --show-toplevel)

if [ ! -e $ROOTDIR/.tmp/jobdsl ]
then
  echo "No jobdsl jar file found. Clone job-dsl-plugin and rebuild it"
  mkdir .tmp
  git clone https://github.com/jenkinsci/job-dsl-plugin.git $ROOTDIR/.tmp/jobdsl
fi

DSL_JAR=$(find $ROOTDIR/.tmp -name '*standalone.jar'|tail -1)

if [ ! -f "${DSL_JAR}" ]; then
   cd $ROOTDIR/.tmp/jobdsl
  ./gradlew :job-dsl-core:oneJar
   cd -
fi

DSL_JAR=$(find $ROOTDIR/.tmp -name '*standalone.jar'|tail -1)

if [ -e "${DSL_JAR}" ]; then
    java -cp "${DSL_JAR}" -DGITHUB_USER=${GITHUB_USER} OneJar *.groovy > xmlbuild.log
else
    echo No JobDSL.jar built.
    exit 1
fi
