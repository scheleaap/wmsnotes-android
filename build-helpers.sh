#!/bin/bash
# functions to commits on a branch in a Travis CI build
# be sure to avoid creating a Travis CI fork bomb
# see https://github.com/travis-ci/travis-ci/issues/1701
# based on https://gist.github.com/ddgenome/f3a60fe4c2af0cbe758556d982fbeea9

function travis_checkout_branch() {
    local head_ref branch_ref
    head_ref=$(git rev-parse HEAD)
    if [[ $? -ne 0 || ! $head_ref ]]; then
        err "failed to get HEAD reference"
        return 1
    fi
    branch_ref=$(git rev-parse "$TRAVIS_BRANCH")
    if [[ $? -ne 0 || ! $branch_ref ]]; then
        err "failed to get $TRAVIS_BRANCH reference"
        return 1
    fi
    if [[ $head_ref != $branch_ref ]]; then
        msg "HEAD ref ($head_ref) does not match $TRAVIS_BRANCH ref ($branch_ref)"
        msg "someone may have pushed new commits before this build cloned the repo"
        return 0
    fi
    if ! git checkout "$TRAVIS_BRANCH"; then
        err "failed to checkout $TRAVIS_BRANCH"
        return 1
    fi
}

function travis_add_and_commit() {
    if [[ $# -ne 2 ]]; then
        err "incorrect number of arguments"
        return 1
    fi
    local add_path=$1
    local commit_message=$2

    git config --global user.name "Travis CI"
    if ! git add --all $add_path; then
        err "failed to add modified files to git index"
        return 1
    fi
    # make Travis CI skip this build
    if ! (git diff --quiet $add_path && git diff --staged --quiet $add_path || git commit -m "$commit_message [ci skip]"); then
        err "failed to commit updates"
        return 1
    fi
}

function travis_push() {
    local remote=origin
    if [[ $GITHUB_TOKEN ]]; then
        remote=https://$GITHUB_TOKEN@github.com/$TRAVIS_REPO_SLUG
    fi
    if ! git push --quiet --follow-tags "$remote" "$TRAVIS_BRANCH" > /dev/null 2>&1; then
        err "failed to push git changes"
        return 1
    fi
}

function msg() {
    echo "travis-commit: $*"
}

function err() {
    msg "$*" 1>&2
}
