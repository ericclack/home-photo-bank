# clojure-photo-bank


Philosophy:

* Home Photo Bank – a safe place to store family photos
* Photos are always JPEGs – if not, convert them first using your photo software
* Use EXIF: Photos are organised by creation date
* Named photos contain keywords
* Home network is fast
* Never modify photo files, except moving them into their category folders


## Prerequisites

You will need [Leiningen][1] 2.0 or above installed, plus MongoDB.

[1]: https://github.com/technomancy/leiningen

## Set up

Start up MongoDB. (TODO)

Now add its name to profiles.clj, here's an example:

```
{:profiles/dev  {:env {:media-path "media"
                       :database-url "mongodb://localhost/photo-bank" }}
 
 :profiles/test {:env {:media-path "media-test"
                       :database-url "mongodb://localhost/photo-bank-test" }}}
```

Create your `media/_import`, `media/_process` and `media/_failed`
directories, these are used when importing photos into your photo
bank.

Put some photos into the _import directory. Name them with some keywords.

Run the following from the REPL:

    (clojure-photo-bank.photo-store/import-images!)

This copies the photos into their category directory (year/month/day
of creation) and generates metadata from the filename and
stores in CouchDB.

Now browse to: http://127.0.0.1:3000/

## Copying files into _process or _import

Set up a new user on your home server and generate keys so that other people can
copy photos into the bank...

(test it)

```
sudo adduser --disabled-password photo-uploader 
su - photo-uploader
ln -s path-to-media-process-directory ~photo-uploader/
ssh-keygen
```

Now add your key to photo-uploader's authorized_keys file:

```
emacs .ssh/authorized_keys
```

Now set permissions on the _process directory

```
path/to/_process $ chgrp photo-uploader .
path/to/_process $ chmod +t .
```

## Running

To start a web server for the application, run:

    lein run

## Photo metadata

Photo Bank uses EXIF data to find the date taken and stores photos by
year/month/date. It also uses any words in the filename for a simple
keyword search.

## To Do

Lots!

## License

GNU General Public License v3.0

Copyright © 2017/18 Eric Clack
