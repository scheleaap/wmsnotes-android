# Documentation

## Signing and Deploying

The Travis CLI was installed as follows (on Ubuntu 16.04):
```bash
sudo apt-get install ruby ruby-dev
sudo apt-get purge ruby-bundler
sudo gem install bundler -NV
sudo gem install travis -v 1.8.9 --no-rdoc --no-ri
sudo gem install fastlane -v 2.119.0 -NV
```

The signing key was created as follows:
```bash
keytool -genkey -v -keystore scheleaap.jks -alias wmsnotes -keyalg RSA -keysize 2048 -validity 10000
```

The secrets file was created as follows:
```bash
tar cvf secrets.tar scheleaap.jks keystore.properties google-services.json
travis login --com
travis encrypt-file --com --add secrets.tar
```

Fastlane Supply was set up as follows:
```bash
supply init -j google-services.json -p info.maaskant.wmsnotes
```

### References

* https://github.com/tdillon/android
* https://github.com/harmittaa/travis-example-android
* https://medium.com/@pratikg17/fastlane-and-travis-ci-integration-for-android-ed77b2a498e2
* https://docs.fastlane.tools/getting-started/android/setup/


## Versioning

This might be useful in the future:

* https://github.com/gladed/gradle-android-git-version
