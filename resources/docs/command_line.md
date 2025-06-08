# Command Line Tools

The beginnings of a command line interface to your photo bank.

One use case: run this on your backup or mirror of your live photo bank.

Only needs Python 3.

## Example

```
    > cd folder-containing-your-mirror
    > ls
    backup	media	resources
    
    > ./resources/tools/photo-metadata-search.py backup/photo-bank2024-12-31_11:34.json

    Enter keyword to search or year/month: sky
    160 results:
    (First 25 are opened)

    Enter keyword to search or year/month: sky view
    27 results:
    (First 25 are opened)
```

## Set up

Mirror your media folder and include a backup database JSON file. See script
for example of how to do this:

https://github.com/ericclack/home-photo-bank/blob/master/resources/tools/mirror-photo-bank

