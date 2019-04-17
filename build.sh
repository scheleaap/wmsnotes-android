#!/bin/bash
set -ev

function get_version_number() {
  cat app/build.gradle | sed -rn 's/.*versionName "([0-9.]+)".*/\1/p'
}

./gradlew clean bundleRelease
if [ "$TRAVIS_BRANCH" == "master" ]; then
    bundle exec fastlane deploy

    version_number=$(get_version_number)
    if [ "$version_number" != "" ]; then
      git tag -a "$version_number" -m"Version $version_number"
    else
      >&2 echo "Version number not tagged!"
    fi
fi
