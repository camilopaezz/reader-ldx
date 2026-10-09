#!/usr/bin/env python3
"""Measure first reading ink below a recorded viewport boundary on clean reading PNGs."""
import argparse,json,sys
from PIL import Image
p=argparse.ArgumentParser();p.add_argument('image');p.add_argument('--content-top',type=int,required=True);p.add_argument('--density',type=int,required=True);a=p.parse_args()
im=Image.open(a.image).convert('RGB');w,h=im.size
ink=next((y for y in range(a.content_top,min(a.content_top+900,h)) if sum(max(im.getpixel((x,y)))<100 for x in range(w//10,9*w//10))>10),None)
gap=None if ink is None else ink-a.content_top;minimum=round(24*a.density/160)
result={'contentTopPx':a.content_top,'firstInkPx':ink,'gapPx':gap,'minimumPx':minimum,'pass':gap is not None and gap>=minimum}
print(json.dumps(result));sys.exit(0 if result['pass'] else 1)
