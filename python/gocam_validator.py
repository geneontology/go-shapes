import click
import logging
from pyshex.evaluate import evaluate
from rdflib import Graph, Namespace, URIRef
from typing import Optional, Set, List, Union, Dict, Tuple, Any
from rdflib.namespace import RDF, RDFS, OWL
from SPARQLWrapper import SPARQLWrapper, JSON
from cachier import cachier
from dataclasses import dataclass, field
import datetime


GOSHAPES = Namespace("http://purl.obolibrary.org/obo/go/shapes/")

cmap = {
    "http://purl.obolibrary.org/obo/GO_0003674" : "MolecularFunction",
    "http://purl.obolibrary.org/obo/GO_0008150" : "BiologicalProcess",
    "http://purl.obolibrary.org/obo/GO_0005575" : "CellularComponent"
}

shex_source = '../shapes/go-cam-shapes.shex'

def get_shexc():
    with open(shex_source,'r') as s:
        shexc = s.read()
    return shexc

@dataclass
class ValidationReport():

    shexc : Optional[str]
    rdf_file: Optional[str]
    all_successful : bool = True
    nodes_with_no_shape : List[URIRef] = field(default_factory=lambda: [])
    pass_list : Tuple[URIRef, URIRef, List[str]] = field(default_factory=lambda: [])
    fail_list : Tuple[URIRef, URIRef, List[str]] = field(default_factory=lambda: [])
    

@click.command()
@click.argument('rdf_file', nargs=-1)
def validate_files(rdf_file : [str]):
    shexc = get_shexc()

    all_files_successful = True
    for f in rdf_file:
        rpt = validate(f, shexc)
        print(f'File: {f} Success: {rpt.all_successful} PASS: {len(rpt.pass_list)} FAIL: {rpt.fail_list}')
        if not rpt.all_successful:
            all_files_successful = False
    print(f'Final report >> all files successful: { all_files_successful }')
        
def validate(filename : str, shexc : Optional[str] = None) -> ValidationReport:
    if shexc is None:
        shexc = get_shexc()
    g = Graph()
    g.parse(filename, format="turtle")
    inst_cls_tuples = []
    smap = {}
    rpt = ValidationReport(shexc=shexc, rdf_file=filename)

    for inst,p,cls in g.triples( (None, RDF.type, None) ):
        if cls in [OWL.Class, OWL.NamedIndividual, OWL.AnnotationProperty, OWL.ObjectProperty]:
            continue
        inst_cls_tuples.append( (inst, cls) )
        ancs = get_ancestors(cls)
        shape_classes = []
        for a in ancs:
            if a in cmap:
                # inject
                g.add( (cls, RDF.type, OWL.Class) )
                g.add( (cls, RDFS.subClassOf, URIRef(a)) )
                shape_classes.append(GOSHAPES[cmap[a]])
        if shape_classes:
            logging.debug(f'IC={inst} type {cls} sc={shape_classes} ancs={len(ancs)}')
            smap[inst] = shape_classes
        else:
            rpt.nodes_with_no_shape.append(inst)
            logging.info(f"No shape class for tuple {inst} {cls}")

    rpt.all_successful = True
    for inst, shape_classes in smap.items():
        for sc in shape_classes:
            success, reason = evaluate(g, shexc, inst, sc)
            if success:
                logging.info(f"Success: {inst} {sc}")
                rpt.pass_list.append( (inst, sc, reason) )
            else:
                rpt.all_successful = False
                logging.info(f"Fail: {inst} {sc} Reason: {reason}")
                rpt.fail_list.append( (inst, sc, reason) )
    return rpt


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
    validate_files()

