from ontobio.rdfgen.gocamgen.utils import ShexHelper
import json

shelper = ShexHelper()
shelper.load_shapes()

with open("shex_dump.json", "w") as sf:
    json.dump(shelper.shapes, sf)
