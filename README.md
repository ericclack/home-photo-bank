# clojure-photo-bank

generated using Luminus version "2.9.11.34"

FIXME

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein run

## Set up

Start up couchdb. Then from the Futon admin system -
http://127.0.0.1:5984/_utils/ - create a database for your photo-bank,
and add its name to profiles.clj, here's an example:

```
{:profiles/dev  {:env {:media-path "media"
                       :database-url "photo-bank" }}
 
 :profiles/test {:env {:media-path "media-test"
                       :database-url "photo-bank-test" }}}
```

Now create the database views:

    lein setup-db

Create your `media/_import` directory, this is where you'll put photos for import into your Photo Bank.

Put some photos into the import directory. Name them with some keywords.

Run the following from the REPL:

    (clojure-photo-bank.photo-store/import-images)

Now browse to: http://127.0.0.1:3000/

## Photo metadata

Photo Bank uses EXIF data to find the date taken and stores photos by
year/month/date. It also uses any words in the filename for a simple
keyword search.

## To Do

Lots!

## License

Copyright © 2017 Eric Clack
