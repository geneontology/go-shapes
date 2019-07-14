import click
import logging
from pyshex.evaluate import evaluate
from rdflib import Graph, Namespace, URIRef
from typing import Optional, Set, List, Union, Dict, Any
from rdflib.namespace import RDF, RDFS, OWL
from SPARQLWrapper import SPARQLWrapper, JSON
from cachier import cachier
import datetime


GOSHAPES = Namespace("http://purl.obolibrary.org/obo/go/shapes/")

cmap = {
    "http://purl.obolibrary.org/obo/GO_0003674" : "MolecularFunction",
    "http://purl.obolibrary.org/obo/GO_0008150" : "BiologicalProcess",
    "http://purl.obolibrary.org/obo/GO_0005575" : "CellularComponent"
}

shex_source = '../shapes/go-cam-shapes.shex'

@click.command()
@click.argument('rdf_file', nargs=-1)
def validate(rdf_file : [str]):
    with open(shex_source,'r') as s:
        shexc = s.read()
        #print(f'Shex={shexc}')
        
    print(f'Files={rdf_file}')
    for f in rdf_file:
        g = Graph()
        g.parse(f, format="turtle")
        inst_cls_tuples = []
        smap = {}
        for inst,p,cls in g.triples( (None, RDF.type, None) ):
            inst_cls_tuples.append( (inst, cls) )
            ancs = get_ancestors(cls)
            #print(f'IC={inst} type {cls} ancs={len(ancs)}')
            shape_classes = []
            for a in ancs:
                if a in cmap:
                    # inject
                    g.add( (cls, RDF.type, OWL.Class) )
                    g.add( (cls, RDFS.subClassOf, URIRef(a)) )
                    shape_classes.append(GOSHAPES[cmap[a]])
            if shape_classes:
                print(f'IC={inst} type {cls} sc={shape_classes} ancs={len(ancs)}')
                smap[inst] = shape_classes
            else:
                print(f"No shape class for tuple {inst} {cls}")
        for inst, shape_classes in smap.items():
            for sc in shape_classes:
                rslt, reason = evaluate(g, shexc, inst, sc)
                print(f"  Focus: {inst}\n SC: {sc}  Start: {rslt}\n  Reason: {str(reason)}")


@cachier(stale_after=datetime.timedelta(days=3))
def get_ancestors(cls):
    sparql = SPARQLWrapper("http://rdf.geneontology.org/sparql")
    logging.info("Made wrapper: {}".format(sparql))
    sparql.setQuery(f"""
    SELECT ?anc WHERE {{
    <{cls}> rdfs:subClassOf* ?anc .
    FILTER(isURI(?anc))
    }}
    """)
    sparql.setReturnFormat(JSON)
    results = sparql.query().convert()
    bindings = results['results']['bindings']
    ancs = [r['anc']['value'] for r in bindings ]
    return ancs

            
if __name__ == "__main__":
    validate()


