#!/bin/bash

function create_changelog_if_not_present() {
    if [[ "$#" -ne 2 ]]; then echo "Usage: $0 <version code> <language code>"; exit 1; fi
    local version_code="$1"
    local language_code="$2"

    local default_path="fastlane/metadata/android/${language_code}/changelogs/default.txt"
    local expected_path="fastlane/metadata/android/${language_code}/changelogs/${version_code}.txt"
    if [[ ! -f ${expected_path} && -f ${default_path} ]]; then
        echo "Creating temporary changelog file ${expected_path}"
        cp ${default_path} ${expected_path}
    fi
}

function create_changelogs_if_not_present() {
    if [[ "$#" -ne 1 ]]; then echo "Usage: $0 <version code>"; exit 1; fi
    local version_code="$1"

    create_changelog_if_not_present ${version_code} "en-US"
    create_changelog_if_not_present ${version_code} "nl-NL"
    local changelog_path="fastlane/metadata/android/*/changelogs"
}

function get_version_code_from_gradle() {
  cat app/build.gradle | sed -rn 's/.*versionCode ([0-9]+)/\1/p'
}

function get_version_number_from_gradle() {
  cat app/build.gradle | sed -rn 's/.*versionName "([0-9.]+)"/\1/p'
}

function tag_git_with_version_number() {
    local version_number="$1"

    if [[ -n "$version_number" ]]; then
      echo "Tagging version $version_number"
      git tag -a "$version_number" -m"Version $version_number [ci skip]"
      git push "$version_number"
    else
      >&2 echo "Version number not tagged!"
    fi
}

set -ex

basedir=$(dirname $0)
source $basedir/build-helpers.sh

version_code=$(get_version_code_from_gradle)
version_number=$(get_version_number_from_gradle)

# Create changelogs
create_changelogs_if_not_present ${version_code}

# Decrypt and unpack secrets
gpg --quiet --batch --yes --decrypt --passphrase="$SECRETS_FILE_PASSPHRASE" \
  --output secrets.tar secrets.tar.gpg
tar xvf secrets.tar

# Create release
./gradlew clean bundleRelease

# Upload to Google Play
# TODO
# bundle exec fastlane deploy version_code:${version_code}

# Create a Git tag
# TODO
# tag_git_with_version_number ${version_number}
