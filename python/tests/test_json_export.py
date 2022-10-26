from linkml_runtime.utils.schemaview import SchemaView
from linkml_runtime.linkml_model.meta import (
    SchemaDefinition
)
import json_export
from pprint import pprint
from linkml.generators.shexgen import ShExGenerator
from linkml.generators.jsonschemagen import JsonSchemaGenerator

SHEX_LINKML_PATH = (
    "../../schema/shex_linkml.yaml"
)
SHEX_JSON_LINKML_PATH = (
    "../../schema/shex_json_linkml.yaml"
)


def test_valid_schema():
    sv = SchemaView(SHEX_JSON_LINKML_PATH)
    schemadef = sv.schema
    assert isinstance(schemadef, SchemaDefinition)
    assert sv.all_classes()
    print(sv.all_classes())

    jsonGen = JsonSchemaGenerator(schema=schemadef)
    print(jsonGen.serialize())


def test_json_parser():
    nfs = json_export.NoctuaFormShex()
    nfs.parse()

    # pprint(nfs.json_shapes)
