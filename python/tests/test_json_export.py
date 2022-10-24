import pytest
from linkml_runtime.utils.schemaview import SchemaView

REMOTE_PATH = (
    "../../schema/shex_linkml.yaml"
)


def test_valid_schema():
    sv = SchemaView(REMOTE_PATH)
    assert sv.all_classes()
