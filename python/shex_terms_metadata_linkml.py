# Auto generated from shex_terms_metadata_json_linkml.yaml by pythongen.py version: 0.9.0
# Generation date: 2022-11-30T19:57:14
# Schema: ShexTermMetadataModel
#
# id: shex-term-metadata
# description: a schema for metadata of the terms used in the shex schema
# license: https://creativecommons.org/publicdomain/zero/1.0/

import dataclasses
import sys
import re
from jsonasobj2 import JsonObj, as_dict
from typing import Optional, List, Union, Dict, ClassVar, Any
from dataclasses import dataclass
from linkml_runtime.linkml_model.meta import EnumDefinition, PermissibleValue, PvFormulaOptions

from linkml_runtime.utils.slot import Slot
from linkml_runtime.utils.metamodelcore import empty_list, empty_dict, bnode
from linkml_runtime.utils.yamlutils import YAMLRoot, extended_str, extended_float, extended_int
from linkml_runtime.utils.dataclass_extensions_376 import dataclasses_init_fn_with_kwargs
from linkml_runtime.utils.formatutils import camelcase, underscore, sfx
from linkml_runtime.utils.enumerations import EnumDefinitionImpl
from rdflib import Namespace, URIRef
from linkml_runtime.utils.curienamespace import CurieNamespace
from linkml_runtime.linkml_model.types import String, Uriorcurie
from linkml_runtime.utils.metamodelcore import URIorCURIE

metamodel_version = "1.7.0"
version = "0.1.0"

# Overwrite dataclasses _init_fn to add **kwargs in __init__
dataclasses._init_fn = dataclasses_init_fn_with_kwargs

# Namespaces
GO = CurieNamespace('GO', 'http://purl.obolibrary.org/obo/GO_')
BIOLINK = CurieNamespace('biolink', 'https://w3id.org/biolink/vocab/')
LINKML = CurieNamespace('linkml', 'https://w3id.org/linkml/')
SCHEMA = CurieNamespace('schema', 'http://example.org/UNKNOWN/schema/')
DEFAULT_ = GO


# Types

# Class references



@dataclass
class Term(YAMLRoot):
    """
    A term metadata
    """
    _inherited_slots: ClassVar[List[str]] = []

    class_class_uri: ClassVar[URIRef] = GO.Term
    class_class_curie: ClassVar[str] = "GO:Term"
    class_name: ClassVar[str] = "Term"
    class_model_uri: ClassVar[URIRef] = GO.Term

    id: Union[str, URIorCURIE] = None
    label: str = None
    definition: Optional[str] = None
    comment: Optional[str] = None
    synonyms: Optional[str] = None

    def __post_init__(self, *_: List[str], **kwargs: Dict[str, Any]):
        if self._is_empty(self.id):
            self.MissingRequiredField("id")
        if not isinstance(self.id, URIorCURIE):
            self.id = URIorCURIE(self.id)

        if self._is_empty(self.label):
            self.MissingRequiredField("label")
        if not isinstance(self.label, str):
            self.label = str(self.label)

        if self.definition is not None and not isinstance(self.definition, str):
            self.definition = str(self.definition)

        if self.comment is not None and not isinstance(self.comment, str):
            self.comment = str(self.comment)

        if self.synonyms is not None and not isinstance(self.synonyms, str):
            self.synonyms = str(self.synonyms)

        super().__post_init__(**kwargs)


@dataclass
class TermCollection(YAMLRoot):
    """
    A collection of terms
    """
    _inherited_slots: ClassVar[List[str]] = []

    class_class_uri: ClassVar[URIRef] = GO.TermCollection
    class_class_curie: ClassVar[str] = "GO:TermCollection"
    class_name: ClassVar[str] = "TermCollection"
    class_model_uri: ClassVar[URIRef] = GO.TermCollection

    terms: Optional[Union[Union[dict, "TermCollection"], List[Union[dict, "TermCollection"]]]] = empty_list()

    def __post_init__(self, *_: List[str], **kwargs: Dict[str, Any]):
        if not isinstance(self.terms, list):
            self.terms = [self.terms] if self.terms is not None else []
        self.terms = [v if isinstance(v, TermCollection) else TermCollection(**as_dict(v)) for v in self.terms]

        super().__post_init__(**kwargs)


# Enumerations


# Slots
class slots:
    pass

slots.id = Slot(uri=GO.id, name="id", curie=GO.curie('id'),
                   model_uri=GO.id, domain=None, range=Union[str, URIorCURIE])

slots.label = Slot(uri=GO.label, name="label", curie=GO.curie('label'),
                   model_uri=GO.label, domain=None, range=str)

slots.definition = Slot(uri=GO.definition, name="definition", curie=GO.curie('definition'),
                   model_uri=GO.definition, domain=None, range=Optional[str])

slots.comment = Slot(uri=GO.comment, name="comment", curie=GO.curie('comment'),
                   model_uri=GO.comment, domain=None, range=Optional[str])

slots.synonyms = Slot(uri=GO.synonyms, name="synonyms", curie=GO.curie('synonyms'),
                   model_uri=GO.synonyms, domain=None, range=Optional[str])

slots.terms = Slot(uri=GO.terms, name="terms", curie=GO.curie('terms'),
                   model_uri=GO.terms, domain=None, range=Optional[Union[Union[dict, TermCollection], List[Union[dict, TermCollection]]]])
