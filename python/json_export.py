from os import path
import json
import requests
from ontobio.rdfgen.assoc_rdfgen import prefix_context
from prefixcommons.curie_util import contract_uri
from pyshexc.parser_impl import generate_shexj
from typing import Optional, List, Union
from ShExJSG.ShExJ import Shape, ShapeAnd, ShapeOr, ShapeNot, TripleConstraint, shapeExpr, \
    shapeExprLabel, tripleExpr, tripleExprLabel, OneOf, EachOf, Annotation
from pyshex import PrefixLibrary
from shex_json_linkml import Association
from pprint import pprint
from pathlib import Path


def get_suffix(uri):
    suffix = contract_uri(uri, cmaps=[prefix_context])
    if len(suffix) > 0:
        return suffix[0]

    return path.basename(uri)


class NoctuaFormShex:
    def __init__(self, shex_text):
        self.exclude_ext_pred = 'http://purl.obolibrary.org/obo/go/shapes/exclude_from_extensions'
        self.json_shapes = [] 
        
        self.shex = generate_shexj.parse(shex_text)
        pref = PrefixLibrary(shex_text)
        self.pref_dict = {
            k: get_suffix(str(v)) for (k, v) in dict(pref).items()
            if str(v).startswith('http://purl.obolibrary.org/obo/')}
        del self.pref_dict['OBO']  # remove this filter and make sure that it works because it needs to be
        # working for every shape.

    def get_shape_name(self, uri, clean=False):
        name = path.basename(uri).upper()
        if '/go/' in uri:
            name = 'GO' + name
        return self.pref_dict.get(name, None if clean else uri)

    def gen_lookup_table(self):
        table = {v: {
            'label': k
        } for (k, v) in self.pref_dict.items()}
        return table

    def _load_expr(self, subject:str, expr: Optional[Union[shapeExprLabel, shapeExpr]], preds=None) -> List:

        if preds is None:
            preds = {}
        if isinstance(expr, str) and isinstance(preds, list):
            preds.append(self.get_shape_name(expr))
        if isinstance(expr, (ShapeOr, ShapeAnd)):
            for expr2 in expr.shapeExprs:
                self._load_expr(subject, expr2, preds)
        elif isinstance(expr, ShapeNot):
            self._load_expr(subject, expr.shapeExpr, preds)
        elif isinstance(expr, Shape) and expr.expression is not None:
            self._load_triple_expr(subject, expr.expression, preds)

        # throw an error here if pred list is empty
        return preds

    def _load_triple_expr(self, subject:str, expr: Union[tripleExpr, tripleExprLabel], preds=None) ->  None:

        if isinstance(expr, (OneOf, EachOf)):
            for expr2 in expr.expressions:
                self._load_triple_expr(subject, expr2, preds)
        elif isinstance(expr, TripleConstraint) and expr.valueExpr is not None:
            predicate = get_suffix(expr.predicate)

            if predicate not in self.pref_dict.values():
                return

            objects = []
            self._load_expr(subject, expr.valueExpr, objects)
            goshape = {} #Association 
            goshape['subject']=subject
            goshape['object']=objects
            goshape['predicate']=predicate  
            
            if isinstance(expr.annotations, list):                    
                goshape['exclude_from_extensions']=self._load_annotation(expr, self.exclude_ext_pred) 
            
            if expr.max is not None:
                goshape["is_multivalued"] = True if expr.max == -1 else False
            
            self.json_shapes.append(goshape)

            return preds


    def _load_annotation(self, expr: Union[tripleExpr, tripleExprLabel], annotation_key):
        for annotation in expr.annotations:
            if isinstance(annotation, Annotation) :
                if annotation.predicate == annotation_key:
                    return True if annotation.object.value=="true" else False

        return None

    def parse_raw(self):
        return json.loads(self.shex._as_json_dumps())

    def parse(self):
        shapes = self.shex.shapes

        for shape in shapes:
            shape_name = self.get_shape_name(shape['id'], True)

            if shape_name is None:
                continue

            print('Parsing Shape: ' + shape['id'])
            
            shexps = shape.shapeExprs or []

            for expr in shexps:
                self._load_expr(shape_name, expr)



if __name__ == "__main__":
   

    base_path = Path(__file__).parent
    shex_fp = (base_path / "../shapes/go-cam-shapes.shex").resolve()
    json_shapes_fp = (base_path / "../shapes/json/shex_dump.json").resolve()
    look_table_fp = (base_path / "../shapes/json/look_table.json").resolve()
    shex_full_fp = (base_path / "../shapes/json/shex_full.json").resolve()
    
    with open(shex_fp) as f:
        shex_text = f.read()

    nfShex = NoctuaFormShex(shex_text)
    nfShex.parse()

    with open(json_shapes_fp, "w") as sf:
        json.dump(nfShex.json_shapes, sf, indent=2)

    with open(look_table_fp, "w") as sf:
        json.dump(nfShex.gen_lookup_table(), sf, indent=2)

    with open(shex_full_fp, "w") as sf:
        json.dump(nfShex.parse_raw(), sf, indent=2)
