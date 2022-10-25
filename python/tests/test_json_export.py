from linkml_runtime.utils.schemaview import SchemaView
from linkml_runtime.linkml_model.meta import (
    SchemaDefinition
)
import json_export
from pprint import pprint
from linkml.generators.shexgen import ShExGenerator
from linkml.generators.jsonschemagen import JsonSchemaGenerator

REMOTE_PATH = (
    "../../schema/shex_linkml.yaml"
)


def test_valid_schema():
    sv = SchemaView(REMOTE_PATH)
    schemadef = sv.schema
    assert isinstance(schemadef, SchemaDefinition)
    assert sv.all_classes()

    shexGen = ShExGenerator(schema=schemadef)
    print(shexGen.serialize())
    jsonGen = JsonSchemaGenerator(schema=schemadef)
    print(jsonGen.serialize())


def test_json_parser():
    nfs = json_export.NoctuaFormShex()
    nfs.parse()

    # pprint(nfs.json_shapes)
