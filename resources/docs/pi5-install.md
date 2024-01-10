# Set up on Raspberry Pi 5

You will need a Java JVM, [Leiningen][1] 2.0 or above installed, plus
[MongoDB][2].

```
sudo apt install default-jdk
```

[1]: https://github.com/technomancy/leiningen
[2]: https://github.com/themattman/mongodb-raspberrypi-binaries

## MongoDB set up

$ sudo chown pi:pi /data/db/
$ mkdir /data/db/photobank_db

$ sudo mkdir /var/local/log/
$ sudo chown pi:pi /var/local/log

## Bugs seen

### Open

```
lein test
- media-test/_test/flower_exiv2.jpg does not exist
```

### Fixed

monger version 3.6.0 required? Yes.