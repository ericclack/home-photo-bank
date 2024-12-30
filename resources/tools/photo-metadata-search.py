#!/usr/bin/env python3

import os
import json
import fileinput
import re
from pprint import pprint

MAX_PHOTOS = 25
keyword_to_photos = {}
yearmonth_to_photos = {}

def open_photos_that_exist(photos):
    # This probably only works on a Mac
    files = " ".join([p['path']
                      for p in photos
                      if os.path.isfile(p['path'])][:MAX_PHOTOS])
    if files:
        os.system("open %s" % files)
    else:
        print("No files exist")

def load_json():
    for line in fileinput.input():
        photo = json.loads(line)
        for k in photo['keywords']:
            if k not in keyword_to_photos:
                keyword_to_photos[k] = []
            keyword_to_photos[k].append(photo)

        r = re.compile('/\d+$') #Match day number incl final /
        ym = r.sub('', photo['category'])
        if ym not in yearmonth_to_photos:
            yearmonth_to_photos[ym] = []
        yearmonth_to_photos[ym].append(photo)
        
load_json()
            
while True:
    k = input("Enter single keyword to search or year/month: " )
    try:
        if re.match('\d\d\d\d/\d+', k):
            photos = yearmonth_to_photos[k]
        else: 
            photos = keyword_to_photos[k]

        print("%s results:" % len(photos))
        #pprint(photos)
        
        open_photos_that_exist(photos)
        
    except KeyError:
        print("No match for %s" % k)
