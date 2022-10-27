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
    A generic grouping for any identifiable entity
    """
    domain_name: Optional[str] = Field(None)
    relationships: Optional[List[Relationship]] = Field(default_factory=list)
    


class Relationship(ConfiguredBaseModel):
    """
    A relationship between two entities
    """
    id: Optional[str] = Field(None)
    range: Optional[List[str]] = Field(default_factory=list)
    is_multivalued: Optional[bool] = Field(None)
    


class Collection(ConfiguredBaseModel):
    
    goshapes: Optional[List[GoShape]] = Field(default_factory=list)
    



# Update forward refs
# see https://pydantic-docs.helpmanual.io/usage/postponed_annotations/
GoShape.update_forward_refs()
Relationship.update_forward_refs()
Collection.update_forward_refs()

