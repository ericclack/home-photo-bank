#!/usr/bin/env python3

import json
import fileinput
from pprint import pprint

MAX_PHOTOS = 5
counter = 0
keyword_to_photos = {}

for line in fileinput.input():
    counter += 1
    photo = json.loads(line)
    for k in photo['keywords']:
        if k not in keyword_to_photos:
            keyword_to_photos[k] = []
        keyword_to_photos[k].append(photo)
        
pprint(keyword_to_photos)
