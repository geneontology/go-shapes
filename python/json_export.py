from os import path
import json
import requests
from ontobio.rdfgen.assoc_rdfgen import prefix_context
from prefixcommons.curie_util import contract_uri
from pyshexc.parser_impl import generate_shexj
from typing import Optional, List, Union
from ShExJSG.ShExJ import Shape, ShapeAnd, ShapeOr, ShapeNot, TripleConstraint, shapeExpr, \
    shapeExprLabel, tripleExpr, tripleExprLabel, OneOf, EachOf
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
    def __init__(self):
        self.json_shapes = {}
        shex_url = "https://raw.githubusercontent.com/geneontology/go-shapes/master/shapes/go-cam-shapes.shex"
        shex_response = requests.get(shex_url)
        self.shex = generate_shexj.parse(shex_response.text)
        pref = PrefixLibrary(shex_response.text)
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

    def _load_expr(self, expr: Optional[Union[shapeExprLabel, shapeExpr]], preds=None) -> List:

        if preds is None:
            preds = {}
        if isinstance(expr, str) and isinstance(preds, list):
            # ('Adding: ' + expr + ' to ' + str(preds))
            preds.append(self.get_shape_name(expr))
        if isinstance(expr, (ShapeOr, ShapeAnd)):
            for expr2 in expr.shapeExprs:
                self._load_expr(expr2, preds)
        elif isinstance(expr, ShapeNot):
            self._load_expr(expr.shapeExpr, preds)
        elif isinstance(expr, Shape) and expr.expression is not None:
            self._load_triple_expr(expr.expression, preds)

        # throw an error here if pred list is empty
        return preds

    def _load_triple_expr(self, expr: Union[tripleExpr, tripleExprLabel], preds=None) ->  None:

        if isinstance(expr, (OneOf, EachOf)):
            for expr2 in expr.expressions:
                self._load_triple_expr(expr2, preds)
        elif isinstance(expr, TripleConstraint) and expr.valueExpr is not None:
            pred = get_suffix(expr.predicate)

            if pred not in self.pref_dict.values():
                return

            preds[pred] = {}
            preds[pred]['range'] = []

            if expr.max is not None:
                preds[pred]['cardinality'] = expr.max

            self._load_expr(expr.valueExpr, preds[pred]['range'])

    def parse(self):
        goshapes = []

        shapes = self.shex.shapes

        for shape in shapes:
            print(shape)
            print("")
            shape_name = self.get_shape_name(shape['id'], True)

            if shape_name is None:
                continue

            goshape = GoShape()
            goshape.domain_name = shape_name
            goshape.relationships = []
            # print('Parsing Shape: ' + shape['id'])
            self.json_shapes[shape_name] = {}

            shexps = shape.shapeExprs or []

            for expr in shexps:
                self.json_shapes[shape_name] = self._load_expr(expr)
                goshape.relationships.append(self._load_expr(expr))
            goshapes.append(goshape)
            # print(goshapes)


nfShex = NoctuaFormShex()
nfShex.parse()

base_path = Path(__file__).parent
json_shapes_file_path = (base_path / "../shapes/json/shex_dump.json").resolve()
look_table_file_path = (base_path / "../shapes/json/look_table.json").resolve()


with open(json_shapes_file_path, "w") as sf:
    json.dump(nfShex.json_shapes, sf, indent=2)

with open(look_table_file_path, "w") as sf:
    json.dump(nfShex.gen_lookup_table(), sf, indent=2)
