#!/usr/bin/env python3

import os
import json
import fileinput
import re
from collections import defaultdict
from pprint import pprint

MAX_PHOTOS = 25
keyword_to_photos = defaultdict(list)
yearmonth_to_photos = defaultdict(list)

def open_photos_that_exist(photos):
    # This probably only works on a Mac
    files = " ".join([p['path']
                      for p in photos
                      if os.path.isfile(p['path'])]
                     [:MAX_PHOTOS])
    if files:
        os.system("open %s" % files)
    else:
        print("No files exist")

def load_json():
    for line in fileinput.input():
        photo = json.loads(line)
        for k in photo['keywords']:
            keyword_to_photos[k].append(photo)

        r = re.compile('/\d+$') #Match day number incl final /
        ym = r.sub('', photo['category'])
        yearmonth_to_photos[ym].append(photo)
        
load_json()
            
while True:
    i = input("Enter keyword to search or year/month: " )
    terms = i.split(" ")
    first = terms[0]
    second = terms[1] if len(terms) > 1 else None
    if len(terms) > 2: print("Sorry, only one or two keywords supported")
    
    try:
        if re.match('\d\d\d\d/\d+', first):
            photos = yearmonth_to_photos[first]
        else: 
            photos = keyword_to_photos[first]

        if second:
            photos = [p for p in photos
                      if second in p['keywords']]
            
        print("%s results:" % len(photos))
        #pprint(photos)
        
        open_photos_that_exist(photos)
        
    except KeyError:
        print("No match for %s" % k)
