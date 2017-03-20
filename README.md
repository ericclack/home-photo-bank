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

Create a profiles.clj, here's an example:

```
{:profiles/dev  {:env {
                       :media-path "media" }}
 :profiles/test {:env {
                       :media-path "media-test" }}}
```

Create your `media/_import` directory, this is where you'll put photos for import into your Photo Bank.

Put some photos into the import directory.

Run the following from the REPL:

    (clojure-photo-bank.photo-store/import-images)

Now browse to: http://127.0.0.1:3000/

## To Do

Choose between [clj-exif.core :as exif]
            [exif-processor.core :as exifp]

When importing:
- No EXIF
- Duplicate file

Resize image to make thumbnail

Exif metadata: http://stackoverflow.com/questions/33050150/using-exiftool-java-library-from-clojure

## License

Copyright © 2017 FIXME
