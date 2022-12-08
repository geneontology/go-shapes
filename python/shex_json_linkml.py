# Auto generated from shex_json_linkml.yaml by pythongen.py version: 0.9.0
# Generation date: 2022-11-30T19:55:17
# Schema: GODomainRangeConstraintsModel
#
# id: go-shex-domain-range-constraints
# description: a schema for the domain and range exchange format for the GO shape expressions
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
from linkml_runtime.linkml_model.types import Boolean, String, Uriorcurie
from linkml_runtime.utils.metamodelcore import Bool, URIorCURIE

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
class Association(YAMLRoot):
    """
    GO domain/range constraint shape is defined as the domain, relationship, and range of a GO shape  expression rule
    """
    _inherited_slots: ClassVar[List[str]] = []

    class_class_uri: ClassVar[URIRef] = GO.Association
    class_class_curie: ClassVar[str] = "GO:Association"
    class_name: ClassVar[str] = "Association"
    class_model_uri: ClassVar[URIRef] = GO.Association

    subject: Union[str, URIorCURIE] = None
    object: Union[Union[str, URIorCURIE], List[Union[str, URIorCURIE]]] = None
    is_multivalued: Union[bool, Bool] = None
    is_required: Union[bool, Bool] = None
    context: str = None
    predicate: Optional[str] = None
    exclude_from_extensions: Optional[Union[bool, Bool]] = None

    def __post_init__(self, *_: List[str], **kwargs: Dict[str, Any]):
        if self._is_empty(self.subject):
            self.MissingRequiredField("subject")
        if not isinstance(self.subject, URIorCURIE):
            self.subject = URIorCURIE(self.subject)

        if self._is_empty(self.object):
            self.MissingRequiredField("object")
        if not isinstance(self.object, list):
            self.object = [self.object] if self.object is not None else []
        self.object = [v if isinstance(v, URIorCURIE) else URIorCURIE(v) for v in self.object]

        if self._is_empty(self.is_multivalued):
            self.MissingRequiredField("is_multivalued")
        if not isinstance(self.is_multivalued, Bool):
            self.is_multivalued = Bool(self.is_multivalued)

        if self._is_empty(self.is_required):
            self.MissingRequiredField("is_required")
        if not isinstance(self.is_required, Bool):
            self.is_required = Bool(self.is_required)

        if self._is_empty(self.context):
            self.MissingRequiredField("context")
        if not isinstance(self.context, str):
            self.context = str(self.context)

        if self.predicate is not None and not isinstance(self.predicate, str):
            self.predicate = str(self.predicate)

        if self.exclude_from_extensions is not None and not isinstance(self.exclude_from_extensions, Bool):
            self.exclude_from_extensions = Bool(self.exclude_from_extensions)

        super().__post_init__(**kwargs)


@dataclass
class AssociationCollection(YAMLRoot):
    """
    A collection of GO domain/range constraint shapes. This is primarily used in this schema to allow several test
    data objects to be submitted in a single file.
    """
    _inherited_slots: ClassVar[List[str]] = []

    class_class_uri: ClassVar[URIRef] = GO.AssociationCollection
    class_class_curie: ClassVar[str] = "GO:AssociationCollection"
    class_name: ClassVar[str] = "AssociationCollection"
    class_model_uri: ClassVar[URIRef] = GO.AssociationCollection

    goshapes: Optional[Union[Union[dict, Association], List[Union[dict, Association]]]] = empty_list()

    def __post_init__(self, *_: List[str], **kwargs: Dict[str, Any]):
        if not isinstance(self.goshapes, list):
            self.goshapes = [self.goshapes] if self.goshapes is not None else []
        self.goshapes = [v if isinstance(v, Association) else Association(**as_dict(v)) for v in self.goshapes]

        super().__post_init__(**kwargs)


# Enumerations


# Slots
class slots:
    pass

slots.object = Slot(uri=GO.object, name="object", curie=GO.curie('object'),
                   model_uri=GO.object, domain=None, range=Union[Union[str, URIorCURIE], List[Union[str, URIorCURIE]]])

slots.id = Slot(uri=GO.id, name="id", curie=GO.curie('id'),
                   model_uri=GO.id, domain=None, range=Optional[str])

slots.predicate = Slot(uri=GO.predicate, name="predicate", curie=GO.curie('predicate'),
                   model_uri=GO.predicate, domain=None, range=Optional[str])

slots.subject = Slot(uri=GO.subject, name="subject", curie=GO.curie('subject'),
                   model_uri=GO.subject, domain=None, range=Union[str, URIorCURIE])

slots.is_multivalued = Slot(uri=GO.is_multivalued, name="is_multivalued", curie=GO.curie('is_multivalued'),
                   model_uri=GO.is_multivalued, domain=None, range=Union[bool, Bool])

slots.goshapes = Slot(uri=GO.goshapes, name="goshapes", curie=GO.curie('goshapes'),
                   model_uri=GO.goshapes, domain=None, range=Optional[Union[Union[dict, Association], List[Union[dict, Association]]]])

slots.is_required = Slot(uri=GO.is_required, name="is_required", curie=GO.curie('is_required'),
                   model_uri=GO.is_required, domain=None, range=Union[bool, Bool])

slots.context = Slot(uri=GO.context, name="context", curie=GO.curie('context'),
                   model_uri=GO.context, domain=None, range=str)

slots.exclude_from_extensions = Slot(uri=GO.exclude_from_extensions, name="exclude_from_extensions", curie=GO.curie('exclude_from_extensions'),
                   model_uri=GO.exclude_from_extensions, domain=None, range=Optional[Union[bool, Bool]])
