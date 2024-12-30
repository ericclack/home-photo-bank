#!/usr/bin/env python3

import os
import json
import fileinput
from pprint import pprint

MAX_PHOTOS = 5
counter = 0
keyword_to_photos = {}

def open_photos_that_exist(photos):
    # This probably only works on a Mac
    files = " ".join([p['path']
                      for p in photos
                      if os.path.isfile(p['path'])][:MAX_PHOTOS])
    if files:
        os.system("open %s" % files)
    else:
        print("No files exist")

for line in fileinput.input():
    counter += 1
    photo = json.loads(line)
    for k in photo['keywords']:
        if k not in keyword_to_photos:
            keyword_to_photos[k] = []
        keyword_to_photos[k].append(photo)
        
while True:
    k = input("Enter single keyword to search: " )
    try:
        photos = keyword_to_photos[k]
        print("%s results:" % len(photos))
        #pprint(photos)
        
        open_photos_that_exist(photos)
        
    except KeyError:
        print("No match for %s" % k)
