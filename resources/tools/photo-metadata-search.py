#!/usr/bin/env python3

"""Somewhat interactive photo search by year/date and keywords.

Usage:

    > cd folder-containing-media-etc
    > ls
    backup	media	resources
    > ./resources/tools/photo-metadata-search.py backup/photo-bank2024-12-31_11:34.json
    Enter keyword to search or year/month: sky
    160 results:
    (First 25 are opened)

    Enter keyword to search or year/month: sky view
    27 results:
    (First 25 are opened)

"""

import os
import json
import fileinput
import re
from collections import defaultdict
import shlex
from pprint import pprint

MAX_PHOTOS = 25
keyword_to_photos = defaultdict(list)
yearmonth_to_photos = defaultdict(list)

def open_photos_that_exist(photos):
    # This probably only works on a Mac
    files = " ".join([shlex.quote(p['path'])
                      for p in photos
                      if os.path.isfile(p['path'])]
                     [:MAX_PHOTOS])
    if files:
        print("Opening %s" % files)
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

def popular_keywords(number=None):
    keys_and_freqs = [(k, len(keyword_to_photos[k])) for k in keyword_to_photos]
    s = sorted(keys_and_freqs, key=lambda i: i[1], reverse=True)
    k = [k for k, f in s]
    
    if number:
        return k[:number]
    else:
        return k

def print_3_columns(items):
    for a, b, c in zip(items[::3], items[1::3], items[2::3]):
        print('{:<30}{:<30}{:<}'.format(a,b,c))

        
load_json()

print("Most popoular keywords: ")
print_3_columns(popular_keywords(50))
print()

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
