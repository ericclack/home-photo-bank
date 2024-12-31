#!/usr/bin/env python3

import fileinput, re

remove_path = r"^media/\d+/\d+/\d+/(.*)\d+.jpe?g"
keywords = {}

for line in fileinput.input():
    image_keywords = [k for k
                      in re.sub(remove_path, r"\1", line ).split("-")
                      if not(k.isspace())]
    for k in image_keywords:
        keywords[k] = keywords.get(k, 0) + 1

for k in keywords:
    print(k, "\t", "x", keywords[k])
    
