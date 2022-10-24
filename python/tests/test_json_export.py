import pytest
from linkml_runtime.utils.schemaview import SchemaView

REMOTE_PATH = (
    "https://raw.githubusercontent.com/geneontology/go-shapes/6b14a45b9f6e6b9dcb731d4201443b29ec5868e7/schema/shex_linkml.yaml"
)


def test_valid_schema():
    sv = SchemaView(
        "https://raw.githubusercontent.com/geneontology/go-shapes/6b14a45b9f6e6b9dcb731d4201443b29ec5868e7/schema/shex_linkml.yaml"
    )
    assert sv.all_classes()
