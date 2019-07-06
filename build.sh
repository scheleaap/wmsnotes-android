#!/bin/bash
# The git stuff was based on this: https://gist.github.com/willprice/e07efd73fb7f13f917ea
set -ex

setup_git() {
  git config --global user.name "Travis CI"
  git fetch --unshallow
}

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
    find
    # The code below currently does not work on Travis, as it checks the repository out with a detached head.
    #local changelog_path="fastlane/metadata/android/*/changelogs"
    #git add ${changelog_path}
    #git diff --quiet ${changelog_path} && git diff --staged --quiet ${changelog_path} \
    #  || git commit -m "chore: Added default changelogs for version code ${version_code}." ${changelog_path} \
    #    && git push https://${GITHUB_TOKEN}@github.com/scheleaap/wmsnotes-android.git \
    #    && echo "Exiting build to let the next build take over." \
    #    && return 1
}

function get_version_code_from_gradle() {
  cat app/build.gradle | sed -rn 's/.*versionCode ([0-9]+)/\1/p'
}

function get_version_number_from_gradle() {
  cat app/build.gradle | sed -rn 's/.*versionName "([0-9.]+)"/\1/p'
}

function tag_git_with_version_number() {
    local version_number="$1"

    if [[ "$version_number" != "" ]]; then
      echo "Tagging version $version_number"
      setup_git
      git tag -a "$version_number" -m"Version $version_number"
      git push https://${GITHUB_TOKEN}@github.com/scheleaap/wmsnotes-android.git "$version_number"
    else
      >&2 echo "Version number not tagged!"
    fi
}

echo "Building Travis branch ${TRAVIS_BRANCH}"
version_code=$(get_version_code_from_gradle)
version_number=$(get_version_number_from_gradle)
create_changelogs_if_not_present ${version_code}
./gradlew clean bundleRelease
if [[ "${TRAVIS_BRANCH}" == "master" ]]; then
    bundle exec fastlane deploy
    tag_git_with_version_number ${version_number}
fi
