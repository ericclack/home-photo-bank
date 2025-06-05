# Set up on Raspberry Pi 5

You will need a Java JVM, [Leiningen][1] 2.0 or above installed, plus
[MongoDB][2].

```
sudo apt install default-jdk
```

You may also need to install libssl1.1 for MongoDB: 

```
sudo apt install libssl1.1
```

[1]: https://github.com/technomancy/leiningen
[2]: https://github.com/themattman/mongodb-raspberrypi-binaries

## MongoDB set up

```
sudo chown pi:pi /data/db/
mkdir /data/db/photobank_db

sudo mkdir /var/local/log/
sudo chown pi:pi /var/local/log
```

## Server start-up

```
crontab -l
@reboot bin/start_mongodb
@reboot bin/start_photobank
```

## Photo uploader

Create user and enable SSH login for your user: 

```
sudo adduser photo-uploader --disabled-password
sudo usermod -G pi photo-uploader
sudo -u photo-uploader -g photo-uploader mkdir ~photo-uploader/.ssh
sudo -u photo-uploader -g photo-uploader touch ~photo-uploader/.ssh/authorized_keys
```

Add your SSH public key:

```
sudo -u photo-uploader -g photo-uploader mkdir ~photo-uploader/.ssh
sudo -u photo-uploader -g photo-uploader touch ~photo-uploader/.ssh/authorized_keys
sudo -u photo-uploader -g photo-uploader nano ~photo-uploader/.ssh/authorized_keys
# Enter your key and save file
```

Pemissions for uploader to upload files

As pi user: allow group access: 

```
cd ~pi
chmod g+rx .
```

As photo-uploader user: symlink for photo-uploader:

```
ln -s /home/pi/code/home-photo-bank/media/_process 
```

## Bugs seen

### Open

```
lein test
- media-test/_test/flower_exiv2.jpg does not exist
```

### Fixed

monger version 3.6.0 required? Yes.
