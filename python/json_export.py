from os import path
import json
import requests
from typing import List
from ontobio.rdfgen.assoc_rdfgen import prefix_context
from prefixcommons.curie_util import contract_uri
from pyshexc.parser_impl import generate_shexj
from typing import Optional, List, Union
from ShExJSG.ShExJ import Shape, ShapeAnd, ShapeOr, ShapeNot, TripleConstraint, shapeExprLabel, shapeExpr, shapeExprLabel, tripleExpr, tripleExprLabel, OneOf, EachOf
from pyshex import PrefixLibrary


class NoctuaFormShex:
    def __init__(self):
        self.json_shapes = {}
        shex_url = "https://raw.githubusercontent.com/geneontology/go-shapes/master/shapes/go-cam-shapes.shex"
        shex_response = requests.get(shex_url)
        self.shex = generate_shexj.parse(shex_response.text)
        pref = PrefixLibrary(shex_response.text)
        self.pref_dict = {k:self.get_suffix(str(v)) for (k,v) in dict(pref).items()}

    def get_suffix(self, uri):
        suffix = contract_uri(uri, cmaps=[prefix_context])
        if len(suffix) > 0:
            return suffix[0]

        return path.basename(uri)
        

    def get_shape_name(self, uri):
        name = path.basename(uri).upper() 
        if '/go/' in uri:
            name = 'GO'+name
        return self.pref_dict.get(name, uri)

    def _load_expr(self, expr: Optional[Union[shapeExprLabel, shapeExpr]], preds=None) -> List:
   
        if(preds == None):
            preds = {}

        if isinstance(expr, str) and isinstance(preds, list):
            preds.append(self.get_shape_name(expr))

        if isinstance(expr, (ShapeOr, ShapeAnd)):
            for expr2 in expr.shapeExprs:
                self._load_expr(expr2, preds)

        elif isinstance(expr, ShapeNot):
            self._load_expr(expr.shapeExpr, preds)

        elif isinstance(expr, Shape):
            if expr.expression is not None:
                self._load_triple_expr(expr.expression, preds)
        
        return preds

            
    def _load_triple_expr(self,  expr: Union[tripleExpr, tripleExprLabel], preds=None) -> None:
        
        if isinstance(expr, (OneOf, EachOf)):
            for expr2 in expr.expressions:
                self._load_triple_expr(expr2, preds)

        elif isinstance(expr, TripleConstraint):
            if expr.valueExpr is not None:
                pred = self.get_suffix(expr.predicate)
                preds[pred] = []
                self._load_expr(expr.valueExpr, preds[pred])

    
    def parse(self):
        shapes = self.shex.shapes

        for shape in shapes:
            shape_name = self.get_shape_name(shape['id'])          
            print('Parsing Shape: ' + shape['id'])
            self.json_shapes[shape_name] = {}

            shexps = shape.shapeExprs or []       

            for expr in shexps:
                self.json_shapes[shape_name] = self._load_expr(expr)
            
                   
     
shelper = NoctuaFormShex()
shelper.parse()



with open("shex_dump.json", "w") as sf:
    json.dump(shelper.json_shapes, sf, indent=2)
