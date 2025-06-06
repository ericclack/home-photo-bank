# Set up on Raspberry Pi 5

Install Raspberry Pi OS using Raspberry Pi Imager https://www.raspberrypi.com/software/

You will need a Java JVM, [Leiningen][1] 2.0 or above installed, plus
[MongoDB][2] (a build for the Pi).

```
sudo apt install default-jdk
```

You may also need to install libssl1.1 for MongoDB: 

```
sudo apt install libssl1.1
```

[1]: https://github.com/technomancy/leiningen
[2]: https://github.com/themattman/mongodb-raspberrypi-binaries

## Get the code

```
cd
mkdir code
cd code
git clone https://github.com/ericclack/home-photo-bank.git
```

## MongoDB set up

```
sudo chown pi:pi /data/db/
mkdir /data/db/photobank_db

sudo mkdir /var/local/log/
sudo chown pi:pi /var/local/log
```

## Server start-up

Link up utilities:

```
cd
mkdir bin
cd bin
ln -s /usr/local/bin/lein .
ln -s ~/code/home-photo-bank/resources/tools/pi/start_* .
```

```
crontab -e
...
@reboot bin/start_mongod
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
