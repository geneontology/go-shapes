import pytest

REMOTE_PATH = (
    "https://raw.githubusercontent.com/biolink/biolink-model/v3.0.3/biolink-model.yaml"
)
def test_get_model_version(toolkit):
    version = toolkit.get_model_version()
    assert version == "3.0.3"
