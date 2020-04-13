# Documentation

## Signing and Deploying

The Travis CLI was installed as follows (on Ubuntu 16.04):
```sh
sudo apt-get install ruby ruby-dev
sudo apt-get purge ruby-bundler
sudo gem install bundler -NV
sudo gem update --system 3.0.6
sudo bundle install
```

The signing key was created as follows:
```sh
keytool -genkey -v -keystore scheleaap.jks -alias wmsnotes -keyalg RSA -keysize 2048 -validity 10000
```

The secrets file was created as follows:
```sh
tar cvf secrets.tar scheleaap.jks keystore.properties google-services.json
travis login --com
travis encrypt-file --com --add secrets.tar
```

Fastlane Supply was set up as follows:
```sh
supply init -j google-services.json -p info.maaskant.wmsnotes
```

The GitHub token was set up as follows:
```sh
bundle exec travis login --com
bundle exec travis encrypt --com GITHUB_TOKEN=<token>
```

### References

* https://github.com/tdillon/android
* https://github.com/harmittaa/travis-example-android
* https://medium.com/@pratikg17/fastlane-and-travis-ci-integration-for-android-ed77b2a498e2
* https://docs.fastlane.tools/getting-started/android/setup/


## Versioning

This might be useful in the future:

* https://github.com/gladed/gradle-android-git-version
