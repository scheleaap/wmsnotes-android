#!/bin/bash
# The git stuff was based on this: https://gist.github.com/willprice/e07efd73fb7f13f917ea
set -ev

setup_git() {
  git config --global user.name "Travis CI"
}

function get_version_number_from_gradle() {
  # cat app/build.gradle | sed -rn 's/.*versionName "([0-9.]+)".*/\1/p'
  date +%s | sha256sum | base64 | head -c 32 ; echo
}

echo "Building Travis branch $TRAVIS_BRANCH"
#./gradlew clean bundleRelease
if [[ "$TRAVIS_BRANCH" == "feature-buildscript" ]]; then
    #bundle exec fastlane deploy

    version_number=$(get_version_number_from_gradle)
    if [[ "$version_number" != "" ]]; then
      echo "Tagging version $version_number"
      setup_git
      git tag -a "test-$version_number" -m"Version $version_number"
      git push https://${GITHUB_TOKEN}@github.com/scheleaap/wmsnotes-desktop-and-server.git "$version_number"
    else
      >&2 echo "Version number not tagged!"
    fi
fi
