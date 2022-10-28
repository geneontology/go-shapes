from __future__ import annotations
from datetime import datetime, date
from enum import Enum
from typing import List, Dict, Optional, Any
from pydantic import BaseModel as BaseModel, Field

metamodel_version = "None"
version = "0.1.0"

class WeakRefShimBaseModel(BaseModel):
   __slots__ = '__weakref__'
    
class ConfiguredBaseModel(WeakRefShimBaseModel,
                validate_assignment = True, 
                validate_all = True, 
                underscore_attrs_are_private = True, 
                extra = 'forbid', 
                arbitrary_types_allowed = True):
    pass                    


class GoShape(ConfiguredBaseModel):
    """
    GO domain/range constraint shape is defined as the domain, relationship, and range of a GO shape  expression rule
    """
    domain_name: str = Field(None, description="""The domain of the GO shape expression rule, this is the subject of the relationship.""")
    relationships: List[Relationship] = Field(default_factory=list, description="""A list of relationships that are defined for a particular domain""")
    


class Relationship(ConfiguredBaseModel):
    """
    A relationship object holds the relationship (Relationship.id) between a GoShape.domain (subject) and the Relatioship.range (object),  the range of the relationship (what values can be provided in the object of a statement), whether or not the  object can be multivalued, if the object is required when used with a particular domain and what context the  relationship should be used for.
    """
    id: Optional[str] = Field(None, description="""The relationship id is the predicate of the relationship between the domain and  range of the GO shape expression rule.""")
    range: List[str] = Field(default_factory=list, description="""The range of the relationship identified by the Relationship.id parameter (This contains the values can be provided in the object of a statement)""")
    is_multivalued: bool = Field(None, description="""for this shape, the relationship in question supports multiple values in the object of the association.""")
    is_required: bool = Field(None)
    context: str = Field(None, description="""used to determine if this shape is used in the the visual pathway editor or the graphical editor.  Those shapes annotated with like this https://github.com/geneontology/go-shapes/pull/285/files will be exlcuded from the visual pathway editor but still included in the file so this file can be used in the graphical editor as well.""")
    


class Collection(ConfiguredBaseModel):
    """
    A collection of GO domain/range constraint shapes.  This is primarily used in this schema to allow several  test data objects to be submitted in a single file. 
    """
    goshapes: Optional[List[GoShape]] = Field(default_factory=list, description="""A collectionm of GO domain/range constraint shapes where a GO domain/range constraint shape  is defined as the domain, relationship, and range of a GO shape expression rule.""")
    



# Update forward refs
# see https://pydantic-docs.helpmanual.io/usage/postponed_annotations/
GoShape.update_forward_refs()
Relationship.update_forward_refs()
Collection.update_forward_refs()

