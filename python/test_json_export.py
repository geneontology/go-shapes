from linkml_runtime.utils.schemaview import SchemaView
from linkml_runtime.linkml_model.meta import (
    SchemaDefinition
)
from python import json_export
from pprint import pprint
from linkml.generators.jsonschemagen import JsonSchemaGenerator
from linkml.generators.pydanticgen import PydanticGenerator

SHEX_LINKML_PATH = (
    "../schema/shex_linkml.yaml"
)
SHEX_JSON_LINKML_PATH = (
    "../schema/shex_json_linkml.yaml"
)

AUTO_LINKML_PATH = (
    "../schema/autogen_schema.yaml"
)


def test_valid_schema():
    sv = SchemaView(SHEX_JSON_LINKML_PATH)
    schemadef = sv.schema
    assert isinstance(schemadef, SchemaDefinition)
    assert sv.all_classes()
    print(sv.all_classes())

    json_gen = JsonSchemaGenerator(schema=schemadef)
    python_gen = PydanticGenerator(schema=schemadef)
    print(json_gen.serialize())

    json_format = open('shex_json_linkml.json', 'w')
    json_format.write(json_gen.serialize())
    pyfile = open('shex_json_linkml.py', 'w')
    pyfile.write(python_gen.serialize())


def test_json_parser():
    nfs = json_export.NoctuaFormShex()
    nfs.parse()
    pprint(nfs.json_shapes)

    # pprint(nfs.json_shapes)