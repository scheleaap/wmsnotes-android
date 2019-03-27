Documentation

The Travis CLI was installed as follows:
```bash
sudo apt-get install ruby ruby-dev
sudo gem install travis -v 1.8.9 --no-rdoc --no-ri
```

The keystore was created as follows:
```bash
keytool -genkey -v -keystore scheleaap.jks -alias wmsnotes -keyalg RSA -keysize 2048 -validity 10000
travis login --org
travis encrypt-file scheleaap.jks --add
```
