#!/usr/bin/env python3

import fileinput, re
from pprint import pprint

remove_path = r"^media/\d+/\d+/\d+/(.*).jpeg"
keywords = {}

for line in fileinput.input():
    image_keywords = re.sub(remove_path, r"\1", line ).split("-")
    for k in image_keywords:
        keywords[k] = keywords.get(k, 0) + 1

pprint(keywords)
    
