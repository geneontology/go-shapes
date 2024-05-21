from os import path
import json
from pathlib import Path
import os
from ontobio.rdfgen.assoc_rdfgen import prefix_context
#from prefixcommons.curie_util import contract_uri

from curies import Converter, Record
from pyshexc.parser_impl import generate_shexj
from typing import Optional, List, Union
from ShExJSG.ShExJ import Shape, ShapeAnd, ShapeOr, ShapeNot, TripleConstraint, shapeExpr, \
    shapeExprLabel, tripleExpr, tripleExprLabel, OneOf, EachOf, Annotation
from pyshex import PrefixLibrary
import requests
from shex_json_linkml import Association, AssociationCollection
from linkml_runtime.dumpers import JSONDumper
from linkml_runtime.loaders import JSONLoader


OUT_JSON = os.path.join('../shapes/json/shex_dump.json')
converter = Converter.from_prefix_map(prefix_context)


def get_suffix(uri):
    suffix = converter.compress(uri)
    
    return suffix if suffix else uri


class NoctuaFormShex:
    def __init__(self, shex_text):
        self.exclude_ext_pred = 'http://purl.obolibrary.org/obo/go/shapes/exclude_from_extensions'
        self.json_shapes = []

        self.shex = generate_shexj.parse(shex_text)
        pref = PrefixLibrary(shex_text)
        self.pref_dict = {
            k: get_suffix(str(v)) for (k, v) in dict(pref).items()
            if str(v).startswith('http://purl.obolibrary.org/obo/')}
        # remove this filter and make sure that it works because it needs to be
        del self.pref_dict['OBO']
        # working for every shape.

    def get_shape_name(self, uri, clean=False):
        name = path.basename(uri).upper()
        if '/go/' in uri:
            name = 'GO' + name
        return self.pref_dict.get(name, None if clean else uri)

    def gen_terms_metadata(self):
        goApi = 'http://api.geneontology.org/api/ontology/term/'
        table = list()
        for k, v in self.pref_dict.items():
            resp = requests.get(goApi+v)
            term = resp.json()
            table.append({
                'id': term['goid'],
                'label': term['label'],
                'definition': term.get('definition', ""),
                'comment': term.get('comment', ""),
                'synonyms': term.get('synonyms', "")
            })
        return table
    
    def _load_root_subject_expr(self, expr: Optional[Union[shapeExprLabel, shapeExpr]]) -> str:
         
        if expr is not None and len(expr)>0 and isinstance(expr[0], str):
                return self.get_shape_name(expr[0])
        
        return None
        

    def _load_expr(self, root_subject: str, subject: str, expr: Optional[Union[shapeExprLabel, shapeExpr]], preds=None) -> List:

        if preds is None:
            preds = {}
        if isinstance(expr, str) and isinstance(preds, list):
            preds.append(self.get_shape_name(expr))
        if isinstance(expr, (ShapeOr, ShapeAnd)):
            for expr2 in expr.shapeExprs:
                self._load_expr(root_subject, subject, expr2, preds)
        elif isinstance(expr, ShapeNot):
            self._load_expr(root_subject, subject, expr.shapeExpr, preds)
        elif isinstance(expr, Shape) and expr.expression is not None:
            self._load_triple_expr(root_subject, subject, expr.expression, preds)

        # throw an error here if pred list is empty
        return preds

    def _load_triple_expr(self, root_subject: str, subject: str, expr: Union[tripleExpr, tripleExprLabel], preds=None) -> None:

        if isinstance(expr, (OneOf, EachOf)):
            for expr2 in expr.expressions:
                self._load_triple_expr(root_subject, subject, expr2, preds)
        elif isinstance(expr, TripleConstraint) and expr.valueExpr is not None:
            predicate = get_suffix(expr.predicate)

            if predicate not in self.pref_dict.values():
                return preds

            objects = []
            self._load_expr(root_subject, subject, expr.valueExpr, objects)

            exclude_from_extensions = False
            if isinstance(expr.annotations, list):
                exclude_from_extensions = self._load_annotation(
                    expr, self.exclude_ext_pred)

            is_multivalued = False
            if expr.max is not None and expr.max == -1:
                is_multivalued = True

            goshape = Association(
                root_subject=root_subject,
                subject=subject,
                object=objects,
                predicate=predicate,
                is_multivalued=is_multivalued,
                exclude_from_extensions=exclude_from_extensions,
                is_required=False,
                context=""
            )
                
            self.json_shapes.append(goshape)

            return preds

    def _load_annotation(self, expr: Union[tripleExpr, tripleExprLabel], annotation_key):
        for annotation in expr.annotations:
            if isinstance(annotation, Annotation) and annotation.predicate == annotation_key:
                return True if annotation.object.value == "true" else False

        return False

    def parse_raw(self):
        return json.loads(self.shex._as_json_dumps())

    def parse(self):
        shapes = self.shex.shapes

        for shape in shapes:
            shape_name = self.get_shape_name(shape['id'], True)
            root_subject = self._load_root_subject_expr(shape.shapeExprs)
            
            if shape_name is None:
                continue

            print('Parsing Shape: ' + shape['id'])

            shexps = shape.shapeExprs or []

            for expr in shexps:
                self._load_expr(root_subject, shape_name, expr)


if __name__ == "__main__":

    base_path = Path(__file__).parent
    shex_fp = (base_path / "../shapes/go-cam-shapes.shex").resolve()
    json_shapes_fp = (base_path / "../shapes/json/shex_dump.json").resolve()
    terms_metadata_fp = (base_path / "../shapes/json/terms_metadata.json").resolve()
    terms_shorthand_fp = (base_path / "../shapes/json/terms_shorthand.json").resolve()
    shex_full_fp = (base_path / "../shapes/json/shex_full.json").resolve()

    with open(shex_fp) as f:
        shex_text = f.read()

    nfShex = NoctuaFormShex(shex_text)
    nfShex.parse()

    with open(json_shapes_fp, "w") as sf:
        jd = JSONDumper()
        coll = AssociationCollection(goshapes=nfShex.json_shapes)
        jd.dump(coll, to_file=OUT_JSON)

    """ with open(terms_metadata_fp, "w") as sf:
        json.dump(nfShex.gen_terms_metadata(), sf, indent=2) """
    
    with open(shex_full_fp, "w") as sf:
        json.dump(nfShex.parse_raw(), sf, indent=2)
