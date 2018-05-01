# Home Photo Bank

Philosophy:

* Home Photo Bank – a safe place to store photos on your home network
* Photos are always JPEGs – if not, convert them first using your photo software
* Use EXIF: Photos are organised by creation date
* Named photos contain keywords
* Home network is fast
* Never modify photo files, except moving them into their category folders

![Search Screen Shot](home-photo-bank-search.jpg?raw=true)

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed, plus MongoDB.

[1]: https://github.com/technomancy/leiningen

## Set up

Start up MongoDB, or run it as a service.

Now add its name to profiles.clj, here's an example:

```
{:profiles/dev  {:env {:media-path "media"
                       :database-url "mongodb://localhost/photo-bank" }}
 
 :profiles/test {:env {:media-path "media-test"
                       :database-url "mongodb://localhost/photo-bank-test" }}}
```

Create the  `media/_import`, `media/_process` and `media/_failed`
directories, these are used when importing photos into your photo
bank.

## Running

To start a web server for the application, run:

    lein run

## Photo metadata

Photo Bank uses EXIF data to find the date taken and stores photos by
year/month/date. It also uses any words in the filename for a simple
keyword search, you can easily add more keywords.

## Import some photos

Put some photos into the _process directory on the server(*), then click
the Import menu item in the app. You can now add some initial keywords
to each image and start the import process.

This process copies the photos into their category directory
(year/month/day of creation) and generates metadata from the filename
and stores in MongoDB.

The import process runs every 2 minutes, so wait a few minutes before
browsing to the app home page to see the photos. 

## * Automate the copying of files to the app

Create some keys and scripts so that you can just drop photos into
a ForPhotoBank folder on your computer and have them appear in the app
automatically.

1. Set up a new user on your server and generate keys for copy scripts:

(needs testing)

```
server> sudo adduser --disabled-password photo-uploader 
server> su - photo-uploader
server> ln -s path-to-media-process-directory ~photo-uploader/
server> ssh-keygen
```

Now add your own SSH key to photo-uploader's authorized_keys file:

```
your-computer> cat .ssh/id_rsa.pub
server> emacs .ssh/authorized_keys
```

2. Now set permissions on the _process directory:

```
server> cd path/to/_process
server> chgrp photo-uploader .
server> chmod +t .
```

3. Add a copy script on your own computer:

Review and modify the script [resources/tools/copy-photos-to-bank](/resources/tools/copy-photos-to-bank)


## To Do

Lots!

## License

GNU General Public License v3.0

Copyright © 2017/18 Eric Clack
