from linkml_runtime.utils.schemaview import SchemaView
from linkml_runtime.linkml_model.meta import (
    SchemaDefinition
)
from python import json_export
from pprint import pprint
from linkml.generators.jsonschemagen import JsonSchemaGenerator
from linkml.generators.pydanticgen import PydanticGenerator
from json_export import NoctuaFormShex
from pathlib import Path

base_path = Path(__file__).parent

SHEX_JSON_LINKML_PATH = (
    "../schema/shex_json_linkml.yaml"
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

    base_path = Path(__file__).parent
    jsonschema = (base_path / "../target/jsonschema/shex_json_linkml.json").resolve()

    json_format = open(jsonschema, 'w')
    json_format.write(json_gen.serialize())
    pyfile = open('../shex_json_linkml.py', 'w')
    pyfile.write(python_gen.serialize())


def test_json_parser():
    nfs = json_export.NoctuaFormShex()
    nfs.parse()
    pprint(nfs.json_shapes)

    # pprint(nfs.json_shapes)
